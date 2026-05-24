package com.pic.catcher.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File

class LogProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.pic.catcher.logprovider"
        
        @JvmField
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
        
        private const val TAG = "LogProvider"
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "LogProvider created")
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        when (method) {
            "move_pic" -> {
                val cachePath = extras?.getString("cache_path")
                val pkg = extras?.getString("pkg")
                val fileName = extras?.getString("name")
                if (cachePath != null && pkg != null && fileName != null) {
                    val success = movePicViaShell(cachePath, pkg, fileName)
                    return Bundle().apply { putBoolean("result", success) }
                }
            }
        }
        return Bundle().apply { putBoolean("result", true) }
    }

    private fun movePicViaShell(cachePath: String, pkg: String, fileName: String): Boolean {
        val baseDir = context?.getExternalFilesDir("Pictures") ?: return false
        val targetDir = File(baseDir, pkg)
        if (!targetDir.exists()) targetDir.mkdirs()

        val targetPath = File(targetDir, fileName).absolutePath

        val moveCmd = "mv -f \"$cachePath\" \"$targetPath\""
        val result = com.pic.catcher.util.RootUtil.runCommand(moveCmd)
        
        if (result.isSuccess) {
            Log.d(TAG, "Successfully moved file via shell: $fileName")
            return true
        } else {
            Log.e(TAG, "Shell move failed: ${result.stderr}, trying fallback...")
            return try {
                val cacheFile = File(cachePath)
                val targetFile = File(targetPath)
                if (cacheFile.exists()) {
                    if (cacheFile.renameTo(targetFile)) {
                        true
                    } else {
                        cacheFile.copyTo(targetFile, overwrite = true)
                        cacheFile.delete()
                        true
                    }
                } else false
            } catch (e: Exception) {
                Log.e(TAG, "All move methods failed for $fileName", e)
                false
            }
        }
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

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
