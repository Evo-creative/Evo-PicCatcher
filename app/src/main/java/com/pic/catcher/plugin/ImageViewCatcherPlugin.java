package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author PicCatcher
 * @description 拦截 ImageView 的设置方法，作为最后的 UI 层兜底。
 * 只要图片显示在屏幕上，基本都会经过这里。
 */
public class ImageViewCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        
        // 1. Hook setImageDrawable
        XposedHelpers2.findAndHookMethod(
                ImageView.class,
                "setImageDrawable",
                Drawable.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchDrawablePic()) {
                            return;
                        }
                        Drawable drawable = (Drawable) param.args[0];
                        processDrawable(drawable);
                    }
                }
        );

        // 2. Hook setImageBitmap
        XposedHelpers2.findAndHookMethod(
                ImageView.class,
                "setImageBitmap",
                Bitmap.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("ImageViewCatcherPlugin", "setImageBitmap captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );
    }

    private void processDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                LogUtil.d("ImageViewCatcherPlugin", "BitmapDrawable captured from ImageView");
                PicExportManager.getInstance().exportBitmap(bitmap);
            }
        }
        // 如果是其他类型的 Drawable (如 Glide 的 GifDrawable)，
        // 它们通常有内部的转 Bitmap 或转字节的方法，可以根据需要继续扩展
    }
}
