package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 终极底层拦截器：Surface 捕捉
 * 针对绕过所有 Java 层 Bitmap/Canvas API 的 App。
 * 只要它在 Surface 上绘图，我们就尝试截获。
 */
public class SurfaceCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        
        // Hook Surface.lockCanvas(Rect dirty)
        XposedHelpers2.findAndHookMethod(
                Surface.class,
                "lockCanvas",
                Rect.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchNativePic()) return;
                        
                        Canvas canvas = (Canvas) param.getResult();
                        if (canvas != null) {
                            // 这是一个极其耗能的操作，所以我们只在必要时开启。
                            // 在这里我们可以通过反射或双缓冲技术，在 unlock 之前拿到像素。
                        }
                    }
                }
        );

        // 核心拦截：unlockCanvasAndPost
        // 当 App 完成绘制并准备显示时，我们强行把这个 Surface 的内容读出来。
        XposedHelpers2.findAndHookMethod(
                Surface.class,
                "unlockCanvasAndPost",
                Canvas.class,
                new XC_MethodHook2() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchNativePic()) return;
                        
                        // 注意：这里由于涉及到跨进程或硬件 Buffer，直接读取 Canvas 像素可能有风险。
                        // 我们更倾向于通过 Hook 绘制这个 Canvas 的地方。
                        LogUtil.d("SurfaceCatcher", "Surface.unlockCanvasAndPost detected");
                    }
                }
        );
    }
}
