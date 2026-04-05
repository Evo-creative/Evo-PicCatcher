package com.pic.catcher.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.lu.lposed.api2.XposedHelpers2
import com.pic.catcher.provider.LogProvider

/**
 * 统一日志工具类
 */
object XLog {
    private const val TAG = "PicCatcher"

    @JvmStatic
    fun i(context: Context?, msg: String) {
        val message = msg ?: "null"
        
        // 1. Logcat 输出
        Log.i(TAG, message)

        // 2. Xposed 日志输出 (LSPosed 管理器可见)
        try {
            XposedHelpers2.log("$TAG: $message")
        } catch (ignored: Throwable) {}

        // 3. 跨进程保存到本地文件 (插件 App 私有目录)
        if (context != null) {
            try {
                val extras = Bundle().apply {
                    putString(LogProvider.KEY_MSG, message)
                    putString(LogProvider.KEY_PKG, context.packageName)
                }
                // 使用 call 方式触发 Provider 写入文件
                context.contentResolver.call(
                    LogProvider.CONTENT_URI,
                    LogProvider.METHOD_LOG,
                    null,
                    extras
                )
            } catch (e: Exception) {
                // 如果失败（通常是 Android 11+ 可见性问题），在 Logcat 报错
                Log.e(TAG, "LogProvider transport failed: ${e.message}")
            }
        }
    }

    @JvmStatic
    fun e(context: Context?, msg: String, throwable: Throwable? = null) {
        val errorMsg = if (throwable != null) {
            "$msg \n${Log.getStackTraceString(throwable)}"
        } else {
            msg
        }
        i(context, "[ERROR] $errorMsg")
    }
}
