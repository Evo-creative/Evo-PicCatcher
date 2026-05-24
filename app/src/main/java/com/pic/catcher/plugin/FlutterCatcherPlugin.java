package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Surface;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Lu
 * @date 2024/11/04
 * @description Flutter 拦截插件。
 * Flutter 拥有独立的渲染引擎（Skia/Impeller），不走 Android 原生的 View 系统。
 * 本插件通过 Hook Flutter 引擎在 Java 层的多个数据交换点（JNI、镜像 View、渲染器、外接纹理）进行深度拦截。
 */
public class FlutterCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (!ModuleConfig.getInstance().isCatchFlutterPic()) {
            return;
        }
        LogUtil.d("FlutterCatcherPlugin", "Installing comprehensive Flutter hooks...");

        // 1. 拦截 FlutterJNI：这是最底层的 Java 接口，用于同步像素到 Native 纹理
        Class<?> flutterJniClass = ClazzN.from("io.flutter.embedding.engine.FlutterJNI", lpparam.classLoader);
        if (flutterJniClass != null) {
            try {
                XposedHelpers2.findAndHookMethod(flutterJniClass, "nativeUpdateJavaBitmap", 
                        Bitmap.class, long.class, new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[0];
                        if (isValid(bitmap)) {
                            LogUtil.d("FlutterCatcherPlugin", "Captured via FlutterJNI.nativeUpdateJavaBitmap");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                });
            } catch (Throwable ignored) {}
        }

        // 2. 拦截镜像视图层：FlutterImageView (现代) & FlutterView (旧版)
        Class<?> flutterImageViewClass = ClazzN.from("io.flutter.embedding.android.FlutterImageView", lpparam.classLoader);
        if (flutterImageViewClass != null) {
            XposedHelpers2.findAndHookMethod(flutterImageViewClass, "setImageBitmap", Bitmap.class, new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Bitmap bitmap = (Bitmap) param.args[0];
                    if (isValid(bitmap)) {
                        LogUtil.d("FlutterCatcherPlugin", "Captured via FlutterImageView.setImageBitmap");
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                }
            });
        }

        Class<?> legacyFlutterViewClass = ClazzN.from("io.flutter.view.FlutterView", lpparam.classLoader);
        if (legacyFlutterViewClass != null) {
            try {
                XposedHelpers2.findAndHookMethod(legacyFlutterViewClass, "getBitmap", new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.getResult();
                        if (isValid(bitmap)) {
                            LogUtil.d("FlutterCatcherPlugin", "Captured via legacy FlutterView.getBitmap");
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                });
            } catch (Throwable ignored) {}
        }

        // 3. 拦截外接纹理注册点 (TextureRegistry)
        // 许多 Flutter 图片/视频插件通过 SurfaceTexture 与 Native 引擎交互
        Class<?> surfaceEntryClass = ClazzN.from("io.flutter.embedding.engine.renderer.FlutterRenderer$SurfaceTextureRegistryEntry", lpparam.classLoader);
        if (surfaceEntryClass != null) {
            XposedHelpers2.hookAllMethods(surfaceEntryClass, "surfaceTexture", new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    LogUtil.d("FlutterCatcherPlugin", "Flutter TextureRegistry access detected");
                }
            });
        }
        
        LogUtil.d("FlutterCatcherPlugin", "Flutter hooks active.");
    }

    private boolean isValid(Bitmap bitmap) {
        // 过滤极小图标，保持与 NativeBitmapCatcher 逻辑一致
        return bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 20 && bitmap.getHeight() > 20;
    }
}
