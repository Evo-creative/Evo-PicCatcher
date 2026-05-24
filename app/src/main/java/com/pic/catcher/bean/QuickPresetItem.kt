package com.pic.catcher.bean

class QuickPresetItem : ItemType {
    var onPresetSelected: ((PresetLevel) -> Unit)? = null

    enum class PresetLevel {
        ALL, HIGH, MEDIUM, LOW
    }
}
