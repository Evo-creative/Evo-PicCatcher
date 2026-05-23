package com.pic.catcher.config

import com.lu.magic.util.log.LogUtil
import com.pic.catcher.util.JSONX
import com.pic.catcher.util.LocalKVUtil
import com.pic.catcher.util.ext.toJSONObject
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class ModuleConfig(var source: JSONObject) {
    var isCatchGlidePic: Boolean
        get() = JSONX.optBoolean(source, "isCatchGlidePic", true)
        set(value) { source.put("isCatchGlidePic", value) }

    var isCatchWebViewPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchWebViewPic", true)
        set(value) { source.put("isCatchWebViewPic", value) }

    var isCatchNetPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchNetPic", true)
        set(value) { source.put("isCatchNetPic", value) }

    var isCatchDrawablePic: Boolean
        get() = JSONX.optBoolean(source, "isCatchDrawablePic", true)
        set(value) { source.put("isCatchDrawablePic", value) }

    var isCatchBitmapPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchBitmapPic", true)
        set(value) { source.put("isCatchBitmapPic", value) }

    var isCatchFrescoPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchFrescoPic", true)
        set(value) { source.put("isCatchFrescoPic", value) }

    var isCatchCanvasPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchCanvasPic", true)
        set(value) { source.put("isCatchCanvasPic", value) }

    var isCatchMoviePic: Boolean
        get() = JSONX.optBoolean(source, "isCatchMoviePic", true)
        set(value) { source.put("isCatchMoviePic", value) }

    var isCatchImageViewPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchImageViewPic", true)
        set(value) { source.put("isCatchImageViewPic", value) }

    var isCatchImageDecoderPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchImageDecoderPic", true)
        set(value) { source.put("isCatchImageDecoderPic", value) }

    var isCatchCoilPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchCoilPic", true)
        set(value) { source.put("isCatchCoilPic", value) }

    var isCatchNativeBitmapPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchNativeBitmapPic", true)
        set(value) { source.put("isCatchNativeBitmapPic", value) }

    var isCatchFilePic: Boolean
        get() = JSONX.optBoolean(source, "isCatchFilePic", true)
        set(value) { source.put("isCatchFilePic", value) }

    var isCatchRenderNodePic: Boolean
        get() = JSONX.optBoolean(source, "isCatchRenderNodePic", true)
        set(value) { source.put("isCatchRenderNodePic", value) }

    var isCatchSurfacePic: Boolean
        get() = JSONX.optBoolean(source, "isCatchSurfacePic", true)
        set(value) { source.put("isCatchSurfacePic", value) }

    var isCatchHardwareRendererPic: Boolean
        get() = JSONX.optBoolean(source, "isCatchHardwareRendererPic", true)
        set(value) { source.put("isCatchHardwareRendererPic", value) }

    var minSpaceSize: Int
        get() = JSONX.optLong(source, "minSpaceSize", 0).toInt()
        set(value) { source.put("minSpaceSize", value) }

    var maxLogSizeMiB: Double
        get() = JSONX.optDouble(source, "maxLogSizeMiB", 2.0)
        set(value) { source.put("maxLogSizeMiB", value) }

    var isSaveToInternal: Boolean
        get() = JSONX.optBoolean(source, "isSaveToInternal", false)
        set(value) { source.put("isSaveToInternal", value) }

    var picDefaultSaveFormat: String
        get() = JSONX.optString(source, "picDefaultSaveFormat", "webp") ?: "webp"
        set(value) { source.put("picDefaultSaveFormat", value) }

    var picQuality: Int
        get() = JSONX.optInt(source, "picQuality", 90)
        set(value) { source.put("picQuality", value) }

    var shellAuthType: String
        get() = JSONX.optString(source, "shellAuthType", "") ?: ""
        set(value) { source.put("shellAuthType", value) }

    var rootStatus: String
        get() = JSONX.optString(source, "rootStatus", "UNKNOWN") ?: "UNKNOWN"
        set(value) { source.put("rootStatus", value) }

    var suManagerName: String
        get() = JSONX.optString(source, "suManagerName", "Root") ?: "Root"
        set(value) { source.put("suManagerName", value) }

    fun toJson(): String = source.toString()

    fun save() {
        save(this)
    }

    companion object {
        private val cachedInstance = AtomicReference<ModuleConfig>()
        private var isBackgroundSyncStarted = false
        private const val SYNC_INTERVAL = 10000L // 10秒异步同步一次

        @JvmStatic
        fun getInstance(): ModuleConfig {
            var instance = cachedInstance.get()
            if (instance == null) {
                instance = load()
                cachedInstance.set(instance)
            }
            return instance
        }

        @JvmStatic
        fun ensureBackgroundSync() {
            if (isBackgroundSyncStarted) return
            synchronized(this) {
                if (isBackgroundSyncStarted) return
                isBackgroundSyncStarted = true
                thread(name = "PicCatcher-ConfigSync", isDaemon = true) {
                    while (true) {
                        try {
                            Thread.sleep(SYNC_INTERVAL)
                            val table = LocalKVUtil.getTable("module")
                            if (table.javaClass.name.contains("XSharedPreferences")) {
                                try {
                                    table.javaClass.getMethod("reload").invoke(table)
                                    cachedInstance.set(load())
                                } catch (e: Exception) {}
                            }
                        } catch (e: InterruptedException) {
                            break
                        } catch (e: Exception) {}
                    }
                }
            }
        }

        @JvmStatic
        fun load(): ModuleConfig {
            val moduleConfig = LocalKVUtil.getTable("module").getString("module_config", "{}")
            return ModuleConfig(moduleConfig.toJSONObject() ?: JSONObject())
        }

        @JvmStatic
        fun save(moduleConfig: ModuleConfig) {
            LocalKVUtil.getTable("module").edit().putString("module_config", moduleConfig.toJson()).apply()
            cachedInstance.set(moduleConfig)
        }

        @JvmStatic
        fun isLessThanMinSize(length: Long): Boolean {
            return length < getInstance().minSpaceSize * 1024L
        }
    }
}
