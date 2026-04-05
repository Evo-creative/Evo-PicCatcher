package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NativeBitmapCatcherPlugin implements IPlugin {
    // 使用 ThreadLocal 防止 Hook 内部递归调用导致闪退
    private static final ThreadLocal<Boolean> isHooking = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        
        XposedHelpers2.hookAllMethods(Bitmap.class, "createBitmap", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isHooking.get()) return;
                try {
                    isHooking.set(true);
                    Bitmap bitmap = (Bitmap) param.getResult();
                    if (isValid(bitmap)) {
                        LogUtil.d("NativeBitmapCatcher", "Captured from createBitmap");
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                } finally {
                    isHooking.set(false);
                }
            }
        });

        XposedHelpers2.hookAllMethods(Bitmap.class, "copy", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isHooking.get()) return;
                try {
                    isHooking.set(true);
                    Bitmap bitmap = (Bitmap) param.getResult();
                    if (isValid(bitmap)) {
                        LogUtil.d("NativeBitmapCatcher", "Captured from Bitmap.copy");
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                } finally {
                    isHooking.set(false);
                }
            }
        });
    }

    private boolean isValid(Bitmap bitmap) {
        // 增加对 bitmap.isRecycled() 的极其严格的检查
        return bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 20 && bitmap.getHeight() > 20;
    }
}
