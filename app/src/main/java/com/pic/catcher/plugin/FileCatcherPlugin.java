package com.pic.catcher.plugin;

import android.content.Context;
import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.pic.catcher.config.ModuleConfig;
import java.io.FileOutputStream;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 修复版文件流拦截器
 * 使用 ThreadLocal 解决“自 Hook”冲突，并增强了数组边界检查以防止崩溃。
 */
public class FileCatcherPlugin implements IPlugin {
    
    // 关键：防止模块自己写文件时触发自己的 Hook 导致死循环
    public static final ThreadLocal<Boolean> isInternalWriting = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        
        XposedHelpers2.findAndHookMethod(FileOutputStream.class, "write", byte[].class, int.class, int.class, new XC_MethodHook2() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // 如果是模块自己在写文件，立即放行
                if (Boolean.TRUE.equals(isInternalWriting.get())) return;
                
                if (!ModuleConfig.getInstance().isCatchNativePic()) return;

                byte[] b = (byte[]) param.args[0];
                int off = (int) param.args[1];
                int len = (int) param.args[2];

                // 增强边界检查：防止 ArrayIndexOutOfBoundsException
                if (b == null || len < 8 || off < 0 || off + len > b.length) return;

                if (isImage(b, off, len)) {
                    byte[] data = new byte[len];
                    System.arraycopy(b, off, data, 0, len);
                    // 异步分发处理
                    PicExportManager.getInstance().exportByteArray(data, null);
                }
            }
        });
    }

    private boolean isImage(byte[] data, int off, int len) {
        // JPEG: FF D8 FF
        if (len >= 3 && (data[off] & 0xFF) == 0xFF && (data[off+1] & 0xFF) == 0xD8 && (data[off+2] & 0xFF) == 0xFF) return true;
        // PNG: 89 50 4E 47
        if (len >= 4 && (data[off] & 0xFF) == 0x89 && data[off+1] == 'P' && data[off+2] == 'N' && data[off+3] == 'G') return true;
        // WebP/RIFF: RIFF .... WEBP
        if (len >= 12 && data[off] == 'R' && data[off+1] == 'I' && data[off+2] == 'F' && data[off+3] == 'F') return true;
        // GIF: GIF8
        if (len >= 4 && data[off] == 'G' && data[off+1] == 'I' && data[off+2] == 'F') return true;
        return false;
    }
}
