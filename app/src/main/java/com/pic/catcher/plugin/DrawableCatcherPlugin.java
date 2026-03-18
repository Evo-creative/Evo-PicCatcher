package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Mingyueyixi
 * @description 拦截 Drawable 创建，特别是 BitmapDrawable
 */
public class DrawableCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        // Hook BitmapDrawable 的构造函数，捕捉传入的 Bitmap
        XposedHelpers2.findAndHookConstructor(
                BitmapDrawable.class,
                android.content.res.Resources.class,
                Bitmap.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[1];
                        if (bitmap != null) {
                            LogUtil.d("DrawableCatcherPlugin", "BitmapDrawable constructor captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // 也可以 Hook setBitmap 方法 (虽然它在某些 Android 版本上是隐藏的或很少直接调用)
        try {
            XposedHelpers2.findAndHookMethod(
                    BitmapDrawable.class,
                    "setBitmap",
                    Bitmap.class,
                    new XC_MethodHook2() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Bitmap bitmap = (Bitmap) param.args[0];
                            if (bitmap != null) {
                                LogUtil.d("DrawableCatcherPlugin", "BitmapDrawable.setBitmap captured");
                                PicExportManager.getInstance().exportBitmap(bitmap);
                            }
                        }
                    }
            );
        } catch (Throwable ignored) {
            // setBitmap 可能不存在于所有版本
        }
    }
}
