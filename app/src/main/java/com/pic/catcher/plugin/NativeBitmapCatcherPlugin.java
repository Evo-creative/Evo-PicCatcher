package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.config.ModuleConfig;

import java.nio.Buffer;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 增强型本地位图拦截器 (3.6.6)
 * 针对 JMComic 等具有图片拼接和底层混淆机制的 App 进行终极优化
 */
public class NativeBitmapCatcherPlugin implements IPlugin {
    private static final ThreadLocal<Boolean> isHooking = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        
        // 1. 拦截常规 createBitmap
        XposedHelpers2.hookAllMethods(Bitmap.class, "createBitmap", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchNativePic()) return;
                if (isHooking.get()) return;
                try {
                    isHooking.set(true);
                    Bitmap bitmap = (Bitmap) param.getResult();
                    if (isValid(bitmap) && (bitmap.getHeight() > 800 || bitmap.getWidth() > 800)) {
                        LogUtil.d("NativeBitmapCatcher", "Captured large Bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                } finally {
                    isHooking.set(false);
                }
            }
        });

        // 2. 拦截像素推送 (核心：针对底层解密拼接后的输出点)
        XposedHelpers2.findAndHookMethod(Bitmap.class, "copyPixelsToBuffer", Buffer.class, new XC_MethodHook2() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchNativePic()) return;
                Bitmap bitmap = (Bitmap) param.thisObject;
                if (isValid(bitmap) && (bitmap.getWidth() > 300 && bitmap.getHeight() > 300)) {
                    LogUtil.d("NativeBitmapCatcher", "Captured from copyPixelsToBuffer: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });

        // 3. 拦截像素覆盖 (针对 setPixels)
        XposedHelpers2.hookAllMethods(Bitmap.class, "setPixels", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchNativePic()) return;
                Bitmap bitmap = (Bitmap) param.thisObject;
                if (isValid(bitmap) && (bitmap.getHeight() > 1000)) { // 漫画长图特性
                    LogUtil.d("NativeBitmapCatcher", "Captured long page from setPixels");
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });

        // 4. 拦截构造函数 (针对 Native 创建的位图)
        XposedHelpers2.findAndHookConstructor(Bitmap.class, long.class, byte[].class, int.class, int.class, int.class, boolean.class, boolean.class, float[].class, "android.graphics.ColorSpace", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchNativePic()) return;
                Bitmap bitmap = (Bitmap) param.thisObject;
                if (isValid(bitmap) && (bitmap.getWidth() > 500 || bitmap.getHeight() > 500)) {
                    LogUtil.d("NativeBitmapCatcher", "Captured from Native constructor");
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });
    }

    private boolean isValid(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 100 && bitmap.getHeight() > 100;
    }
}
