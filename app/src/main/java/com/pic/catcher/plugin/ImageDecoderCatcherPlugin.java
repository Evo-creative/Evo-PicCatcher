package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author PicCatcher
 * @description 拦截 Android 9.0+ 的 ImageDecoder API。
 * 这是现代 Android 系统推荐的解码方式，BitmapFactory 的继任者。
 */
public class ImageDecoderCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return;

        // 1. Hook decodeBitmap
        XposedHelpers2.findAndHookMethod(
                ImageDecoder.class,
                "decodeBitmap",
                ImageDecoder.Source.class,
                ImageDecoder.OnHeaderDecodedListener.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.getResult();
                        if (bitmap != null) {
                            LogUtil.d("ImageDecoderCatcher", "decodeBitmap captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // 2. Hook decodeDrawable
        XposedHelpers2.findAndHookMethod(
                ImageDecoder.class,
                "decodeDrawable",
                ImageDecoder.Source.class,
                ImageDecoder.OnHeaderDecodedListener.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Drawable drawable = (Drawable) param.getResult();
                        if (drawable instanceof BitmapDrawable) {
                            LogUtil.d("ImageDecoderCatcher", "decodeDrawable (Bitmap) captured");
                            PicExportManager.getInstance().exportBitmap(((BitmapDrawable) drawable).getBitmap());
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable instanceof AnimatedImageDrawable) {
                            LogUtil.d("ImageDecoderCatcher", "decodeDrawable (Animated) captured");
                            // 这种动态图目前可能需要额外处理字节流，或者通过 Canvas 捕捉
                        }
                    }
                }
        );
    }
}
