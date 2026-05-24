package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.hardware.HardwareBuffer;
import android.os.Build;

import com.lu.magic.util.log.LogUtil;
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

        // 4. SurfaceControl.Transaction.setBuffer (核心拦截点)
        // 这是 Android 10+ 拦截图形流的最核心、最高效位置
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                XC_MethodHook2 setBufferHook = new XC_MethodHook2() {
                    private long lastCaptureTime = 0;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!config.isCatchSurfacePic()) return;

                        // 针对 120Hz/144Hz 设备优化：
                        // 1. 采样率限制：150ms 间隔 (约 6.6 fps)，足以捕获关键画面且不影响渲染流畅度
                        long now = System.currentTimeMillis();
                        if (now - lastCaptureTime < 150) return;

                        Object bufferObj = param.args[1];
                        if (bufferObj instanceof HardwareBuffer) {
                            HardwareBuffer buffer = (HardwareBuffer) bufferObj;
                            // 2. 尺寸过滤：排除非内容级 Buffer (如 1x1 纯色、小图标)
                            if (buffer.getWidth() > 200 && buffer.getHeight() > 200) {
                                lastCaptureTime = now;
                                // 3. 异步处理：wrapHardwareBuffer 是轻量级引用，不产生像素拷贝
                                try {
                                    Bitmap bitmap = Bitmap.wrapHardwareBuffer(buffer, ColorSpace.get(ColorSpace.Named.SRGB));
                                    if (bitmap != null) {
                                        PicExportManager.getInstance().exportBitmap(bitmap);
                                    }
                                } catch (Exception e) {
                                    LogUtil.e("wrapHardwareBuffer failed", e);
                                }
                            }
                        }
                    }
                };

                // Hook 不同版本的 setBuffer 重载以兼容各厂商及 Android 版本
                XposedHelpers2.findAndHookMethod(SurfaceControl.Transaction.class, "setBuffer",
                        SurfaceControl.class, HardwareBuffer.class, setBufferHook);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 部分机型使用带 Fence 的重载
                    try {
                        XposedHelpers2.findAndHookMethod(SurfaceControl.Transaction.class, "setBuffer",
                                SurfaceControl.class, HardwareBuffer.class, "android.hardware.SyncFence", setBufferHook);
                    } catch (Throwable ignored) {
                        try {
                            XposedHelpers2.findAndHookMethod(SurfaceControl.Transaction.class, "setBuffer",
                                    SurfaceControl.class, HardwareBuffer.class, Object.class, setBufferHook);
                        } catch (Throwable ignored2) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
