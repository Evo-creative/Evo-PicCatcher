package com.pic.catcher.plugin;

import android.content.Context;
import android.graphics.Movie;

import com.lu.lposed.api2.XC_MethodHook2;
import com.lu.lposed.api2.XposedHelpers2;
import com.lu.lposed.plugin.IPlugin;
import com.lu.magic.util.log.LogUtil;

import java.io.InputStream;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Mingyueyixi
 * @description 拦截原生 Movie 解码（解决旧式 GIF 表情包）
 */
public class MovieCatcherPlugin implements IPlugin {
    @Override
    public void handleHook(Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        // Hook Movie.decodeStream(InputStream)
        XposedHelpers2.findAndHookMethod(
                Movie.class,
                "decodeStream",
                InputStream.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        InputStream inputStream = (InputStream) param.args[0];
                        if (inputStream != null) {
                            LogUtil.d("MovieCatcherPlugin", "Captured from decodeStream");
                            PicExportManager.getInstance().exportUrlIfNeed(null); // 此处建议传入流处理，暂用 exportUrlIfNeed 的逻辑做演示
                        }
                    }
                }
        );

        // Hook Movie.decodeByteArray(byte[], int, int)
        XposedHelpers2.findAndHookMethod(
                Movie.class,
                "decodeByteArray",
                byte[].class,
                int.class,
                int.class,
                new XC_MethodHook2() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        byte[] data = (byte[]) param.args[0];
                        if (data != null) {
                            LogUtil.d("MovieCatcherPlugin", "Captured from decodeByteArray, size: " + data.length);
                            PicExportManager.getInstance().exportByteArray(data, "gif");
                        }
                    }
                }
        );
    }
}
