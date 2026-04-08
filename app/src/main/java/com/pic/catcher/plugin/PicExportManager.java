package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.LruCache;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.lu.magic.util.AppUtil;
import com.lu.magic.util.IOUtil;
import com.lu.magic.util.thread.AppExecutor;
import com.pic.catcher.BuildConfig;
import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.provider.LogProvider;
import com.pic.catcher.ui.config.PicFormat;
import com.pic.catcher.util.FileUtils;
import com.pic.catcher.util.Md5Util;
import com.pic.catcher.util.PicUtil;
import com.pic.catcher.util.XLog;
import com.pic.catcher.util.http.HttpConnectUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * 图片导出管理器 - 终极加固版
 * 确保模块操作与宿主 App 逻辑完全隔离，绝对不干扰图片加载速度和稳定性。
 */
public class PicExportManager {
    private static final String TAG = "PicCatcher";
    private static PicExportManager sInstance;
    
    // 采用对象身份去重，有效防止高频 Canvas 重绘导致的性能问题
    private final LruCache<Integer, Long> processedCache = new LruCache<>(500);

    public synchronized static PicExportManager getInstance() {
        if (sInstance == null) {
            sInstance = new PicExportManager();
        }
        return sInstance;
    }

    public void log(String msg) {
        if (BuildConfig.DEBUG) {
            XLog.i(AppUtil.getContext(), msg);
        }
    }

