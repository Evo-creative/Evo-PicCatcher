package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 修复版 Fresco 拦截器 - 3.6.17
 * 移除导致图片阻断的 getInputStream 直接读取逻辑。
 */
public class FrescoCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        
        // 方案：不再 Hook decode() 前的数据流（这会造成阻断），改为 Hook 解码容器 CloseableStaticBitmap。
        // 这样既能拿到图片，又能保证 Fresco 正常渲染。
        
        Class<?> closeableStaticBitmapClazz = ClazzN.from("com.facebook.imagepipeline.image.CloseableStaticBitmap");
        Class<?> qualityInfoClazz = ClazzN.from("com.facebook.imagepipeline.image.QualityInfo");

        if (closeableStaticBitmapClazz != null) {
            // Hook 构造函数：public CloseableStaticBitmap(Bitmap bitmap, QualityInfo qualityInfo, int rotationAngle, int exifOrientation)
            XposedHelpers2.findAndHookConstructor(closeableStaticBitmapClazz, 
                Bitmap.class, qualityInfoClazz, int.class, int.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("FrescoCatcher", "Captured from CloseableStaticBitmap constructor");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                });
        }
        
        // 针对 Gif 等动图，Fresco 使用 CloseableAnimatedImage
        Class<?> animatedImageClazz = ClazzN.from("com.facebook.imagepipeline.image.CloseableAnimatedImage");
        if (animatedImageClazz != null) {
            XposedHelpers2.hookAllMethods(animatedImageClazz, "getUnderlyingImage", new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // 获取内部的动画资源
                }
            });
        }
    }
}
