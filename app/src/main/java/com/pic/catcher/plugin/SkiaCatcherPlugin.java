package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Lu
 * @date 2024/11/04
 * @description Skia 拦截插件。
 * 针对使用 Skia 引擎自绘的场景，除了 Native 层的拦截外，在 Java 层尝试捕捉
 * 任何显式暴露的 Skia Canvas 操作。
 */
public class SkiaCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!ModuleConfig.getInstance().isCatchSkiaPic()) {
            return;
        }
        LogUtil.d("SkiaCatcherPlugin", "handleHook");

        // 现代 Android 系统中，Canvas 底层就是 Skia。
        // isCatchSkiaPic 在这里主要作为一个细化开关，用于开启更激进的底层绘制拦截。
        // 目前 Java 层主要依赖针对 Canvas 的扩展 Hook。
        
        // 尝试 Hook 一些特定于某些 Skia 封装库的类（如果存在）
        // 例如：某些混合开发框架可能会暴露 SkCanvas 的 Java 封装
    }
}