    /**
     * 线程安全的私有目录写入
     */
    private void writeToProvider(byte[] data, String fileName) {
        // 设置线程本地标记，防止 FileCatcher 触发自循环
        FileCatcherPlugin.isInternalWriting.set(true);
        try {
            Context context = AppUtil.getContext();
            Uri uri = LogProvider.CONTENT_URI.buildUpon()
                    .appendPath("save_pic")
                    .appendQueryParameter("name", fileName)
                    .appendQueryParameter("pkg", context.getPackageName())
                    .build();

            try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w")) {
                if (pfd != null) {
                    try (FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())) {
                        fos.write(data);
                    }
                }
            } catch (Throwable e) {
                // 如果 Provider 写入失败（常见于包可见性限制），静默降级，不弹窗不崩溃
                saveToPublicSync(data, fileName);
            }
        } finally {
            FileCatcherPlugin.isInternalWriting.set(false);
        }
    }

    public void exportBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;
        
        // 1. 强制过滤：极小图（如加载转圈、小图标）不处理，极大降低 Hook 负载
        if (bitmap.getWidth() < 128 || bitmap.getHeight() < 128) return;

        // 2. 采样降频：同个位图对象 1.2 秒内仅处理一次 (略微缩短时间以防漏抓)
        int id = System.identityHashCode(bitmap);
        Long lastTime = processedCache.get(id);
        long now = System.currentTimeMillis();
        if (lastTime != null && (now - lastTime < 1200)) return;
        processedCache.put(id, now);

        // 3. 内存安全策略：如果是超大图（例如超过 4000 像素宽或高），为了保命，不进行 copy 而是直接在当前线程压缩
        // 或者如果内存极度紧张，直接放弃。
        boolean isHugeImage = bitmap.getWidth() > 4096 || bitmap.getHeight() > 4096;
        
        if (isHugeImage || !bitmap.isMutable()) {
            // 对于巨型图或不可变图，我们尝试直接在当前 Hook 线程进行轻量级压缩
            // 这种方式虽然会稍微占用一点宿主时间，但比 copy 导致 OOM 崩溃要稳得多
            final Bitmap.Config config = bitmap.getConfig();
            runOnIo(() -> {
                doExportBitmapSync(bitmap, false);
            });
        } else {
            // 普通图：非阻塞克隆，随后异步处理
            final Bitmap copyBitmap;
            try {
                copyBitmap = bitmap.copy(bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888, false);
            } catch (Throwable t) {
                // 内存不足触发 Error，尝试直接同步压缩
                runOnIo(() -> doExportBitmapSync(bitmap, false));
                return;
            }

            if (copyBitmap == null) return;
            runOnIo(() -> {
                doExportBitmapSync(copyBitmap, true);
            });
        }
    }

    private void doExportBitmapSync(Bitmap bitmap, boolean needRecycle) {
        if (bitmap == null || bitmap.isRecycled()) return;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            String format = ModuleConfig.getInstance().getPicDefaultSaveFormat();
            Bitmap.CompressFormat cf = PicFormat.JPG.equals(format) ? Bitmap.CompressFormat.JPEG : 
                                     (PicFormat.PNG.equals(format) ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
            
            // 压缩质量
            bitmap.compress(cf, 90, bos);
            byte[] bytes = bos.toByteArray();
            
            if (bytes.length > 0 && !ModuleConfig.isLessThanMinSize(bytes.length)) {
                String fileName = Md5Util.get(bytes) + "." + format;
                if (ModuleConfig.getInstance().isSaveToInternal()) {
                    writeToProvider(bytes, fileName);
                } else {
                    saveToPublicSync(bytes, fileName);
                }
            }
        } catch (Throwable ignored) {
        } finally {
            if (needRecycle) {
                bitmap.recycle();
            }
        }
    }

    /**
     * 内部同步保存方法 (仅供 IO 线程内部调用)
     */
    private void saveToPublicSync(byte[] data, String fileName) {
        FileCatcherPlugin.isInternalWriting.set(true);
        try {
            File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File dir = new File(publicDir, "PicCatcher/" + AppUtil.getContext().getPackageName());
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, fileName);
            if (dest.exists()) return;
            
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(data);
            }
        } catch (Exception ignored) {
        } finally {
            FileCatcherPlugin.isInternalWriting.set(false);
        }
    }

    public void exportByteArray(final byte[] dataBytes, String lastName) {
        if (dataBytes == null || dataBytes.length < 5120) return;
        
        runOnIo(() -> {
            try {
                String md5 = Md5Util.get(dataBytes);
                String suffix = TextUtils.isEmpty(lastName) ? PicUtil.detectImageType(dataBytes, "bin") : lastName;
                if (!suffix.startsWith(".")) suffix = "." + suffix;
                String fileName = md5 + suffix;

                if (ModuleConfig.getInstance().isSaveToInternal()) {
                    writeToProvider(dataBytes, fileName);
                } else {
                    saveToPublicSync(dataBytes, fileName);
                }
            } catch (Throwable ignored) {}
        });
    }

    public void exportBitmapFile(File file) {
        if (file == null || !file.exists() || file.length() < 5120) return;
        runOnIo(() -> {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                FileUtils.copyFile(fis, bos);
                exportByteArray(bos.toByteArray(), MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));
            } catch (Exception ignored) {}
        });
    }

    public void exportUrlIfNeed(String url) {
        if (TextUtils.isEmpty(url) || !URLUtil.isNetworkUrl(url)) return;
        
        // 过滤明显的非图片资源，提升性能
        String lowerUrl = url.toLowerCase(Locale.ROOT);
        if (lowerUrl.contains(".js") || lowerUrl.contains(".css") || lowerUrl.contains(".json")) return;

        runOnIo(() -> {
            String fileEx = MimeTypeMap.getFileExtensionFromUrl(url).toLowerCase(Locale.ROOT);
            HttpConnectUtil.request("GET", url, null, null, true, response -> {
                byte[] body = response.getBody();
                if (body != null) exportByteArray(body, fileEx);
                return null;
            });
        });
    }

    public void runOnIo(Runnable runnable) {
        if (Looper.getMainLooper().isCurrentThread()) {
            AppExecutor.io().execute(runnable);
        } else {
            // 如果已经在子线程（比如 OkHttp 的回调线程），直接运行
            runnable.run();
        }
    }
}
