package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.pic.catcher.ClazzN;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Picasso 拦截插件
 */
public class PicassoCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        final ModuleConfig config = ModuleConfig.getInstance();

        // 1. 拦截 BitmapHunter (Picasso 内部执行解码的任务)
        Class<?> bitmapHunterClass = ClazzN.from("com.squareup.picasso.BitmapHunter", lpparam.classLoader);
        if (bitmapHunterClass != null) {
            XposedHelpers2.findAndHookMethod(bitmapHunterClass, "decodeStream", java.io.InputStream.class, "com.squareup.picasso.Request", new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!config.isCatchPicassoPic()) return;
                    Bitmap result = (Bitmap) param.getResult();
                    if (result != null) {
                        PicExportManager.getInstance().exportBitmap(result);
                    }
                }
            });
        }

        // 2. 拦截 RequestHandler 的结果 (更底层的拦截)
        Class<?> requestHandlerResultClass = ClazzN.from("com.squareup.picasso.RequestHandler$Result", lpparam.classLoader);
        if (requestHandlerResultClass != null) {
            XposedHelpers2.hookAllConstructors(requestHandlerResultClass, new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!config.isCatchPicassoPic()) return;
                    // Result 构造函数可能有 Bitmap 或 InputStream
                    Object bitmap = XposedHelpers2.getObjectField(param.thisObject, "bitmap");
                    if (bitmap instanceof Bitmap) {
                        PicExportManager.getInstance().exportBitmap((Bitmap) bitmap);
                    }
                    // 注意：InputStream 暂不拦截，因为可能导致后续 Picasso 无法读取
                }
            });
        }
    }
}
