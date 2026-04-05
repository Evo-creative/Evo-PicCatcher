package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Mingyueyixi
 * @description 拦截 Canvas.drawBitmap，这是一个底层的“兜底”方案。
 */
public class CanvasCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        // Hook Canvas.drawBitmap(Bitmap, float, float, Paint)
        XposedHelpers2.findAndHookMethod(
                Canvas.class,
                "drawBitmap",
                Bitmap.class,
                float.class,
                float.class,
                Paint.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("CanvasCatcherPlugin", "Canvas.drawBitmap(Bitmap, float, float, Paint) captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // Hook Canvas.drawBitmap(Bitmap, Rect, Rect, Paint)
        XposedHelpers2.findAndHookMethod(
                Canvas.class,
                "drawBitmap",
                Bitmap.class,
                Rect.class,
                Rect.class,
                Paint.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("CanvasCatcherPlugin", "Canvas.drawBitmap(Bitmap, Rect, Rect, Paint) captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // Hook Canvas.drawBitmap(Bitmap, Rect, RectF, Paint)
        XposedHelpers2.findAndHookMethod(
                Canvas.class,
                "drawBitmap",
                Bitmap.class,
                Rect.class,
                RectF.class,
                Paint.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("CanvasCatcherPlugin", "Canvas.drawBitmap(Bitmap, Rect, RectF, Paint) captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // Hook Canvas.drawBitmap(Bitmap, Matrix, Paint)
        XposedHelpers2.findAndHookMethod(
                Canvas.class,
                "drawBitmap",
                Bitmap.class,
                Matrix.class,
                Paint.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (bitmap != null) {
                            LogUtil.d("CanvasCatcherPlugin", "Canvas.drawBitmap(Bitmap, Matrix, Paint) captured");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );
    }
}
