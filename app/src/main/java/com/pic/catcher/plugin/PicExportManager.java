package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.lu.magic.util.AppUtil;
import com.lu.magic.util.thread.AppExecutor;
import com.pic.catcher.config.ModuleConfig;
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
 * 图片导出管理器 - 采用宿主私有缓存中转方案
 * 解决 EPERM 和分区存储限制
 */
public class PicExportManager {
    private static final String TAG = "PicCatcher";
    private static PicExportManager sInstance;

    public synchronized static PicExportManager getInstance() {
        if (sInstance == null) {
            sInstance = new PicExportManager();
        }
        return sInstance;
    }

    public void log(String msg) {
        XLog.i(AppUtil.getContext(), msg);
    }

    /**
     * 将字节数组写入宿主自己的 Android/data/[pkg]/cache/PicCatcher 目录
     */
    private void writeToHostCache(byte[] data, String fileName) {
        Context context = AppUtil.getContext();
        // 宿主私有缓存路径
        File cacheDir = new File(context.getExternalCacheDir(), "PicCatcher");
        
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            saveToHostPrivate(data, fileName);
            return;
        }
        
        File cacheFile = new File(cacheDir, fileName);
        try {
            if (cacheFile.exists()) cacheFile.delete();
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(data);
                fos.flush();
                try { fos.getFD().sync(); } catch (Exception ignored) {}
            }
            // 宿主逻辑完成，文件等待模块 Service 扫描搬运
        } catch (Exception e) {
            log("Cache write failed: " + e.getMessage());
            saveToHostPrivate(data, fileName);
        }
    }

    private void saveToHostPrivate(byte[] data, String fileName) {
        try {
            File dir = AppUtil.getContext().getExternalFilesDir("Pictures");
            if (dir != null) {
                File dest = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(data);
                }
            }
        } catch (Exception e) {
            log("Fallback failed: " + e.getMessage());
        }
    }

    public void exportBitmap(Bitmap bitmap) {
        runOnIo(() -> {
            if (bitmap == null || bitmap.isRecycled()) return;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                String format = ModuleConfig.getInstance().getPicDefaultSaveFormat();
                Bitmap.CompressFormat cf = PicFormat.JPG.equals(format) ? Bitmap.CompressFormat.JPEG : 
                                         (PicFormat.PNG.equals(format) ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                synchronized (bitmap) {
                    if (bitmap.isRecycled()) return;
                    bitmap.compress(cf, 100, bos);
                }
                byte[] bytes = bos.toByteArray();
                if (bytes.length == 0 || ModuleConfig.isLessThanMinSize(bytes.length)) return;
                String fileName = Md5Util.get(bytes) + "." + format;
                writeToHostCache(bytes, fileName);
            } catch (Exception e) {
                log("Export bitmap error: " + e.getMessage());
            }
        });
    }

    public void exportByteArray(final byte[] dataBytes, String lastName) {
        if (dataBytes == null || dataBytes.length == 0) return;
        if (ModuleConfig.isLessThanMinSize(dataBytes.length)) return;
        runOnIo(() -> {
            String suffix = TextUtils.isEmpty(lastName) ? PicUtil.detectImageType(dataBytes, "bin") : lastName;
            if (!suffix.startsWith(".")) suffix = "." + suffix;
            String fileName = Md5Util.get(dataBytes) + suffix;
            writeToHostCache(dataBytes, fileName);
        });
    }

    public void exportBitmapFile(File file) {
        runOnIo(() -> {
            if (!file.exists()) return;
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                FileUtils.copyFile(fis, bos);
                exportByteArray(bos.toByteArray(), MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));
            } catch (Exception e) {
                log("Export file error: " + e.getMessage());
            }
        });
    }

    public void exportUrlIfNeed(String url) {
        runOnIo(() -> {
            if (TextUtils.isEmpty(url)) return;
            String fileEx = MimeTypeMap.getFileExtensionFromUrl(url).toLowerCase(Locale.ROOT);
            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                HttpConnectUtil.request("GET", url, null, null, true, response -> {
                    byte[] body = response.getBody();
                    if (body != null) exportByteArray(body, fileEx);
                    return null;
                });
            }
        });
    }

    public void runOnIo(Runnable runnable) {
        if (Looper.getMainLooper().isCurrentThread()) {
            AppExecutor.io().execute(runnable);
        } else {
            runnable.run();
        }
    }
}
