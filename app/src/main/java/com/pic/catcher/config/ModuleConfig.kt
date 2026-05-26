package com.pic.catcher.config

import com.pic.catcher.util.JSONX
import com.pic.catcher.util.LocalKVUtil
import com.pic.catcher.util.ext.toJSONObject
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * 拦截配置系统核心模型
 * 按照：高影响(底层渲染) -> 中影响(框架容器) -> 低开销(标准组件) 划分子系统
 */
class ModuleConfig(var source: JSONObject) {

    // ==========================================
    // 1. 高影响 (HIGH IMPACT) - 系统渲染流水线与底层引擎
    // ==========================================

    var isCatchSkiaPic: Boolean = JSONX.optBoolean(source, "isCatchSkiaPic", false)
        set(value) { field = value; source.put("isCatchSkiaPic", value) }

    var isCatchNativeBitmapPic: Boolean = JSONX.optBoolean(source, "isCatchNativeBitmapPic", true)
        set(value) { field = value; source.put("isCatchNativeBitmapPic", value) }

    var isCatchCanvasPic: Boolean = JSONX.optBoolean(source, "isCatchCanvasPic", true)
        set(value) { field = value; source.put("isCatchCanvasPic", value) }

    var isCatchSurfacePic: Boolean = JSONX.optBoolean(source, "isCatchSurfacePic", true)
        set(value) { field = value; source.put("isCatchSurfacePic", value) }

    var isCatchRenderNodePic: Boolean = JSONX.optBoolean(source, "isCatchRenderNodePic", true)
        set(value) { field = value; source.put("isCatchRenderNodePic", value) }

    var isCatchHardwareRendererPic: Boolean = JSONX.optBoolean(source, "isCatchHardwareRendererPic", true)
        set(value) { field = value; source.put("isCatchHardwareRendererPic", value) }

    var isCatchTextureViewPic: Boolean = JSONX.optBoolean(source, "isCatchTextureViewPic", false)
        set(value) { field = value; source.put("isCatchTextureViewPic", value) }

    // ==========================================
    // 2. 中影响 (MEDIUM IMPACT) - 跨平台框架、UI 容器与自绘系统
    // ==========================================

    var isCatchWebViewPic: Boolean = JSONX.optBoolean(source, "isCatchWebViewPic", true)
        set(value) { field = value; source.put("isCatchWebViewPic", value) }

    var isCatchComposePic: Boolean = JSONX.optBoolean(source, "isCatchComposePic", false)
        set(value) { field = value; source.put("isCatchComposePic", value) }

    var isCatchFlutterPic: Boolean = JSONX.optBoolean(source, "isCatchFlutterPic", false)
        set(value) { field = value; source.put("isCatchFlutterPic", value) }

    var isCatchReactNativePic: Boolean = JSONX.optBoolean(source, "isCatchReactNativePic", false)
        set(value) { field = value; source.put("isCatchReactNativePic", value) }

    var isCatchLithoPic: Boolean = JSONX.optBoolean(source, "isCatchLithoPic", true)
        set(value) { field = value; source.put("isCatchLithoPic", value) }

    // ==========================================
    // 3. 低开销 (LOW OVERHEAD) - 基础图片库、标准 View 与网络拦截
    // ==========================================

    var isCatchNetPic: Boolean = JSONX.optBoolean(source, "isCatchNetPic", true)
        set(value) { field = value; source.put("isCatchNetPic", value) }

    var isCatchBitmapPic: Boolean = JSONX.optBoolean(source, "isCatchBitmapPic", true)
        set(value) { field = value; source.put("isCatchBitmapPic", value) }

    var isCatchGlidePic: Boolean = JSONX.optBoolean(source, "isCatchGlidePic", true)
        set(value) { field = value; source.put("isCatchGlidePic", value) }

    var isCatchCoilPic: Boolean = JSONX.optBoolean(source, "isCatchCoilPic", true)
        set(value) { field = value; source.put("isCatchCoilPic", value) }

    var isCatchFrescoPic: Boolean = JSONX.optBoolean(source, "isCatchFrescoPic", true)
        set(value) { field = value; source.put("isCatchFrescoPic", value) }

    var isCatchPicassoPic: Boolean = JSONX.optBoolean(source, "isCatchPicassoPic", true)
        set(value) { field = value; source.put("isCatchPicassoPic", value) }

    var isCatchFilePic: Boolean = JSONX.optBoolean(source, "isCatchFilePic", true)
        set(value) { field = value; source.put("isCatchFilePic", value) }

    var isCatchMoviePic: Boolean = JSONX.optBoolean(source, "isCatchMoviePic", true)
        set(value) { field = value; source.put("isCatchMoviePic", value) }

    var isCatchDrawablePic: Boolean = JSONX.optBoolean(source, "isCatchDrawablePic", true)
        set(value) { field = value; source.put("isCatchDrawablePic", value) }

    var isCatchImageViewPic: Boolean = JSONX.optBoolean(source, "isCatchImageViewPic", true)
        set(value) { field = value; source.put("isCatchImageViewPic", value) }

    var isCatchImageDecoderPic: Boolean = JSONX.optBoolean(source, "isCatchImageDecoderPic", true)
        set(value) { field = value; source.put("isCatchImageDecoderPic", value) }

    // ==========================================
    // 辅助配置 (General Settings)
    // ==========================================

    var minSpaceSize: Int = JSONX.optLong(source, "minSpaceSize", 0).toInt()
        set(value) { field = value; source.put("minSpaceSize", value) }

