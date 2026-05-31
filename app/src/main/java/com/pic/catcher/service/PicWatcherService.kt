package com.pic.catcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.util.RootUtil

/**
 * 自动搬运服务 - 优化版
 * 1. 运行在独立后台线程，杜绝界面卡死
 * 2. 脚本化批量搬运，极低 CPU 占用
 */
class PicWatcherService : Service() {

    companion object {
        private const val TAG = "PicWatcher"
        private const val CHANNEL_ID = "pic_watcher_service"
        private const val MODULE_PRIVATE_ROOT = "/sdcard/Android/data/com.evo.piccatcher/files/Pictures"
        private const val PUBLIC_ROOT = "/sdcard/Pictures/PicCatcher"
    }

    private lateinit var serviceHandler: Handler
    private lateinit var handlerThread: HandlerThread

    private val harvestRunnable = object : Runnable {
        override fun run() {
            try {
                // 确保 Root 会话已建立，且仅在 Root 权限下运行脚本
                if (RootUtil.hasRootPermission()) {
                    performBatchHarvest()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Harvest error", e)
            } finally {
                // 将扫描频率设置为 10 秒，更及时地搬运图片
                serviceHandler.postDelayed(this, 10000)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        
        // 关键修复：创建专门的后台线程，绝不在主线程运行 Shell
        handlerThread = HandlerThread("PicWatcherThread")
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        
        serviceHandler.postDelayed(harvestRunnable, 5000)
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "PicCatcher Monitor", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            
            val notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("PicCatcher is active")
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .build()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(101, notification)
            }
        }
    }

    /**
     * 使用单条复杂的 Shell 脚本完成批量检测和搬运
     */
    private fun performBatchHarvest() {
        val isInternal = ModuleConfig.getInstance().isSaveToInternal
        val targetRoot = if (isInternal) MODULE_PRIVATE_ROOT else PUBLIC_ROOT

        // 核心脚本优化：
        // 1. 扫描 Android/data 和 /data/data (某些受保护 App)
        // 2. 只有发现文件才进行搬运，搬运成功后才删除源文件
        // 3. 增强：添加对 /storage/emulated/0 的兼容性
        val script = """
            TARGET_BASE="$targetRoot"
            mkdir -p "${"$"}TARGET_BASE"
            
            # 定义扫描函数
            scan_and_move() {
                local base_dir="${"$"}1"
                # 兼容不同挂载点路径
                [ -d "${"$"}base_dir" ] || return
                
                for dir in ${"$"}base_dir/*/cache/PicCatcher; do
                    [ -d "${"$"}dir" ] || continue
                    files=$(ls -A "${"$"}dir" 2>/dev/null)
                    if [ -n "${"$"}files" ]; then
                        pkg=$(echo "${"$"}dir" | awk -F'/' '{print $(NF-2)}')
                        dest="${"$"}TARGET_BASE/${"$"}pkg"
                        mkdir -p "${"$"}dest"
                        # 使用 cp -Rf 配合 rm -rf 实现跨挂载点安全转移
                        if cp -Rf "${"$"}dir"/* "${"$"}dest/" 2>/dev/null; then
                            rm -rf "${"$"}dir"/*
                            echo "Harvested from ${"$"}pkg"
                        fi
                    fi
                done
            }

            scan_and_move "/sdcard/Android/data"
            scan_and_move "/storage/emulated/0/Android/data"
            scan_and_move "/data/data"
        """.trimIndent()

        val result = RootUtil.runCommand(script)
        if (result.stdout.isNotEmpty()) {
            Log.d(TAG, result.stdout)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        handlerThread.quitSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
