package com.pic.catcher.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.slider.Slider
import com.pic.catcher.R
import com.pic.catcher.adapter.BindingListAdapter
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.bean.*
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.databinding.*
import com.pic.catcher.ui.config.PicFormat
import com.pic.catcher.util.RootUtil
import com.pic.catcher.util.ext.dp
import com.pic.catcher.util.ext.setPadding
import com.pic.catcher.util.ext.toDoubleElse
import com.pic.catcher.util.ext.toIntElse

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var mAdapter: ConfigListAdapter
    private lateinit var moduleConfig: ModuleConfig
    private lateinit var mConfigSourceText: String
    private lateinit var catcherItems: List<ItemType>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::moduleConfig.isInitialized && moduleConfig.source.toString() != mConfigSourceText) {
            moduleConfig.save()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = ConfigListAdapter()
        binding.listView.adapter = mAdapter
        
        binding.toolbar.inflateMenu(R.menu.menu_settings)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_refresh) {
                refreshConfigUI()
                Toast.makeText(context, "已刷新配置", Toast.LENGTH_SHORT).show()
                true
            } else false
        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val item = mAdapter.getItem(position)
            if (item is GroupItem) {
                toggleCatcherGroup(item, position)
            } else if (item is TextItem && item.name == "授权状态") {
                if (RootUtil.hasRootPermission()) {
                    Toast.makeText(context, "Root 权限正常", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "请前往首页点击“激活”或“申请权限”", Toast.LENGTH_SHORT).show()
                }
            }
        }

        refreshConfigUI()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && isAdded) {
            refreshConfigUI()
        }
    }

    private fun refreshConfigUI() {
        val oldExpanded = (if (::mAdapter.isInitialized) mAdapter.getData().find { it is GroupItem } as? GroupItem else null)?.isExpanded ?: false
        moduleConfig = ModuleConfig.getInstance()
        mConfigSourceText = moduleConfig.source.toString()

        val picFormatList = listOf(PicFormat.WEBP, PicFormat.JPG, PicFormat.PNG)
        val picSelectFormatIndex = picFormatList.indexOfFirst { it == moduleConfig.picDefaultSaveFormat }
        
        catcherItems = mutableListOf<ItemType>(
            // --- 高影响 (High) ---
            TextItem(getString(R.string.config_header_high)),
            SwitchItem(getString(R.string.config_catch_skiapic_pic), moduleConfig.isCatchSkiaPic, getString(R.string.config_catch_skiapic_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchSkiaPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_native_bitmap_pic), moduleConfig.isCatchNativeBitmapPic, getString(R.string.config_catch_native_bitmap_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchNativeBitmapPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_canvas_pic), moduleConfig.isCatchCanvasPic, getString(R.string.config_catch_canvas_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchCanvasPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_surface_pic), moduleConfig.isCatchSurfacePic, getString(R.string.config_catch_surface_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchSurfacePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_rendernode_pic), moduleConfig.isCatchRenderNodePic, getString(R.string.config_catch_rendernode_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchRenderNodePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_hardwarerenderer_pic), moduleConfig.isCatchHardwareRendererPic, getString(R.string.config_catch_hardwarerenderer_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchHardwareRendererPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_textureview_pic), moduleConfig.isCatchTextureViewPic, getString(R.string.config_catch_textureview_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchTextureViewPic = checked; updateConfig() }
            },

            // --- 中影响 (Medium) ---
            TextItem(getString(R.string.config_header_medium)),
            SwitchItem(getString(R.string.config_catch_webview_pic), moduleConfig.isCatchWebViewPic, getString(R.string.config_catch_webview_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchWebViewPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_compose_pic), moduleConfig.isCatchComposePic, getString(R.string.config_catch_compose_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchComposePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_flutter_pic), moduleConfig.isCatchFlutterPic, getString(R.string.config_catch_flutter_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchFlutterPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_reactnative_pic), moduleConfig.isCatchReactNativePic, getString(R.string.config_catch_reactnative_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchReactNativePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_litho_pic), moduleConfig.isCatchLithoPic, getString(R.string.config_catch_litho_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchLithoPic = checked; updateConfig() }
            },

            // --- 低开销 (Low) ---
            TextItem(getString(R.string.config_header_low)),
            SwitchItem(getString(R.string.config_catch_net_pic), moduleConfig.isCatchNetPic, getString(R.string.config_catch_net_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchNetPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_bitmap_pic), moduleConfig.isCatchBitmapPic, getString(R.string.config_catch_bitmap_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchBitmapPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_glide_pic), moduleConfig.isCatchGlidePic, getString(R.string.config_catch_glide_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchGlidePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_coil_pic), moduleConfig.isCatchCoilPic, getString(R.string.config_catch_coil_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchCoilPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_fresco_pic), moduleConfig.isCatchFrescoPic, getString(R.string.config_catch_fresco_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchFrescoPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_picasso_pic), moduleConfig.isCatchPicassoPic, getString(R.string.config_catch_picasso_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchPicassoPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_file_pic), moduleConfig.isCatchFilePic, getString(R.string.config_catch_file_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchFilePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_movie_pic), moduleConfig.isCatchMoviePic, getString(R.string.config_catch_movie_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchMoviePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_drawable_pic), moduleConfig.isCatchDrawablePic, getString(R.string.config_catch_drawable_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchDrawablePic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_imageview_pic), moduleConfig.isCatchImageViewPic, getString(R.string.config_catch_imageview_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchImageViewPic = checked; updateConfig() }
            },
            SwitchItem(getString(R.string.config_catch_imagedecoder_pic), moduleConfig.isCatchImageDecoderPic, getString(R.string.config_catch_imagedecoder_pic_desc)).apply {
                addPropertyChangeListener { moduleConfig.isCatchImageDecoderPic = checked; updateConfig() }
            }
        )

        val items = mutableListOf<ItemType>()
        val groupItem = GroupItem(getString(R.string.config_group_catcher_methods), oldExpanded)
        items.add(groupItem)
        
        if (groupItem.isExpanded) {
            // 插入“快捷开启/关闭”标题
            items.add(TextItem(getString(R.string.config_header_quick_presets)))
            // 将快捷预设移到折叠项内部的第一行
            items.add(QuickPresetItem().apply {
                onPresetSelected = { level -> applyQuickPreset(level) }
            })
            items.addAll(catcherItems)
        }

        // 插入“图片保存”标题
        items.add(TextItem(getString(R.string.config_header_save_settings)))
        items.add(
            SwitchItem(
                getString(R.string.config_save_to_internal),
                moduleConfig.isSaveToInternal,
                if (moduleConfig.isSaveToInternal) getString(R.string.config_save_to_internal_on) else getString(R.string.config_save_to_internal_off)
            ).apply {
                addPropertyChangeListener {
                    if ("checked" == it.propertyName) {
                        moduleConfig.isSaveToInternal = checked
                        desc = if (checked) getString(R.string.config_save_to_internal_on) else getString(R.string.config_save_to_internal_off)
                        mAdapter.notifyDataSetChanged()
                        updateConfig()
                    }
                }
            }
        )

        val authStatus = when (moduleConfig.rootStatus) {
            "AUTHORIZED" -> "${moduleConfig.suManagerName} 已授权"
            "DENIED" -> "已拒绝 (${moduleConfig.suManagerName})"
            "NOT_FOUND" -> "Root 未授权"
            else -> "未授权 (请到首页授权)"
        }
        items.add(TextItem("授权状态", authStatus))

        items.addAll(listOf(
            EditItem(getString(R.string.config_min_space_size), moduleConfig.minSpaceSize.toString(), InputType.TYPE_CLASS_NUMBER).apply {
                addPropertyChangeListener { moduleConfig.minSpaceSize = value.toIntElse(0); updateConfig() }
            },
            SliderItem(getString(R.string.config_pic_quality), moduleConfig.picQuality.toFloat(), 1.0f, 100.0f).apply {
                addPropertyChangeListener {
                    if (it.propertyName == "value") {
                        moduleConfig.picQuality = (it.newValue as Float).toInt()
                    }
                }
            },
            SpinnerItem(getString(R.string.config_save_pic_default_format), picFormatList, picSelectFormatIndex).apply {
                addPropertyChangeListener { moduleConfig.picDefaultSaveFormat = picFormatList[selectedIndex]; updateConfig() }
            }
        ))
        
        mAdapter.setData(items)
    }

    private fun applyQuickPreset(level: QuickPresetItem.PresetLevel) {
        val targetList = when (level) {
            QuickPresetItem.PresetLevel.ALL -> listOf(
                // High
                "isCatchSkiaPic", "isCatchNativeBitmapPic", "isCatchCanvasPic",
                "isCatchSurfacePic", "isCatchRenderNodePic", "isCatchHardwareRendererPic",
                "isCatchTextureViewPic",
                // Medium
                "isCatchWebViewPic", "isCatchComposePic", "isCatchFlutterPic",
                "isCatchReactNativePic", "isCatchLithoPic",
                // Low
                "isCatchNetPic", "isCatchBitmapPic", "isCatchGlidePic", "isCatchCoilPic",
                "isCatchFrescoPic", "isCatchPicassoPic", "isCatchFilePic", "isCatchMoviePic",
                "isCatchDrawablePic", "isCatchImageViewPic", "isCatchImageDecoderPic"
            )
            // 高影响：系统渲染流水线最末端的 Native/Framework 图形引擎点 (Skia/Native/Canvas 等)
            QuickPresetItem.PresetLevel.HIGH -> listOf(
                "isCatchSkiaPic", "isCatchNativeBitmapPic", "isCatchCanvasPic",
                "isCatchSurfacePic", "isCatchRenderNodePic", "isCatchHardwareRendererPic",
                "isCatchTextureViewPic"
            )
            // 中影响：跨平台声明式 UI 框架与复杂 Web 容器 (WebView/Flutter/Compose 等)
            QuickPresetItem.PresetLevel.MEDIUM -> listOf(
                "isCatchWebViewPic", "isCatchComposePic", "isCatchFlutterPic",
                "isCatchReactNativePic", "isCatchLithoPic"
            )
            // 低开销：应用层标准图片加载库、View 组件及网络流量拦截 (Glide/Bitmap/NetPic 等)
            QuickPresetItem.PresetLevel.LOW -> listOf(
                "isCatchNetPic", "isCatchBitmapPic", "isCatchGlidePic", "isCatchCoilPic",
                "isCatchFrescoPic", "isCatchPicassoPic", "isCatchFilePic", "isCatchMoviePic",
                "isCatchDrawablePic", "isCatchImageViewPic", "isCatchImageDecoderPic"
            )
        }

        // 逻辑：如果列表中有任何一个没开，就全部打开；如果全部都开着，就全部关闭
        val allEnabled = targetList.all { key ->
            try {
                val field = moduleConfig.javaClass.getDeclaredField(key)
                field.isAccessible = true
                field.get(moduleConfig) as Boolean
            } catch (e: Exception) {
                false
            }
        }

        val targetState = !allEnabled
        targetList.forEach { key ->
            try {
                // 直接更新 source 并同步字段，确保 ModuleConfig.save() 能保存正确状态
                moduleConfig.source.put(key, targetState)
            } catch (e: Exception) {}
        }
        moduleConfig.updateFrom(moduleConfig.source)

        updateConfig()
        refreshConfigUI()
        val status = if (targetState) "已开启" else "已关闭"
        Toast.makeText(context, "${getPresetName(level)}$status", Toast.LENGTH_SHORT).show()
    }

    private fun getPresetName(level: QuickPresetItem.PresetLevel) = when (level) {
        QuickPresetItem.PresetLevel.ALL -> "全火力"
        QuickPresetItem.PresetLevel.HIGH -> "高影响"
        QuickPresetItem.PresetLevel.MEDIUM -> "中影响"
        QuickPresetItem.PresetLevel.LOW -> "低开销"
    }

    private fun toggleCatcherGroup(group: GroupItem, position: Int) {
        group.isExpanded = !group.isExpanded
        refreshConfigUI()
    }

    private fun updateConfig() {
        moduleConfig.save()
    }

    inner class ConfigListAdapter : BindingListAdapter<ItemType>() {
        override fun getViewTypeCount(): Int = 7
        override fun getItemViewType(position: Int): Int {
            val item = getItem(position) ?: return ItemType.TYPE_TEXT
            return when (item) {
                is SwitchItem -> ItemType.TYPE_SWITCH
                is EditItem -> ItemType.TYPE_EDIT
                is SpinnerItem -> ItemType.TYPE_SPINNER
                is SliderItem -> ItemType.TYPE_SLIDER
                is GroupItem -> ItemType.TYPE_GROUP
                is QuickPresetItem -> ItemType.TYPE_QUICK_PRESET
                else -> ItemType.TYPE_TEXT
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder = when (viewType) {
            ItemType.TYPE_SWITCH -> BindingHolder(ItemConfigSwitchBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_EDIT -> BindingHolder(ItemConfigEditBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_SPINNER -> BindingHolder(ItemConfigSpinnerBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_SLIDER -> BindingHolder(ItemConfigSliderBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_GROUP -> BindingHolder(ItemConfigGroupBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_QUICK_PRESET -> BindingHolder(ItemConfigQuickPresetBinding.inflate(layoutInflater, parent, false))
            else -> BindingHolder(ItemConfigTextBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(vh: BindingHolder, position: Int, parent: ViewGroup) {
            vh.binding.root.setPadding(h = 16.dp, v = 8.dp)
            when (val item = getItem(position)) {
                is QuickPresetItem -> {
                    val holder = vh.binding as ItemConfigQuickPresetBinding
                    holder.btnAll.setOnClickListener { item.onPresetSelected?.invoke(QuickPresetItem.PresetLevel.ALL) }
                    holder.btnHigh.setOnClickListener { item.onPresetSelected?.invoke(QuickPresetItem.PresetLevel.HIGH) }
                    holder.btnMedium.setOnClickListener { item.onPresetSelected?.invoke(QuickPresetItem.PresetLevel.MEDIUM) }
                    holder.btnLow.setOnClickListener { item.onPresetSelected?.invoke(QuickPresetItem.PresetLevel.LOW) }
                }
                is GroupItem -> {
                    val holder = vh.binding as ItemConfigGroupBinding
                    holder.groupTitle.text = item.title
                    holder.groupIndicator.animate()
                        .rotation(if (item.isExpanded) 180f else 0f)
                        .setDuration(300)
                        .start()
                }
                is SwitchItem -> {
                    val holder = vh.binding as ItemConfigSwitchBinding
                    holder.itemTitle.text = item.title
                    holder.itemDesc.visibility = if (item.desc != null) View.VISIBLE else View.GONE
                    holder.itemDesc.text = item.desc
                    holder.itemSwitch.setOnCheckedChangeListener(null)
                    holder.itemSwitch.isChecked = item.checked
                    holder.itemSwitch.setOnCheckedChangeListener { _, isChecked -> item.checked = isChecked }
                }
                is EditItem -> {
                    val holder = vh.binding as ItemConfigEditBinding
                    holder.itemTitle.text = item.name
                    holder.itemEdit.inputType = item.inputType
                    holder.itemEdit.setText(item.value)
                    val oldWatcher = holder.itemEdit.getTag(R.id.tag_text_watcher) as? TextWatcher
                    holder.itemEdit.removeTextChangedListener(oldWatcher)
                    val textWatcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) { item.value = s?.toString() }
                    }
                    holder.itemEdit.setTag(R.id.tag_text_watcher, textWatcher)
                    holder.itemEdit.addTextChangedListener(textWatcher)
                }
                is SpinnerItem -> {
                    val holder = vh.binding as ItemConfigSpinnerBinding
                    holder.itemTitle.text = item.title
                    holder.itemSpinner.adapter = ArrayAdapter(vh.itemView.context, android.R.layout.simple_spinner_item, item.items)
                    holder.itemSpinner.setSelection(item.selectedIndex)
                    holder.itemSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                            item.selectedIndex = pos
                            holder.itemDesc.text = when (item.selectedItem ?: "") {
                                PicFormat.WEBP -> getString(R.string.spinner_des_webp_format)
                                PicFormat.PNG -> getString(R.string.spinner_des_png_format)
                                PicFormat.JPG -> getString(R.string.spinner_des_jpg_format)
                                else -> ""
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }
                is SliderItem -> {
                    val holder = vh.binding as ItemConfigSliderBinding
                    holder.itemTitle.text = item.title
                    holder.itemValueText.text = item.currentValue.toInt().toString()
                    holder.itemSlider.valueFrom = item.min
                    holder.itemSlider.valueTo = item.max
                    holder.itemSlider.stepSize = item.step
                    holder.itemSlider.value = item.currentValue
                    holder.itemSlider.clearOnChangeListeners()
                    holder.itemSlider.addOnChangeListener { _, value, fromUser ->
                        if (fromUser) {
                            item.currentValue = value
                            holder.itemValueText.text = value.toInt().toString()
                        }
                    }
                    holder.itemSlider.clearOnSliderTouchListeners()
                    holder.itemSlider.addOnSliderTouchListener(object : com.google.android.material.slider.Slider.OnSliderTouchListener {
                        override fun onStartTrackingTouch(slider: com.google.android.material.slider.Slider) {}
                        override fun onStopTrackingTouch(slider: com.google.android.material.slider.Slider) {
                            updateConfig()
                        }
                    })
                }
                is TextItem -> {
                    val holder = vh.binding as ItemConfigTextBinding
                    holder.itemTitle.text = item.name
                    holder.itemValue.text = item.value
                }
            }
        }
    }
}
