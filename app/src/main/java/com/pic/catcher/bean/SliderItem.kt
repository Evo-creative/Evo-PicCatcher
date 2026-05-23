package com.pic.catcher.bean

/**
 * @author Evo
 * @date 2025/01/24
 * @description 滑块配置项
 */
class SliderItem(
    val title: String,
    value: Float,
    val min: Float,
    val max: Float,
    val step: Float = 1.0f
) : UiItem() {

    var currentValue: Float by observableProperty(value, "value")
}
