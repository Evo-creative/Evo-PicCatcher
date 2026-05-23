package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.util.PicUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * RenderNode 拦截插件
 * 针对硬件加速渲染流程，拦截 RecordingCanvas 的绘制行为
 */
public class RenderNodeCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        // RecordingCanvas 是 RenderNode.beginRecording 返回的类，在 Android 10+ 中广泛使用
        // 它的 drawBitmap 通常是 native 实现，直接 hook Canvas 可能拦截不到
        Class<?> recordingCanvasClass = XposedHelpers2.findClassIfExists("android.graphics.RecordingCanvas", lpparam.classLoader);
        if (recordingCanvasClass == null) {
            recordingCanvasClass = XposedHelpers2.findClassIfExists("android.view.DisplayListCanvas", lpparam.classLoader);
        }

        if (recordingCanvasClass != null) {
            LogUtil.d("RenderNodeCatcher", "Hooking " + recordingCanvasClass.getName());
            hookCanvasDrawMethods(recordingCanvasClass);
        }
    }

    private void hookCanvasDrawMethods(Class<?> clazz) {
        // 拦截多种 drawBitmap 重载
        XposedHelpers2.findAndHookMethod(clazz, "drawBitmap", Bitmap.class, float.class, float.class, Paint.class, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchRenderNodePic()) {
                    return;
                }
                Bitmap bitmap = (Bitmap) param.args[0];
                if (bitmap != null) {
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });

        XposedHelpers2.findAndHookMethod(clazz, "drawBitmap", Bitmap.class, Rect.class, Rect.class, Paint.class, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchRenderNodePic()) {
                    return;
                }
                Bitmap bitmap = (Bitmap) param.args[0];
                if (bitmap != null) {
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });

        XposedHelpers2.findAndHookMethod(clazz, "drawBitmap", Bitmap.class, Matrix.class, Paint.class, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ModuleConfig.getInstance().isCatchRenderNodePic()) {
                    return;
                }
                Bitmap bitmap = (Bitmap) param.args[0];
                if (bitmap != null) {
                    PicExportManager.getInstance().exportBitmap(bitmap);
                }
            }
        });
    }
}
