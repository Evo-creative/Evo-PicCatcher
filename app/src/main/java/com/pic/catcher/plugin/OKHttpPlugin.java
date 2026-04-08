package com.pic.catcher.plugin;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.AppUtil;
import com.lu.magic.util.log.LogUtil;
import com.pic.catcher.ClazzN;
import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.util.PicUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OKHttpPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        handleHookOkHttp3(context, loadPackageParam);
        handleHookAndroidOkHttp(context, loadPackageParam);
    }

    private void handleHookAndroidOkHttp(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> httpEngineClazz = ClazzN.from("com.android.okhttp.internal.http.HttpEngine");
        if (httpEngineClazz == null) return;

        XposedHelpers2.findAndHookMethod(
                httpEngineClazz,
                "readResponse",
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchNetPic()) return;
                        
                        try {
                            Object response = XposedHelpers2.getObjectField(param.thisObject, "userResponse");
                            if (response == null) return;

                            String contentType = (String) XposedHelpers2.callMethod(response, "header", "Content-Type");
                            if (TextUtils.isEmpty(contentType)) return;

                            String guessFileEx = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
                            if (!PicUtil.isPicSuffix(guessFileEx)) return;

                            Object body = XposedHelpers2.callMethod(response, "body");
                            if (body == null) return;

                            // 安全增强：对于 Android 系统内置的旧版 OkHttp，readByteArray 会彻底破坏流
                            // 且反射写回（buffer.write）在很多深度定制系统上无效，会导致宿主闪退。
                            // 策略调整：如果无法安全获取数据，则跳过网络层拦截，由下游的 BitmapFactory/Glide 拦截器补齐。
                            // 这样既保证了不闪退，也能抓到图。
                            
                            /* 
                            // 风险代码屏蔽：
                            Object bufferSource = XposedHelpers2.callMethod(body, "source");
                            Object bytes = XposedHelpers2.callMethod(bufferSource, "readByteArray"); 
                            ... 
                            */
                            
                        } catch (Throwable t) {
                            LogUtil.w("AndroidOkHttp hook error", t);
                        }
                    }
                }
        );
    }

    private void handleHookOkHttp3(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        ClassLoader clazzLoader = AppUtil.getClassLoader();
        Class<?> realCallClazz = ClazzN.from("okhttp3.RealCall", clazzLoader);
        if (realCallClazz == null) return;

        XposedHelpers2.findAndHookMethod(
                realCallClazz,
                "getResponseWithInterceptorChain",
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchNetPic()) return;
                        
                        Object response = param.getResult();
                        if (response == null) return;

                        try {
                            String contentType = (String) XposedHelpers2.callMethod(response, "header", "Content-Type");
                            if (TextUtils.isEmpty(contentType)) return;

                            String guessFileEx = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
                            if (!PicUtil.isPicSuffix(guessFileEx)) return;

                            // OkHttp3 使用 peekBody 是极其安全的，它通过副本读取，不影响原始 Response 内容
                            // 限制 10MB 以内，防止大文件导致 OOM
                            Object response2 = XposedHelpers2.callMethod(response, "peekBody", 1024 * 1024 * 10L);
                            if (response2 != null) {
                                Object bytes = XposedHelpers2.callMethod(response2, "bytes");
                                if (bytes instanceof byte[]) {
                                    PicExportManager.getInstance().exportByteArray((byte[]) bytes, guessFileEx);
                                }
                            }
                        } catch (Throwable t) {
                            LogUtil.w("OkHttp3 hook error", t);
                        }
                    }
                }
        );
    }
}