    var isSaveToInternal: Boolean = JSONX.optBoolean(source, "isSaveToInternal", false)
        set(value) { field = value; source.put("isSaveToInternal", value) }

    var isGenerateNoMedia: Boolean = JSONX.optBoolean(source, "isGenerateNoMedia", true)
        set(value) { field = value; source.put("isGenerateNoMedia", value) }

    var picDefaultSaveFormat: String = JSONX.optString(source, "picDefaultSaveFormat", "webp") ?: "webp"
        set(value) { field = value; source.put("picDefaultSaveFormat", value) }

    var picQuality: Int = JSONX.optInt(source, "picQuality", 90)
        set(value) { field = value; source.put("picQuality", value) }

    var shellAuthType: String = JSONX.optString(source, "shellAuthType", "") ?: ""
        set(value) { field = value; source.put("shellAuthType", value) }

    var rootStatus: String = JSONX.optString(source, "rootStatus", "UNKNOWN") ?: "UNKNOWN"
        set(value) { field = value; source.put("rootStatus", value) }

    var suManagerName: String = JSONX.optString(source, "suManagerName", "Root") ?: "Root"
        set(value) { field = value; source.put("suManagerName", value) }

    fun toJson(): String = source.toString()

    fun updateFrom(newSource: JSONObject) {
        this.source = newSource
        // 1. High
        isCatchSkiaPic = JSONX.optBoolean(source, "isCatchSkiaPic", false)
        isCatchNativeBitmapPic = JSONX.optBoolean(source, "isCatchNativeBitmapPic", true)
        isCatchCanvasPic = JSONX.optBoolean(source, "isCatchCanvasPic", true)
        isCatchSurfacePic = JSONX.optBoolean(source, "isCatchSurfacePic", true)
        isCatchRenderNodePic = JSONX.optBoolean(source, "isCatchRenderNodePic", true)
        isCatchHardwareRendererPic = JSONX.optBoolean(source, "isCatchHardwareRendererPic", true)
        isCatchTextureViewPic = JSONX.optBoolean(source, "isCatchTextureViewPic", false)

        // 2. Medium
        isCatchWebViewPic = JSONX.optBoolean(source, "isCatchWebViewPic", true)
        isCatchComposePic = JSONX.optBoolean(source, "isCatchComposePic", false)
        isCatchFlutterPic = JSONX.optBoolean(source, "isCatchFlutterPic", false)
        isCatchReactNativePic = JSONX.optBoolean(source, "isCatchReactNativePic", false)
        isCatchLithoPic = JSONX.optBoolean(source, "isCatchLithoPic", true)

        // 3. Low
        isCatchNetPic = JSONX.optBoolean(source, "isCatchNetPic", true)
        isCatchBitmapPic = JSONX.optBoolean(source, "isCatchBitmapPic", true)
        isCatchGlidePic = JSONX.optBoolean(source, "isCatchGlidePic", true)
        isCatchCoilPic = JSONX.optBoolean(source, "isCatchCoilPic", true)
        isCatchFrescoPic = JSONX.optBoolean(source, "isCatchFrescoPic", true)
        isCatchPicassoPic = JSONX.optBoolean(source, "isCatchPicassoPic", true)
        isCatchFilePic = JSONX.optBoolean(source, "isCatchFilePic", true)
        isCatchMoviePic = JSONX.optBoolean(source, "isCatchMoviePic", true)
        isCatchDrawablePic = JSONX.optBoolean(source, "isCatchDrawablePic", true)
        isCatchImageViewPic = JSONX.optBoolean(source, "isCatchImageViewPic", true)
        isCatchImageDecoderPic = JSONX.optBoolean(source, "isCatchImageDecoderPic", true)

        // Misc
        minSpaceSize = JSONX.optLong(source, "minSpaceSize", 0).toInt()
        isSaveToInternal = JSONX.optBoolean(source, "isSaveToInternal", false)
        isGenerateNoMedia = JSONX.optBoolean(source, "isGenerateNoMedia", true)
        picDefaultSaveFormat = JSONX.optString(source, "picDefaultSaveFormat", "webp") ?: "webp"
        picQuality = JSONX.optInt(source, "picQuality", 90)
        shellAuthType = JSONX.optString(source, "shellAuthType", "") ?: ""
        rootStatus = JSONX.optString(source, "rootStatus", "UNKNOWN") ?: "UNKNOWN"
        suManagerName = JSONX.optString(source, "suManagerName", "Root") ?: "Root"
    }

    fun save() {
        save(this)
    }

    companion object {
        private val cachedInstance = AtomicReference<ModuleConfig>()
        private var isBackgroundSyncStarted = false
        private const val SYNC_INTERVAL = 5000L

        @JvmStatic
        fun getInstance(): ModuleConfig {
            return cachedInstance.get() ?: load()
        }

        @JvmStatic
        fun load(): ModuleConfig {
            val jsonStr = LocalKVUtil.getTable("module").getString("module_config", "{}")
            val json = jsonStr.toJSONObject() ?: JSONObject()
            val current = cachedInstance.get()
            if (current != null) {
                current.updateFrom(json)
                return current
            }
            val instance = ModuleConfig(json)
            cachedInstance.set(instance)
            return instance
        }

        @JvmStatic
        fun save(moduleConfig: ModuleConfig) {
            val json = moduleConfig.toJson()
            LocalKVUtil.getTable("module").edit().putString("module_config", json).apply()
            load()
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
                                    load()
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
        fun isLessThanMinSize(length: Long): Boolean {
            return length < getInstance().minSpaceSize * 1024L
        }
    }
}
