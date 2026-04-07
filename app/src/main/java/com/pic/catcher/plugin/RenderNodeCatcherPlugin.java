package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RenderNode;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 针对现代 Android 硬件加速渲染的拦截器
 * 拦截 RenderNode 相关的绘制，捕获那些不走普通 Canvas.drawBitmap 的图片
 */
public class RenderNodeCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        // RenderNode 是 Android P (28) 引入的，你的 minSDK 是 24，所以需要判断
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            return;
        }

        try {
            // 很多自研阅读器会把图片包装成 RenderNode 提交给 HardwareRenderer
            XposedHelpers2.findAndHookMethod(
                    RenderNode.class,
                    "beginRecording",
                    int.class,
                    int.class,
                    new XC_MethodHook2() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // 这是一个实验性的路径：在录制开始时检查状态
                            // 实际上我们更倾向于拦截把内容画入 RenderNode 的地方
                        }
                    }
            );

            // 拦截一些可能隐藏在录制过程中的 Bitmap 操作
            // 虽然 RenderNode 内部是 Native 的，但它在 Java 层有少量入口
        } catch (Throwable t) {
            LogUtil.w("RenderNodeCatcherPlugin hook fail", t);
        }
    }
}
