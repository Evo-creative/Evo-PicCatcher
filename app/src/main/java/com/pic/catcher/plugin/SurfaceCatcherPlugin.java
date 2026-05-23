package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 深入 Hook Surface 与 SurfaceControl (Android 11+ BLASTBufferQueue)
 * 解决卡死：完全移除同步 reload 逻辑，仅依赖 ModuleConfig 的内存缓存。
 */
public class SurfaceCatcherPlugin implements IPlugin {

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader classLoader = loadPackageParam.classLoader;
        final ModuleConfig config = ModuleConfig.getInstance();

        // 1. Hook Surface.lockCanvas
        XposedHelpers2.findAndHookMethod(Surface.class, "lockCanvas", Rect.class, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!config.isCatchSurfacePic()) return;
                Canvas canvas = (Canvas) param.getResult();
                if (canvas != null) {
                    // TODO: 捕获逻辑
                }
            }
        });

        // 2. Hook Surface.unlockCanvasAndPost
        XposedHelpers2.findAndHookMethod(Surface.class, "unlockCanvasAndPost", Canvas.class, new XC_MethodHook2() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!config.isCatchSurfacePic()) return;
                // TODO: 提交前捕获
            }
        });

        // 3. Android 11+ BLASTBufferQueue Hook (核心现代渲染路径)
        try {
            Class<?> blastClass = XposedHelpers2.findClass("android.view.BLASTBufferQueue", classLoader);
            XposedHelpers2.findAndHookMethod(blastClass, "update", SurfaceControl.class, int.class, int.class, int.class, new XC_MethodHook2() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!config.isCatchSurfacePic()) return;
                    // BLASTBufferQueue 更新时拦截
                }
            });
        } catch (Throwable ignored) {}

        // 4. SurfaceControl.Transaction.setBuffer (拦截底层图形缓冲区提交)
        try {
            XposedHelpers2.findAndHookMethod(SurfaceControl.Transaction.class, "setBuffer", 
                SurfaceControl.class, android.hardware.HardwareBuffer.class, XposedHelpers2.findClass("android.view.SurfaceControl$Fence", classLoader),
                new XC_MethodHook2() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!config.isCatchSurfacePic()) return;
                    // TODO: 拦截 HardwareBuffer
                }
            });
        } catch (Throwable ignored) {}
    }
}
