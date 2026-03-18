package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.IOUtil;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;

import java.io.InputStream;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Lu, Mingyueyixi
 * 增强型 Fresco 拦截器。不再仅依赖 URI，而是拦截解码过程获取字节流。
 */
public class FrescoCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        // 1. 尝试 Hook 核心解码器 DefaultImageDecoder
        Class<?> defaultImageDecoderClazz = ClazzN.from("com.facebook.imagepipeline.decoder.DefaultImageDecoder");
        Class<?> encodedImageClazz = ClazzN.from("com.facebook.imagepipeline.image.EncodedImage");
        Class<?> qualityInfoClazz = ClazzN.from("com.facebook.imagepipeline.image.QualityInfo");

        if (defaultImageDecoderClazz != null && encodedImageClazz != null) {
            // public CloseableImage decode(final EncodedImage encodedImage, final int length, final QualityInfo qualityInfo, final ImageDecodeOptions options)
            XposedHelpers2.hookAllMethods(defaultImageDecoderClazz, "decode", new XC_MethodHook2() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Object encodedImage = param.args[0];
                    if (encodedImage == null) return;

                    // 从 EncodedImage 中获取输入流
                    try {
                        // 使用 Xposed 直接调用 getInputStream
                        InputStream inputStream = XposedHelpers2.callMethod(encodedImage, "getInputStream");
                        if (inputStream != null) {
                            // 注意：此处读取流可能会导致原 App 无法再次读取（如果流不支持 reset）
                            // 但是 Fresco 的 getInputStream() 通常会返回一个新的流拷贝（如基于 PooledByteBuffer）
                            byte[] data = IOUtil.readToBytes(inputStream);
                            if (data != null && data.length > 0) {
                                LogUtil.d("FrescoCatcherPlugin", "Captured from DefaultImageDecoder, size: " + data.length);
                                PicExportManager.getInstance().exportByteArray(data, null);
                            }
                            IOUtil.closeQuietly(inputStream);
                        }
                    } catch (Throwable t) {
                        LogUtil.w("FrescoCatcherPlugin decode hook error", t);
                    }
                }
            });
        }

        // 2. 兜底方案：Hook CloseableStaticBitmap 的创建（这是 Fresco 解码后的 Bitmap 容器）
        Class<?> closeableStaticBitmapClazz = ClazzN.from("com.facebook.imagepipeline.image.CloseableStaticBitmap");
        if (closeableStaticBitmapClazz != null) {
            XposedHelpers2.findAndHookConstructor(closeableStaticBitmapClazz, 
                Bitmap.class, qualityInfoClazz, int.class, int.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("FrescoCatcherPlugin", "Captured from CloseableStaticBitmap constructor");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                });
        }
    }
}
