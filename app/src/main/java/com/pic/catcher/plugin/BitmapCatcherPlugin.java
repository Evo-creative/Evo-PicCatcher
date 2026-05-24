package com.pic.catcher.plugin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.TypedValue;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.pic.catcher.config.ModuleConfig;

import java.io.File;
import java.io.InputStream;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * BitmapFactory 全局拦截插件
 * 覆盖所有主要的 decodeXXX 方法
 */
public class BitmapCatcherPlugin implements IPlugin {

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        
        // 1. 拦截 decodeFile
        XposedHelpers2.findAndHookMethod(
                BitmapFactory.class,
                "decodeFile",
                String.class,
                BitmapFactory.Options.class,
                new XC_MethodHook2() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchBitmapPic()) return;
                        String filePath = (String) param.args[0];
                        if (filePath != null) {
                            PicExportManager.getInstance().exportBitmapFile(new File(filePath));
                        }
                    }
                });

        // 2. 拦截 decodeStream
        XposedHelpers2.findAndHookMethod(
                BitmapFactory.class,
                "decodeStream",
                InputStream.class,
                Rect.class,
                BitmapFactory.Options.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchBitmapPic()) return;
                        Bitmap bitmap = (Bitmap) param.getResult();
                        if (bitmap != null) {
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // 3. 拦截 decodeResource
        XposedHelpers2.findAndHookMethod(
                BitmapFactory.class,
                "decodeResource",
                Resources.class,
                int.class,
                BitmapFactory.Options.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchBitmapPic()) return;
                        Bitmap bitmap = (Bitmap) param.getResult();
                        if (bitmap != null) {
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // 4. 拦截 decodeByteArray (关键：拦截原始数据)
        XposedHelpers2.findAndHookMethod(
                BitmapFactory.class,
                "decodeByteArray",
                byte[].class,
                int.class,
                int.class,
                BitmapFactory.Options.class,
                new XC_MethodHook2() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchBitmapPic()) return;
                        byte[] data = (byte[]) param.args[0];
                        int offset = (int) param.args[1];
                        int length = (int) param.args[2];
                        if (data != null && length > 0) {
                            byte[] actualData;
                            if (offset == 0 && length == data.length) {
                                actualData = data;
                            } else {
                                actualData = new byte[length];
                                System.arraycopy(data, offset, actualData, 0, length);
                            }
                            PicExportManager.getInstance().exportByteArray(actualData, null);
                        }
                    }
                }
        );

        // 5. 拦截 decodeFileDescriptor
        XposedHelpers2.findAndHookMethod(
                BitmapFactory.class,
                "decodeFileDescriptor",
                java.io.FileDescriptor.class,
                Rect.class,
                BitmapFactory.Options.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchBitmapPic()) return;
                        Bitmap bitmap = (Bitmap) param.getResult();
                        if (bitmap != null) {
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );

        // 6. 拦截 decodeResourceStream (较少用，但为了全面性添加)
        XposedHelpers2.findAndHookMethod(
                BitmapFactory.class,
                "decodeResourceStream",
                Resources.class,
                TypedValue.class,
                InputStream.class,
                Rect.class,
                BitmapFactory.Options.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchBitmapPic()) return;
                        Bitmap bitmap = (Bitmap) param.getResult();
                        if (bitmap != null) {
                            PicExportManager.getInstance().exportBitmap(bitmap);
                        }
                    }
                }
        );
    }
}
