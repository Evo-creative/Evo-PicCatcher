package com.pic.catcher.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.lu.magic.util.AppUtil
import java.util.concurrent.ConcurrentHashMap

class LocalKVUtil {
    companion object {
        const val defaultTableName = "app"
        private val tableCache = ConcurrentHashMap<String, SharedPreferences>()

        @SuppressLint("WorldReadableFiles")
        @JvmStatic
        fun getTable(name: String): SharedPreferences {
            var table = tableCache[name]
            if (table != null) return table

            val packageName = "com.evo.piccatcher"
            val context = try { AppUtil.getContext() } catch (e: Exception) { null }
            
            table = if (context != null && packageName == context.packageName) {
                try {
                    @Suppress("DEPRECATION")
                    context.getSharedPreferences(name, Context.MODE_WORLD_READABLE)
                } catch (e: Exception) {
                    context.getSharedPreferences(name, Context.MODE_PRIVATE)
                }
            } else {
                // xposed hook
                LspUtil.getTable(name)
            }

            tableCache[name] = table
            return table
        }

        @JvmStatic
        fun getDefaultTable(): SharedPreferences {
            return getTable(defaultTableName)
        }
    }
}
