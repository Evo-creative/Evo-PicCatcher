package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author PicCatcher
 * @description 拦截 Coil 图片加载库。
 * Coil 是目前 Kotlin 开发中最流行的图片库。
 */
public class CoilCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        // Coil 的核心类通常是 coil.RealImageLoader 或 coil.ImageLoader
        Class<?> realImageLoaderClazz = ClazzN.from("coil.RealImageLoader");
        if (realImageLoaderClazz == null) return;

        // Hook execute 或 enqueue 方法后的结果
        // suspend fun execute(request: ImageRequest): ExecuteResult
        // 这里尝试 Hook 内部的转换或结果处理
        XposedHelpers2.hookAllMethods(realImageLoaderClazz, "execute", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object result = param.getResult(); // SuccessResult or ErrorResult
                if (result == null) return;

                // SuccessResult 有一个 drawable 字段
                try {
                    Drawable drawable = (Drawable) XposedHelpers2.getObjectField(result, "drawable");
                    if (drawable instanceof BitmapDrawable) {
                        LogUtil.d("CoilCatcher", "Captured from SuccessResult");
                        PicExportManager.getInstance().exportBitmap(((BitmapDrawable) drawable).getBitmap());
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }
}
