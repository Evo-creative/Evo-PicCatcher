package com.pic.catcher.bean

data class PresetItem(
    val name: String,
    val category: String,
    val description: String,
    val apps: List<String>,
    val details: Map<String, Boolean>,
    var isExpanded: Boolean = false
) {
    fun getDetailText(): String {
        return details.entries.joinToString("\n") { (key, value) ->
            val status = if (value) "[开]" else "[关]"
            "$status ${getFriendlyName(key)}"
        }
    }

    private fun getFriendlyName(key: String): String {
        return when (key) {
            "isCatchNetPic" -> "网络拦截"
            "isCatchWebViewPic" -> "WebView"
            "isCatchGlidePic" -> "Glide"
            "isCatchPicassoPic" -> "Picasso"
            "isCatchLithoPic" -> "Litho"
            "isCatchBitmapPic" -> "BitmapFactory"
            "isCatchFrescoPic" -> "Fresco"
            "isCatchCanvasPic" -> "Canvas"
            "isCatchCoilPic" -> "Coil"
            "isCatchSurfacePic" -> "Surface"
            "isCatchHardwareRendererPic" -> "硬件加速"
            "isCatchImageViewPic" -> "ImageView"
            "isCatchRenderNodePic" -> "RenderNode"
            "isCatchDrawablePic" -> "Drawable"
            "isCatchFilePic" -> "文件流"
            "isCatchMoviePic" -> "Movie/Gif"
            "isCatchImageDecoderPic" -> "ImageDecoder"
            "isCatchNativeBitmapPic" -> "Native层"
            "isCatchComposePic" -> "Jetpack Compose"
            "isCatchTextureViewPic" -> "TextureView"
            "isCatchFlutterPic" -> "Flutter/Skia"
            "isCatchReactNativePic" -> "ReactNative"
            "isCatchSkiaPic" -> "Skia(直接)"
            else -> key
        }
    }

    companion object {
        fun getAllPresets(): List<PresetItem> = listOf(
            PresetItem(
                "全量拦截 (极致体验)",
                "极致",
                "开启所有抓取引擎，包含现代框架。注意：由于 Hook 点极多，可能会有明显的性能抖动，建议仅在排查时使用。",
                listOf("全应用通用"),
                mapOf(
                    "isCatchNetPic" to true,
                    "isCatchWebViewPic" to true,
                    "isCatchGlidePic" to true,
                    "isCatchPicassoPic" to true,
                    "isCatchLithoPic" to true,
                    "isCatchBitmapPic" to true,
                    "isCatchFrescoPic" to true,
                    "isCatchCanvasPic" to true,
                    "isCatchCoilPic" to true,
                    "isCatchSurfacePic" to true,
                    "isCatchHardwareRendererPic" to true,
                    "isCatchImageViewPic" to true,
                    "isCatchRenderNodePic" to true,
                    "isCatchDrawablePic" to true,
                    "isCatchFilePic" to true,
                    "isCatchMoviePic" to true,
                    "isCatchImageDecoderPic" to true,
                    "isCatchNativeBitmapPic" to true,
                    "isCatchComposePic" to true,
                    "isCatchTextureViewPic" to true,
                    "isCatchFlutterPic" to true,
                    "isCatchReactNativePic" to true,
                    "isCatchSkiaPic" to true
                )
            ),
            PresetItem(
                "Glide 系 (国产主流)",
                "主流",
                "特点：RecyclerView 图片流、电商/社区/资讯。命中率最高，性能开销最低。",
                listOf("哔哩哔哩", "酷安", "微博", "贴吧", "知乎", "虎扑", "淘宝", "京东", "拼多多", "大众点评", "美团", "网易云音乐", "QQ音乐", "汽水音乐", "小黑盒"),
                mapOf(
                    "isCatchGlidePic" to true,
                    "isCatchBitmapPic" to true,
                    "isCatchImageViewPic" to true
                )
            ),
            PresetItem(
                "Coil + Compose (新时代)",
                "现代",
                "特点：Jetpack Compose、Kotlin 化、新版 App。Compose 生态大量使用 Coil。",
                listOf("小红书新版", "Google Play", "Google Photos", "Google Home", "Google Drive", "网易云音乐新版", "Bilibili部分页面", "Mihon", "Jellyfin", "Immich"),
                mapOf(
                    "isCatchCoilPic" to true,
                    "isCatchComposePic" to true,
                    "isCatchSkiaPic" to true,
                    "isCatchImageDecoderPic" to true
                )
            ),
            PresetItem(
                "Fresco / Litho (Meta 系)",
                "社交",
                "特点：超高性能 Feed 流、异步组件树、复杂社交流。Fresco 是 Meta 系核心方案。",
                listOf("Instagram", "Facebook", "Threads", "Messenger", "Pixiv", "Discord", "Reddit", "Pinterest"),
                mapOf(
                    "isCatchFrescoPic" to true,
                    "isCatchLithoPic" to true,
                    "isCatchBitmapPic" to true,
                    "isCatchCanvasPic" to true
                )
            ),
            PresetItem(
                "WebView / Chromium (H5)",
                "网页",
                "特点：H5、混合应用、小程序、浏览器内核。很多国产 App 本质是 WebView 壳。",
                listOf("微信", "QQ", "支付宝", "淘宝部分页面", "Chrome", "Edge", "Via", "X浏览器", "夸克", "UC浏览器"),
                mapOf(
                    "isCatchWebViewPic" to true,
                    "isCatchCanvasPic" to true,
                    "isCatchSkiaPic" to true,
                    "isCatchDrawablePic" to true
                )
            ),
            PresetItem(
                "短视频 / Surface / 硬件",
                "多媒体",
                "特点：视频帧、GPU 纹理、Surface、RenderNode。解决此类 App “图片不是图片”的问题。",
                listOf("抖音", "TikTok", "快手", "西瓜视频", "B站直播", "斗鱼", "虎牙", "YouTube", "Netflix", "Disney+"),
                mapOf(
                    "isCatchSurfacePic" to true,
                    "isCatchTextureViewPic" to true,
                    "isCatchHardwareRendererPic" to true,
                    "isCatchRenderNodePic" to true
                )
            ),
            PresetItem(
                "React Native 系",
                "跨平台",
                "特点：JS Bridge、混合渲染。Discord 等社交 App 常用方案。",
                listOf("Discord", "Threads", "Instagram部分页面", "Coinbase", "Bluesky", "Shopify Shop"),
                mapOf(
                    "isCatchReactNativePic" to true,
                    "isCatchFrescoPic" to true,
                    "isCatchCanvasPic" to true
                )
            ),
            PresetItem(
                "Flutter / Skia (自绘)",
                "跨平台",
                "特点：自带 Skia、不走 Android View。通过 Skia 指令拦截实现图片抓取。",
                listOf("闲鱼", "Google Ads", "Google Pay部分页面", "eBay Motors", "部分银行App"),
                mapOf(
                    "isCatchFlutterPic" to true,
                    "isCatchSkiaPic" to true,
                    "isCatchSurfacePic" to true
                )
            ),
            PresetItem(
                "游戏 / Unity / Unreal",
                "游戏",
                "特点：OpenGL/Vulkan、GPU 纹理、FrameBuffer。针对 Native 层和底层渲染链路。",
                listOf("原神", "崩坏：星穹铁道", "鸣潮", "明日方舟", "PUBG Mobile", "王者荣耀", "和平精英"),
                mapOf(
                    "isCatchSurfacePic" to true,
                    "isCatchHardwareRendererPic" to true,
                    "isCatchNativeBitmapPic" to true
                )
            ),
            PresetItem(
                "压力测试 (典型应用)",
                "测试",
                "选取了各技术栈最具代表性的应用，建议作为新功能上线后的首选测试对象。",
                listOf("Chrome (WebView)", "Instagram (Fresco)", "TikTok (Surface)", "Discord (RN)", "闲鱼 (Flutter)", "小红书 (Compose)", "Bilibili (混合)"),
                mapOf(
                    "isCatchGlidePic" to true,
                    "isCatchCoilPic" to true,
                    "isCatchComposePic" to true,
                    "isCatchFlutterPic" to true,
                    "isCatchWebViewPic" to true,
                    "isCatchFrescoPic" to true,
                    "isCatchSurfacePic" to true
                )
            )
        )
    }
}
