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
 * @author Mingyueyixi
 * @date 2024/9/22 0:22
 * @description bitmap hook
 */
public class BitmapCatcherPlugin implements IPlugin {
    private boolean isEnabled = true;

    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        // 插件加载时同步一次开关状态，之后由 ModuleConfig 内部的异步刷新线程通过其单例更新
        // 但为了性能，我们在 Hook 回调里尽量直接访问，不再重复 getInstance
        
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
                        if (filePath == null) return;
                        PicExportManager.getInstance().exportBitmapFile(new File(filePath));
                    }
                });

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
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                }
        );

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
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                }
        );
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
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                }
        );
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
                        PicExportManager.getInstance().exportBitmap(bitmap);
                    }
                }
        );
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
                            // 直接导出原始字节，避免 Bitmap 压缩
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
    }


}
