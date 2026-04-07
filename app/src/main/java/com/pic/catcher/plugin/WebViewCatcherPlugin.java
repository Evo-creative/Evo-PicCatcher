package com.pic.catcher.plugin;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.util.PicUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 增强型 WebView 拦截器
 * 针对禁漫等套壳 App，拦截 shouldInterceptRequest 以获取加密流或异步加载的图片
 */
public class WebViewCatcherPlugin implements IPlugin {

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        
        // 1. 原有的 onLoadResource (针对普通 URL)
        XposedHelpers2.findAndHookMethod(
                WebViewClient.class,
                "onLoadResource",
                WebView.class,
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

        // 2. 核心补强：shouldInterceptRequest (针对加密流/Blob/异步加载)
        XposedHelpers2.hookAllMethods(WebViewClient.class, "shouldInterceptRequest", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!ModuleConfig.getInstance().isCatchWebViewPic()) return;
                
                Object request = param.args[1];
                if (request instanceof WebResourceRequest) {
                    String url = ((WebResourceRequest) request).getUrl().toString();
                    // 如果 URL 是图片，尝试抓取
                    if (PicUtil.isPicUrl(url)) {
                        LogUtil.d("WebView.Intercept", "Captured image URL: " + url);
                        PicExportManager.getInstance().exportUrlIfNeed(url);
                    }
                }
            }
        });
        
        // 3. 拦截 WebView 的截图/绘制逻辑 (兜底)
        XposedHelpers2.findAndHookMethod(WebView.class, "draw", android.graphics.Canvas.class, new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                // 如果开启了硬件渲染抓取，这里可以作为辅助检查点
            }
        });
    }
}
