package com.pic.catcher.config

import com.lu.magic.util.log.LogUtil
import com.pic.catcher.util.JSONX
import com.pic.catcher.util.LocalKVUtil
import com.pic.catcher.util.ext.toJSONObject
import org.json.JSONObject

/**
 * @author Lu
 * @date 2025/1/5 20:02
 * @description
 */
class ModuleConfig(var source: JSONObject) {
    var isCatchGlidePic: Boolean = true
        get() = JSONX.optBoolean(source, "isCatchGlidePic", true)
        set(value) {
            field = value
            source.put("isCatchGlidePic", value)
        }

    var isCatchWebViewPic: Boolean = true
        get() = JSONX.optBoolean(source, "isCatchWebViewPic", true)
        set(value) {
            field = value
            source.put("isCatchWebViewPic", value)
        }

    var isCatchNetPic: Boolean = true
        get() = JSONX.optBoolean(source, "isCatchNetPic", true)
        set(value) {
            field = value
            source.put("isCatchNetPic", value)
        }

    var isCatchDrawablePic: Boolean = true
        get() = JSONX.optBoolean(source, "isCatchDrawablePic", true)
        set(value) {
            field = value
            source.put("isCatchDrawablePic", value)
        }

    var minSpaceSize = 0
        get() = JSONX.optLong(source, "minSpaceSize", 0).toInt()
        set(value) {
            field = value
            source.put("minSpaceSize", value)
        }

    // 修改：日志最大限制改为 Double 以支持小数 (MiB)
    var maxLogSizeMiB = 2.0
        get() = JSONX.optDouble(source, "maxLogSizeMiB", 2.0)
        set(value) {
            field = value
            source.put("maxLogSizeMiB", value)
        }

    var isSaveToInternal: Boolean = false
        get() = JSONX.optBoolean(source, "isSaveToInternal", false)
        set(value) {
            field = value
            source.put("isSaveToInternal", value)
        }

    var picDefaultSaveFormat: String = "webp"
        get() = JSONX.optString(source, "picDefaultSaveFormat", "webp") ?: "webp"
        set(value) {
            field = value
            source.put("picDefaultSaveFormat", value)
        }

    var shellAuthType: String = ""
        get() = JSONX.optString(source, "shellAuthType", "") ?: ""
        set(value) {
            field = value
            source.put("shellAuthType", value)
        }

    var rootStatus: String = "UNKNOWN"
        get() = JSONX.optString(source, "rootStatus", "UNKNOWN") ?: "UNKNOWN"
        set(value) {
            field = value
            source.put("rootStatus", value)
        }

    var suManagerName: String = "Root"
        get() = JSONX.optString(source, "suManagerName", "Root") ?: "Root"
        set(value) {
            field = value
            source.put("suManagerName", value)
        }

    fun toJson(): String {
        return source.toString()
    }

    fun save() {
        save(this)
    }

    companion object {
        private var cachedInstance: ModuleConfig? = null

        private var lastReloadTime = 0L
        private const val RELOAD_INTERVAL = 10000L // 10秒重载一次配置，避免磁盘 IO 过频

        @JvmStatic
        fun getInstance(): ModuleConfig {
            val table = LocalKVUtil.getTable("module")
            
            // 安全检查：仅在 XSharedPreferences 且超过间隔时重载
            if (table.javaClass.name.contains("XSharedPreferences")) {
                val now = System.currentTimeMillis()
                if (now - lastReloadTime > RELOAD_INTERVAL) {
                    try {
                        table.javaClass.getMethod("reload").invoke(table)
                        lastReloadTime = now
                        cachedInstance = null // 清除缓存以触发重新加载
                    } catch (e: Exception) {
                        LogUtil.e("Reload config failed", e)
                    }
                }
            }
            
            if (cachedInstance == null) {
                cachedInstance = load()
            }
            return cachedInstance!!
        }

        @JvmStatic
        fun load(): ModuleConfig {
            val moduleConfig = LocalKVUtil.getTable("module").getString("module_config", "{}")
            val json = moduleConfig.toJSONObject()
            return ModuleConfig(json ?: JSONObject())
        }

        @JvmStatic
        fun save(moduleConfig: ModuleConfig) {
            val json = moduleConfig.toJson()
            LocalKVUtil.getTable("module").edit().putString("module_config", json).apply()
            cachedInstance = moduleConfig
        }

        @JvmStatic
        fun isLessThanMinSize(length: Long): Boolean {
            return length < getInstance().minSpaceSize * 1024L
        }

    }
}
