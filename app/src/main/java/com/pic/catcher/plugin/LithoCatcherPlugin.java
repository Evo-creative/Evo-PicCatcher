package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.pic.catcher.ClazzN;
import com.pic.catcher.config.ModuleConfig;
import com.pic.catcher.util.PicUtil;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Facebook Litho / Yoga 拦截插件
 * Litho 广泛用于 Facebook, Instagram, 以及国内一些 App (如某音, 某宝)
 */
public class LithoCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        final ModuleConfig config = ModuleConfig.getInstance();
        ClassLoader classLoader = lpparam.classLoader;

        // 1. 拦截 MountItem.mountContent (当 Litho 渲染内容到宿主时)
        Class<?> mountItemClass = ClazzN.from("com.facebook.litho.MountItem", classLoader);
        if (mountItemClass != null) {
            XposedHelpers2.findAndHookMethod(mountItemClass, "mountContent", Context.class, new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!config.isCatchLithoPic()) return;
                    Object content = XposedHelpers2.callMethod(param.thisObject, "getContent");
                    if (content instanceof Drawable) {
                        handleDrawable((Drawable) content);
                    }
                }
            });
        }

        // 2. 拦截 DebugComponent (用于更精确的组件级图片发现，如果开启了调试功能或混淆较弱)
        Class<?> matrixDrawableClass = ClazzN.from("com.facebook.litho.MatrixDrawable", classLoader);
        if (matrixDrawableClass != null) {
            XposedHelpers2.findAndHookMethod(matrixDrawableClass, "mount", Drawable.class, new XC_MethodHook2() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!config.isCatchLithoPic()) return;
                    handleDrawable((Drawable) param.args[0]);
                }
            });
        }
        
        // 3. Yoga 布局节点拦截 (可选，Yoga 主要是坐标计算，但有些实现会直接在布局层绑定资源)
        // 此处暂不做深度 Yoga 拦截，因为 Litho 层已经能拿到最终渲染物。
    }

    private void handleDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            PicExportManager.getInstance().exportBitmap(((BitmapDrawable) drawable).getBitmap());
        } else {
            // 对于非 BitmapDrawable，尝试转换为 Bitmap
            Bitmap bitmap = PicUtil.drawableToBitmap(drawable);
            if (bitmap != null) {
                PicExportManager.getInstance().exportBitmap(bitmap);
            }
        }
    }
}
