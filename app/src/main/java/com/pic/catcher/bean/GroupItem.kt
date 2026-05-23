package com.pic.catcher.bean

/**
 * @author PicCatcher
 * @description 分组标题项，支持展开/折叠
 */
class GroupItem(title: CharSequence, isExpanded: Boolean = true) : UiItem() {
    var title: CharSequence by observableProperty(title, "title")
    var isExpanded: Boolean by observableProperty(isExpanded, "isExpanded")
}
