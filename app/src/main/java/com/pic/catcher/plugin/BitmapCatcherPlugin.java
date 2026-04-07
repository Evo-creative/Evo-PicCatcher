package com.pic.catcher.plugin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.util.TypedValue;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;

import java.io.File;
import java.io.InputStream;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Mingyueyixi
 * @description 增强型位图拦截器，支持大图分块解码器 (BitmapRegionDecoder)
 */
public class BitmapCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        
        // --- 1. 原有的 BitmapFactory Hook ---
        XposedHelpers2.findAndHookMethod(BitmapFactory.class, "decodeFile", String.class, BitmapFactory.Options.class, new XC_MethodHook2() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String filePath = (String) param.args[0];
                PicExportManager.getInstance().exportBitmapFile(new File(filePath));
            }
        });

        XposedHelpers2.hookAllMethods(BitmapFactory.class, "decodeStream", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Bitmap bitmap = (Bitmap) param.getResult();
                PicExportManager.getInstance().exportBitmap(bitmap);
            }
        });

        XposedHelpers2.hookAllMethods(BitmapFactory.class, "decodeByteArray", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Bitmap bitmap = (Bitmap) param.getResult();
                PicExportManager.getInstance().exportBitmap(bitmap);
            }
        });

        // --- 2. 新增：拦截 BitmapRegionDecoder (针对漫画大图分块加载) ---
        Class<?> regionDecoderClazz = BitmapRegionDecoder.class;
        XposedHelpers2.hookAllMethods(regionDecoderClazz, "decodeRegion", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Bitmap bitmap = (Bitmap) param.getResult();
                // 漫画分块通常很大，在这里抓取能拿到拼接前的原片
                PicExportManager.getInstance().exportBitmap(bitmap);
            }
        });

        // 兜底拦截 decodeResource 等其他方法
        XposedHelpers2.hookAllMethods(BitmapFactory.class, "decodeResource", new XC_MethodHook2() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Bitmap bitmap = (Bitmap) param.getResult();
                PicExportManager.getInstance().exportBitmap(bitmap);
            }
        });
    }
}
