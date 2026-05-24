package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.lu.magic.util.AppUtil;
import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.ui.config.PicFormat;
import com.pic.catcher.util.FileUtils;
import com.pic.catcher.util.Md5Util;
import com.pic.catcher.util.PicUtil;
import com.pic.catcher.util.http.HttpConnectUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 图片导出管理器
 */
public class PicExportManager {
    private static final String TAG = "PicCatcher_Host";
    private static PicExportManager sInstance;
    private File mCachedCacheDir;
    
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService sWorker = new ThreadPoolExecutor(
            Math.max(2, Math.min(CPU_COUNT - 1, 4)), 
            8,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            r -> {
                Thread t = new Thread(r, "PicExportWorker");
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            },
            new ThreadPoolExecutor.DiscardOldestPolicy() 
    );

    private final LruCache<Integer, Boolean> mProcessedIdCache = new LruCache<>(200);

    public synchronized static PicExportManager getInstance() {
        if (sInstance == null) {
            sInstance = new PicExportManager();
        }
        return sInstance;
    }

    private File getPicCacheDir() {
        if (mCachedCacheDir == null) {
            Context context = AppUtil.getContext();
            if (context != null) {
                File cacheDir = context.getExternalCacheDir();
                if (cacheDir != null) {
                    File dir = new File(cacheDir, "PicCatcher");
                    if (dir.exists() || dir.mkdirs()) {
                        mCachedCacheDir = dir;
                    }
                }
            }
        }
        return mCachedCacheDir;
    }

    /**
     * 导出 Bitmap
     */
    public void exportBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;
        
        // 去重过滤
        int identity = System.identityHashCode(bitmap);
        if (mProcessedIdCache.get(identity) != null) return;
        mProcessedIdCache.put(identity, true);

        sWorker.execute(() -> {
            try {
                if (bitmap.isRecycled()) return;
                
                String format = ModuleConfig.getInstance().getPicDefaultSaveFormat();
                Bitmap.CompressFormat cf = PicFormat.JPG.equals(format) ? Bitmap.CompressFormat.JPEG : 
                                         (PicFormat.PNG.equals(format) ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    int quality = ModuleConfig.getInstance().getPicQuality();
                    bitmap.compress(cf, quality, bos);
                    byte[] bytes = bos.toByteArray();
                    
                    // 仅使用配置中的过滤
                    if (ModuleConfig.isLessThanMinSize((long) bytes.length)) return;
                    
                    // 优化：使用简单的 Hash 配合长度代替 MD5，减少 CPU 占用
                    String fileName = Integer.toHexString(bitmap.hashCode()) + "_" + bytes.length + "." + format;
                    writeToHostCache(bytes, fileName);
                }
            } catch (Throwable ignored) {}
        });
    }

    /**
     * 导出字节数组 (通用)
     */
    public void exportByteArray(final byte[] dataBytes, String lastName) {
        if (dataBytes == null) return;
        if (ModuleConfig.isLessThanMinSize((long) dataBytes.length)) return;
        
        int identity = System.identityHashCode(dataBytes);
        if (mProcessedIdCache.get(identity) != null) return;
        mProcessedIdCache.put(identity, true);

        sWorker.execute(() -> {
            try {
                String suffix = TextUtils.isEmpty(lastName) ? PicUtil.detectImageType(dataBytes, "bin") : lastName;
                if (!suffix.startsWith(".")) suffix = "." + suffix;
                // 快速生成文件名
                String fileName = "raw_" + System.identityHashCode(dataBytes) + "_" + dataBytes.length + suffix;
                writeToHostCache(dataBytes, fileName);
            } catch (Throwable ignored) {}
        });
    }

    public void exportBitmapFile(File file) {
        if (file == null || !file.exists()) return;
        if (ModuleConfig.isLessThanMinSize(file.length())) return;
        
        sWorker.execute(() -> {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                FileUtils.copyFile(fis, bos);
                byte[] data = bos.toByteArray();
                String ext = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
                exportByteArrayInternal(data, ext);
            } catch (Exception ignored) {}
        });
    }

    private void exportByteArrayInternal(byte[] data, String suffix) {
        if (data == null) return;
        if (ModuleConfig.isLessThanMinSize((long) data.length)) return;
        
        String ext = TextUtils.isEmpty(suffix) ? PicUtil.detectImageType(data, "bin") : suffix;
        if (!ext.startsWith(".")) ext = "." + ext;
        String fileName = Md5Util.get(data) + ext;
        writeToHostCache(data, fileName);
    }

    private void writeToHostCache(byte[] data, String fileName) {
        File cacheDir = getPicCacheDir();
        if (cacheDir == null) return;
        
        File cacheFile = new File(cacheDir, fileName);
        if (cacheFile.exists()) return;

        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            fos.write(data);
            fos.flush();
            Log.i(TAG, "Successfully captured to: " + cacheFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Write failed: " + fileName, e);
        }
    }

    public void exportUrlIfNeed(String url) {
        if (TextUtils.isEmpty(url)) return;
        sWorker.execute(() -> {
            String fileEx = MimeTypeMap.getFileExtensionFromUrl(url).toLowerCase(Locale.ROOT);
            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                HttpConnectUtil.request("GET", url, null, null, true, response -> {
                    byte[] body = response.getBody();
                    if (body != null) exportByteArrayInternal(body, fileEx);
                    return null;
                });
            }
        });
    }
}
