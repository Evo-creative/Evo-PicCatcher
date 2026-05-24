package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Lu
 * @date 2024/11/04
 * @description Compose 拦截插件。通过 Hook AndroidComposeView 或相关绘制逻辑获取 Compose 图片。
 * 在 Compose 中，常用的图片加载库如 Coil 已经有对应的 CoilCatcherPlugin。
 * 这里主要针对 Compose 自身的绘制逻辑或特定的 Image 组件进行拦截。
 */
public class ComposeCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        LogUtil.d("ComposeCatcherPlugin", "handleHook");

        // Compose 的根 View 通常是 androidx.compose.ui.platform.AndroidComposeView
        Class<?> androidComposeViewClass = ClazzN.from("androidx.compose.ui.platform.AndroidComposeView", loadPackageParam.classLoader);
        if (androidComposeViewClass != null) {
            LogUtil.d("ComposeCatcherPlugin", "Found AndroidComposeView");
            // 可以在这里做一些全局的 Compose 层级分析
        }

        // Hook androidx.compose.ui.graphics.AndroidCanvas.drawBitmap (如果存在)
        // 或者 Hook Compose 内部 Painter 的绘制
        Class<?> androidCanvasClass = ClazzN.from("androidx.compose.ui.graphics.AndroidCanvas", loadPackageParam.classLoader);
        if (androidCanvasClass != null) {
            XposedHelpers2.hookAllMethods(androidCanvasClass, "drawImage-impl", new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ModuleConfig.getInstance().isCatchComposePic()) {
                        return;
                    }
                    // 第一个参数通常是 ImageBitmap
                    Object imageBitmap = param.args[0];
                    if (imageBitmap != null) {
                        LogUtil.d("ComposeCatcherPlugin", "drawImage-impl captured");
                        // 尝试从 ImageBitmap 中提取 Bitmap
                        // ImageBitmap 是一个接口，Android 上的实现通常持有 android.graphics.Bitmap
                        try {
                            // androidx.compose.ui.graphics.AndroidImageBitmap
                            Bitmap bitmap = (Bitmap) XposedHelpers2.callMethod(imageBitmap, "getBitmap");
                            if (bitmap != null) {
                                PicExportManager.getInstance().exportBitmap(bitmap);
                            }
                        } catch (Throwable e) {
                            LogUtil.w("ComposeCatcherPlugin", "Failed to get bitmap from ImageBitmap", e);
                        }
                    }
                }
            });
        }
    }
}
