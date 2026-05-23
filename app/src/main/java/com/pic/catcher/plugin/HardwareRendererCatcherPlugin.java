package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.HardwareRenderer;
import android.graphics.RenderNode;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.pic.catcher.config.ModuleConfig;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 拦截 HardwareRenderer (RenderThread)
 * 解决卡死：渲染线程直接访问内存缓存，后台线程负责同步配置。
 */
public class HardwareRendererCatcherPlugin implements IPlugin {

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader classLoader = loadPackageParam.classLoader;
        try {
            XposedHelpers2.findAndHookMethod(HardwareRenderer.class, "setContentRoot", RenderNode.class, new XC_MethodHook2() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ModuleConfig.getInstance().isCatchHardwareRendererPic()) return;
                    // RenderThread 开始渲染根节点
                }
            });

            // 拦截同步帧的方法，这是渲染流水线的关键点
            XposedHelpers2.findAndHookMethod(HardwareRenderer.class, "syncAndDrawFrame", 
                XposedHelpers2.findClass("android.graphics.FrameInfo", classLoader), new XC_MethodHook2() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ModuleConfig.getInstance().isCatchHardwareRendererPic()) return;
                    // TODO: 在帧同步时分析 RenderNode 树
                }
            });

        } catch (Throwable ignored) {
            // 某些低版本或定制 ROM 可能没有这些私有方法
        }
    }
}
