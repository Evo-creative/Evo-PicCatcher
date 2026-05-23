package com.pic.catcher.plugin;

import android.content.Context;
import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.pic.catcher.config.ModuleConfig;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Mingyueyixi
 * @date 2024/11/22
 * @description 底层文件流拦截插件 (FileCatcher)
 */
public class FileCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam lpparam) {
        // 拦截 FileInputStream 的构造函数或 open 方法，识别是否是图片文件
        XposedHelpers2.findAndHookConstructor(
                FileInputStream.class,
                File.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchFilePic()) return;
                        File file = (File) param.args[0];
                        if (file != null && isImageFile(file)) {
                            PicExportManager.getInstance().exportBitmapFile(file);
                        }
                    }
                }
        );

        XposedHelpers2.findAndHookConstructor(
                FileInputStream.class,
                String.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchFilePic()) return;
                        String path = (String) param.args[0];
                        if (path != null) {
                            File file = new File(path);
                            if (isImageFile(file)) {
                                PicExportManager.getInstance().exportBitmapFile(file);
                            }
                        }
                    }
                }
        );

        // 也可以考虑拦截 FileOutputStream，捕获 App 正在写入的图片
        XposedHelpers2.findAndHookConstructor(
                FileOutputStream.class,
                File.class,
                boolean.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!ModuleConfig.getInstance().isCatchFilePic()) return;
                        File file = (File) param.args[0];
                        if (file != null && isImageFile(file)) {
                            // 这里可能需要等到 close 时再处理，或者直接记录
                            PicExportManager.getInstance().exportBitmapFile(file);
                        }
                    }
                }
        );
    }

    private boolean isImageFile(File file) {
        if (!file.exists() || !file.isFile()) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") 
                || name.endsWith(".webp") || name.endsWith(".gif") || name.endsWith(".bmp");
    }
}
