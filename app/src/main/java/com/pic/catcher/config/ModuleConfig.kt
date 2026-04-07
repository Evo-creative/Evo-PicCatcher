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

    // 新增：抓取底层位图 (Native/createBitmap)
    var isCatchNativePic: Boolean = true
        get() = JSONX.optBoolean(source, "isCatchNativePic", true)
        set(value) {
            field = value
            source.put("isCatchNativePic", value)
        }

    // 新增：抓取硬件渲染内容 (RenderNode)
    var isCatchRenderNodePic: Boolean = true
        get() = JSONX.optBoolean(source, "isCatchRenderNodePic", true)
        set(value) {
            field = value
            source.put("isCatchRenderNodePic", value)
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

    fun toJson(): String {
        return source.toString()
    }

    fun save() {
        save(this)
    }

    companion object {
        private var cachedInstance: ModuleConfig? = null

        @JvmStatic
        fun getInstance(): ModuleConfig {
            val table = LocalKVUtil.getTable("module")
            
            // 安全检查：通过类名判断，避免在 UI 进程中因为找不到 XSharedPreferences 类而崩溃
            if (table.javaClass.name.contains("XSharedPreferences")) {
                try {
                    // 反射调用 reload() 强制刷新配置
                    table.javaClass.getMethod("reload").invoke(table)
                } catch (e: Exception) {
                    LogUtil.e("Reload config failed", e)
                }
                val moduleConfigStr = table.getString("module_config", "{}")
                return ModuleConfig(moduleConfigStr.toJSONObject() ?: JSONObject())
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
