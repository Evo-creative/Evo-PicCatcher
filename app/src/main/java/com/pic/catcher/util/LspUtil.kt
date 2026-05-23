package com.pic.catcher.util

import android.content.SharedPreferences
import com.pic.catcher.BuildConfig
import de.robv.android.xposed.XSharedPreferences

/**
 * xposed api
 */
class LspUtil {

    companion object {
        private val prefCache = mutableMapOf<String, XSharedPreferences>()

        @JvmStatic
        fun getTable(table: String): SharedPreferences {
            synchronized(prefCache) {
                return prefCache.getOrPut(table) {
                    val pref = XSharedPreferences(BuildConfig.APPLICATION_ID, table)
                    pref.makeWorldReadable()
                    pref
                }
            }
        }
    }
}
