package com.pic.catcher.bean

/**
 * 分辨率配置项 [宽] x [高]
 */
class ResolutionItem(val title: String, var width: String, var height: String) : ItemType {
    
    var isPreviewing: Boolean = false
    private val listeners = mutableListOf<(String, String) -> Unit>()

    fun onResolutionChanged(listener: (String, String) -> Unit) {
        listeners.add(listener)
    }

    fun notifyChanged() {
        listeners.forEach { it(width, height) }
    }
}
