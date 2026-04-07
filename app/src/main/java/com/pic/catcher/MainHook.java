package com.pic.catcher;

import android.app.Application;
import android.content.Context;
import android.os.Process;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.PluginProviders;
import com.lu.lposed.plugin.PluginRegistry;
import com.lu.magic.util.AppUtil;
import com.lu.magic.util.log.LogUtil;
import com.lu.magic.util.log.SimpleLogger;
import com.pic.catcher.plugin.BitmapCatcherPlugin;
import com.pic.catcher.plugin.CanvasCatcherPlugin;
import com.pic.catcher.plugin.CoilCatcherPlugin;
import com.pic.catcher.plugin.DrawableCatcherPlugin;
import com.pic.catcher.plugin.FileCatcherPlugin;
import com.pic.catcher.plugin.FrescoCatcherPlugin;
import com.pic.catcher.plugin.GlideCatcherPlugin;
import com.pic.catcher.plugin.ImageDecoderCatcherPlugin;
import com.pic.catcher.plugin.ImageViewCatcherPlugin;
import com.pic.catcher.plugin.MovieCatcherPlugin;
import com.pic.catcher.plugin.NativeBitmapCatcherPlugin;
import com.pic.catcher.plugin.OKHttpPlugin;
import com.pic.catcher.plugin.RenderNodeCatcherPlugin;
import com.pic.catcher.plugin.SurfaceCatcherPlugin;
import com.pic.catcher.plugin.WebViewCatcherPlugin;
import com.pic.catcher.plugin.X5WebViewCatcherPlugin;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static String MODULE_PATH = "";
    private volatile boolean hasInit = false;
    private List<XC_MethodHook.Unhook> initUnHookList = new ArrayList<>();
    private boolean isHookEntryHandle = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (isHookEntryHandle) return;
        isHookEntryHandle = true;

        LogUtil.setLogger(new SimpleLogger() {
            @Override
            public void onLog(int level, @NonNull Object[] objects) {
                if (BuildConfig.DEBUG) {
                    super.onLog(level, objects);
                } else if (level > 1) {
                    String msgText = buildLogText(objects);
                    XposedHelpers2.log(TAG + " " + msgText);
                }
            }
        });
        
        LogUtil.i("Process attached: ", lpparam.processName, Process.myPid());

        XposedHelpers2.findAndHookMethod(
                Application.class.getName(),
                lpparam.classLoader,
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        initPlugin((Context) param.thisObject, lpparam);
                    }
                }
        );
    }

    private synchronized void initPlugin(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        if (context == null || hasInit) return;
        hasInit = true;
        AppUtil.attachContext(context);

        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            initSelfPlugins(context, lpparam);
        } else {
            initTargetPlugins(context, lpparam);
        }

        for (XC_MethodHook.Unhook unhook : initUnHookList) {
            if (unhook != null) unhook.unhook();
        }
    }

    private void initSelfPlugins(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        SelfHook.getInstance().handleHook(context, lpparam);
    }

    private void initTargetPlugins(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        PluginRegistry.register(
                BitmapCatcherPlugin.class,
                GlideCatcherPlugin.class,
                WebViewCatcherPlugin.class,
                X5WebViewCatcherPlugin.class,
                OKHttpPlugin.class,
                FrescoCatcherPlugin.class,
                DrawableCatcherPlugin.class,
                CanvasCatcherPlugin.class,
                MovieCatcherPlugin.class,
                ImageViewCatcherPlugin.class,
                ImageDecoderCatcherPlugin.class,
                CoilCatcherPlugin.class,
                NativeBitmapCatcherPlugin.class,
                RenderNodeCatcherPlugin.class,
                SurfaceCatcherPlugin.class,
                FileCatcherPlugin.class
        ).handleHooks(context, lpparam);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {}
}
