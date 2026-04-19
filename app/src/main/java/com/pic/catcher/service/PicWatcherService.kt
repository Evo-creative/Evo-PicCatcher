package com.pic.catcher.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.lu.magic.util.thread.AppExecutor
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.util.ShellUtil
import java.io.File
import java.util.*

/**
 * 自动搬运服务 - 宿主私有目录中转模式
 * 解决 EPERM 权限问题，并根据配置决定转移终点。
 */
class PicWatcherService : Service() {

    companion object {
        private const val TAG = "PicWatcher"
        private const val MODULE_PKG = "com.evo.piccatcher"
        private const val DATA_ROOT = "/sdcard/Android/data"
        private const val MODULE_PRIVATE_ROOT = "/sdcard/Android/data/com.evo.piccatcher/files/Pictures"
        private const val PUBLIC_ROOT = "/sdcard/Pictures/PicCatcher"
    }

    private val timer = Timer()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "PicWatcherService started (Private Cache Mode)")
        
        // 每 15 秒轮询一次全量收割
        timer.schedule(object : TimerTask() {
            override fun run() {
                scanAndHarvestAll()
            }
        }, 3000, 15000)
    }

    private fun scanAndHarvestAll() {
        if (!ShellUtil.isShizukuAvailable() || !ShellUtil.hasShizukuPermission()) return

        val dataDir = File(DATA_ROOT)
        val packages = dataDir.listFiles()?.filter { it.isDirectory } ?: return

        for (pkgDir in packages) {
            val pkgName = pkgDir.name
            if (pkgName == MODULE_PKG) continue

            // 宿主私有缓存路径：Android/data/[pkg]/cache/PicCatcher
            val cachePath = "$DATA_ROOT/$pkgName/cache/PicCatcher"
            val cacheDir = File(cachePath)
            
            if (cacheDir.exists() && cacheDir.isDirectory) {
                val files = cacheDir.listFiles()
                if (files != null && files.isNotEmpty()) {
                    harvestPackage(pkgName, cachePath)
                }
                
                // 停止运行清理逻辑：如果 App 没在运行，直接把目录端掉，不留痕迹
                if (!isPackageRunning(pkgName)) {
                    Log.d(TAG, "Cleaning up inactive cache for $pkgName")
                    ShellUtil.runCommand("rm -rf \"$cachePath\"")
                }
            }
        }
    }

    private fun harvestPackage(pkgName: String, cachePath: String) {
        // 根据配置决定搬运终点
        val isSaveToInternal = ModuleConfig.getInstance().isSaveToInternal
        val targetRoot = if (isSaveToInternal) MODULE_PRIVATE_ROOT else PUBLIC_ROOT
        val targetDir = "$targetRoot/$pkgName"

        // 执行强制搬运
        val cmd = "mkdir -p \"$targetDir\" && mv -f \"$cachePath\"/* \"$targetDir/\""
        val result = ShellUtil.runCommand(cmd)
        
        if (result.isSuccess) {
            Log.i(TAG, "Harvested from $pkgName to $targetRoot")
        }
    }

    private fun isPackageRunning(pkgName: String): Boolean {
        return try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses
            runningProcesses?.any { it.processName == pkgName } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 显式启动时立即触发一次扫描
        AppExecutor.io().execute { scanAndHarvestAll() }
        return START_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
