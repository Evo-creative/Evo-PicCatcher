package com.pic.catcher.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import com.pic.catcher.config.ModuleConfig
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.evo.piccatcher.logprovider"
        
        @JvmField
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
        
        const val METHOD_LOG = "log"
        const val KEY_MSG = "msg"
        const val KEY_PKG = "pkg"

        private const val TAG = "LogProvider"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(): Boolean {
        Log.d(TAG, "LogProvider created")
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method == METHOD_LOG) {
            val msg = extras?.getString(KEY_MSG)
            val pkg = extras?.getString(KEY_PKG)
            if (msg != null) {
                writeLogToFile(pkg, msg)
            }
        }
        return Bundle().apply { putBoolean("result", true) }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        if (uri.path?.startsWith("/save_pic") == true) {
            val fileName = uri.getQueryParameter("name") ?: return null
            val subDir = uri.getQueryParameter("pkg") ?: "unknown"
            
            val baseDir = context?.getExternalFilesDir("Pictures") ?: return null
            val targetDir = File(baseDir, subDir)
            if (!targetDir.exists()) targetDir.mkdirs()
            
            val targetFile = File(targetDir, fileName)
            return ParcelFileDescriptor.open(
                targetFile,
                ParcelFileDescriptor.MODE_WRITE_ONLY or 
                ParcelFileDescriptor.MODE_CREATE or 
                ParcelFileDescriptor.MODE_TRUNCATE
            )
        }
        return super.openFile(uri, mode)
    }

    private fun writeLogToFile(pkg: String?, msg: String) {
        try {
            val logDir = context?.getExternalFilesDir(null) ?: context?.filesDir ?: return
            if (!logDir.exists()) logDir.mkdirs()
            val logFile = File(logDir, "logs.txt")
            
            // 写入前检查大小
            checkLogFileSize(logFile)

            PrintWriter(FileWriter(logFile, true)).use { out ->
                val time = dateFormat.format(Date())
                out.println("$time [$pkg] $msg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Log write failed", e)
        }
    }

    /**
     * 检查日志大小，如果超限则删除旧日志
     */
    private fun checkLogFileSize(file: File) {
        if (!file.exists()) return
        
        // 计算字节大小，保持小数精度
        val maxSizeBytes = (ModuleConfig.getInstance().maxLogSizeMiB * 1024 * 1024).toLong()
        if (file.length() <= maxSizeBytes) return

        Log.d(TAG, "Log file exceeds limit, cleaning up...")
        
        try {
            // 保留最后一半大小的内容
            val raf = RandomAccessFile(file, "rw")
            val length = raf.length()
            val keepBytes = maxSizeBytes / 2
            
            if (keepBytes > 0 && length > keepBytes) {
                val buffer = ByteArray(keepBytes.toInt())
                raf.seek(length - keepBytes)
                raf.readFully(buffer)
                
                // 重新写入文件
                raf.setLength(0)
                raf.write(buffer)
            }
            raf.close()
            
            Log.d(TAG, "Log file cleaned, new size: ${file.length()}")
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup log file failed", e)
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
