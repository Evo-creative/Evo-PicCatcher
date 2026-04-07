package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix;
import android.os.Build;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 增强型画布拦截器
 * 支持 BaseCanvas 拦截，覆盖 Android 10+ 的底层绘制路径
 */
public class CanvasCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        
        // Android 10+ 许多方法移到了 BaseCanvas
        Class<?> targetClass = Canvas.class;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                targetClass = Class.forName("android.graphics.BaseCanvas");
            }
        } catch (ClassNotFoundException ignored) {}

        // 核心：拦截所有 drawBitmap 重载
        XposedHelpers2.hookAllMethods(targetClass, "drawBitmap", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.args.length > 0 && param.args[0] instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) param.args[0];
                    if (isValid(bitmap)) {
                        LogUtil.d("CanvasCatcher", "Captured from " + param.method.getName());
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                }
            }
        });

        // 针对漫画 App 特有的 drawBitmapMesh (用于翻页特效或拉伸)
        XposedHelpers2.hookAllMethods(targetClass, "drawBitmapMesh", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Bitmap bitmap = (Bitmap) param.args[0];
                if (isValid(bitmap)) {
                    LogUtil.d("CanvasCatcher", "Captured from drawBitmapMesh");
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });
    }

    private boolean isValid(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 100 && bitmap.getHeight() > 100;
    }
}
