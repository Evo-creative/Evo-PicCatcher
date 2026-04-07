package com.pic.catcher.plugin;

import android.content.Context;
import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.config.ModuleConfig;
import java.io.FileOutputStream;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 重写的全文件 IO 拦截器
 * 直接监控宿主 App 的文件写入行为，通过检测 Magic Number 截获图片
 */
public class FileCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        
        // 拦截 FileOutputStream.write(byte[] b, int off, int len)
        XposedHelpers2.findAndHookMethod(FileOutputStream.class, "write", byte[].class, int.class, int.class, new XC_MethodHook2() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchNativePic()) return;

                byte[] b = (byte[]) param.args[0];
                int off = (int) param.args[1];
                int len = (int) param.args[2];

                if (b == null || len < 12) return;

                // 实时检测图片魔数 (Magic Number)
                if (isImage(b, off)) {
                    byte[] data = new byte[len];
                    System.arraycopy(b, off, data, 0, len);
                    LogUtil.d("FileCatcher", "Captured image from FileOutputStream.write, size: " + len);
                    PicExportManager.getInstance().exportByteArray(data, null);
                }
            }
        });
    }

    private boolean isImage(byte[] data, int off) {
        // JPEG: FF D8 FF
        if ((data[off] & 0xFF) == 0xFF && (data[off+1] & 0xFF) == 0xD8) return true;
        // PNG: 89 50 4E 47
        if ((data[off] & 0xFF) == 0x89 && data[off+1] == 'P' && data[off+2] == 'N' && data[off+3] == 'G') return true;
        // WebP: RIFF .... WEBP
        if (data[off] == 'R' && data[off+1] == 'I' && data[off+2] == 'F' && data[off+3] == 'F') return true;
        // GIF: GIF8
        if (data[off] == 'G' && data[off+1] == 'I' && data[off+2] == 'F') return true;
        return false;
    }
}
