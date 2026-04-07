package com.pic.catcher.plugin;

import android.content.Context;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;
import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.util.PicUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 增强型 X5WebView 拦截器 (3.6.7)
 * 针对国内 App 常用的腾讯 X5 内核进行深度拦截
 */
public class X5WebViewCatcherPlugin implements IPlugin {

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> webViewClientClazz = ClazzN.from("com.tencent.smtt.sdk.WebViewClient");
        Class<?> webViewClazz = ClazzN.from("com.tencent.smtt.sdk.WebView");
        Class<?> webResourceRequestClazz = ClazzN.from("com.tencent.smtt.export.external.interfaces.WebResourceRequest");

        if (webViewClientClazz == null || webViewClazz == null) {
            return;
        }

        // 1. Hook onLoadResource
        XposedHelpers2.findAndHookMethod(
                webViewClientClazz,
                "onLoadResource",
                webViewClazz,
                String.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!ModuleConfig.getInstance().isCatchWebViewPic()) return;
                        String url = (String) param.args[1];
                        PicExportManager.getInstance().exportUrlIfNeed(url);
                    }
                }
        );

        // 2. 核心补强：Hook shouldInterceptRequest (针对 X5 内核的异步加载和加密流)
        if (webResourceRequestClazz != null) {
            XposedHelpers2.findAndHookMethod(
                    webViewClientClazz,
                    "shouldInterceptRequest",
                    webViewClazz,
                    webResourceRequestClazz,
                    new XC_MethodHook2() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (!ModuleConfig.getInstance().isCatchWebViewPic()) return;
                            
                            Object request = param.args[1];
                            try {
                                // X5 的 WebResourceRequest 通过反射或 getUrl() 获取地址
                                Object uri = XposedHelpers2.callMethod(request, "getUrl");
                                if (uri != null) {
                                    String url = uri.toString();
                                    if (PicUtil.isPicUrl(url)) {
                                        LogUtil.d("X5WebView.Intercept", "Captured URL: " + url);
                                        PicExportManager.getInstance().exportUrlIfNeed(url);
                                    }
                                }
                            } catch (Throwable t) {
                                // ignore
                            }
                        }
                    }
            );
        }
    }
}
