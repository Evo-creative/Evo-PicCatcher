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
        moduleConfig = ModuleConfig.getInstance()
        mConfigSourceText = moduleConfig.source.toString()

        mAdapter = ConfigListAdapter().apply {
            val picFormatList = listOf(PicFormat.WEBP, PicFormat.JPG, PicFormat.PNG)
            val picSelectFormatIndex = picFormatList.indexOfFirst { it == moduleConfig.picDefaultSaveFormat }
            
            val catcherItems = mutableListOf<ItemType>(
                SwitchItem(getString(R.string.config_catch_net_pic), moduleConfig.isCatchNetPic, getString(R.string.config_catch_net_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchNetPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_webview_pic), moduleConfig.isCatchWebViewPic, getString(R.string.config_catch_webview_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchWebViewPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_glide_pic), moduleConfig.isCatchGlidePic, getString(R.string.config_catch_glide_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchGlidePic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_coil_pic), moduleConfig.isCatchCoilPic, getString(R.string.config_catch_coil_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchCoilPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_fresco_pic), moduleConfig.isCatchFrescoPic, getString(R.string.config_catch_fresco_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchFrescoPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_bitmap_pic), moduleConfig.isCatchBitmapPic, getString(R.string.config_catch_bitmap_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchBitmapPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_imagedecoder_pic), moduleConfig.isCatchImageDecoderPic, getString(R.string.config_catch_imagedecoder_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchImageDecoderPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_native_bitmap_pic), moduleConfig.isCatchNativeBitmapPic, getString(R.string.config_catch_native_bitmap_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchNativeBitmapPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_rendernode_pic), moduleConfig.isCatchRenderNodePic, getString(R.string.config_catch_rendernode_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchRenderNodePic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_canvas_pic), moduleConfig.isCatchCanvasPic, getString(R.string.config_catch_canvas_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchCanvasPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_surface_pic), moduleConfig.isCatchSurfacePic, getString(R.string.config_catch_surface_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchSurfacePic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_hardwarerenderer_pic), moduleConfig.isCatchHardwareRendererPic, getString(R.string.config_catch_hardwarerenderer_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchHardwareRendererPic = checked
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_drawable_pic), moduleConfig.isCatchDrawablePic, getString(R.string.config_catch_drawable_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchDrawablePic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_imageview_pic), moduleConfig.isCatchImageViewPic, getString(R.string.config_catch_imageview_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchImageViewPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_file_pic), moduleConfig.isCatchFilePic, getString(R.string.config_catch_file_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchFilePic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_movie_pic), moduleConfig.isCatchMoviePic, getString(R.string.config_catch_movie_pic_desc)).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchMoviePic = checked
                        updateConfig()
                    }
                }
            )

            val items = mutableListOf<ItemType>()
            
            val groupItem = GroupItem(getString(R.string.config_group_catcher_methods), false)
            items.add(groupItem)
            // 默认不展开
            if (groupItem.isExpanded) {
                items.addAll(catcherItems)
            }

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
                            
                            if (checked) {
                                Toast.makeText(context, "将尝试使用 Root 权限进行搬运（需要系统已 Root）", Toast.LENGTH_LONG).show()
                            }
                            
                            mAdapter.notifyDataSetChanged()
                            updateConfig()
                        }
                    }
                }
            )

            // 添加授权状态显示
            val authStatus = when (moduleConfig.rootStatus) {
                "AUTHORIZED" -> "${moduleConfig.suManagerName} 已授权"
                "DENIED" -> "已拒绝 (${moduleConfig.suManagerName})"
                "NOT_FOUND" -> "Root 未授权"
                else -> "未授权 (请到首页授权)"
            }
            items.add(TextItem("授权状态", authStatus))

            items.addAll(listOf(
                EditItem(
                    getString(R.string.config_min_space_size),
                    moduleConfig.minSpaceSize.toString(),
                    InputType.TYPE_CLASS_NUMBER
                ).apply {
                    addPropertyChangeListener { 
                        moduleConfig.minSpaceSize = value.toIntElse(0) 
                        updateConfig()
                    }
                },
                EditItem(
                    getString(R.string.config_max_log_size),
                    moduleConfig.maxLogSizeMiB.toString(),
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                ).apply {
                    addPropertyChangeListener { 
                        moduleConfig.maxLogSizeMiB = value.toDoubleElse(2.0)
                        updateConfig()
                    }
                },
                SliderItem(
                    getString(R.string.config_pic_quality),
                    moduleConfig.picQuality.toFloat(),
                    1.0f,
                    100.0f
                ).apply {
                    addPropertyChangeListener {
                        moduleConfig.picQuality = (it.newValue as Float).toInt()
                        updateConfig()
                        // 局部刷新，这里为了方便直接 notifyDataSetChanged
                        mAdapter.notifyDataSetChanged()
                    }
                },
                SpinnerItem(
                    getString(R.string.config_save_pic_default_format),
                    picFormatList,
                    picSelectFormatIndex
                ).apply {
                    addPropertyChangeListener { 
                        moduleConfig.picDefaultSaveFormat = picFormatList[selectedIndex] 
                        updateConfig()
                    }
                }
            ))
            
            setData(items)
            
            // 保存 catcherItems 以便后续切换显示
            this@SettingsFragment.catcherItems = catcherItems
        }
        binding.listView.adapter = mAdapter
        
        // 处理 TextItem 点击
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
    }

    private lateinit var catcherItems: List<ItemType>

    private fun toggleCatcherGroup(group: GroupItem, position: Int) {
        group.isExpanded = !group.isExpanded
        val currentItems = mAdapter.getData().toMutableList()
        if (group.isExpanded) {
            currentItems.addAll(position + 1, catcherItems)
        } else {
            // 安全删除，防止越界或逻辑错误
            val start = position + 1
            if (start < currentItems.size) {
                val sizeToRemove = catcherItems.size.coerceAtMost(currentItems.size - start)
                repeat(sizeToRemove) {
                    currentItems.removeAt(start)
                }
            }
        }
        mAdapter.updateDataAt(currentItems)
    }

    private fun updateConfig() {
        moduleConfig.save()
    }

    inner class ConfigListAdapter : BindingListAdapter<ItemType>() {
        override fun getViewTypeCount(): Int = 6
        override fun getItemViewType(position: Int): Int = when (getItem(position)) {
            is SwitchItem -> ItemType.TYPE_SWITCH
            is EditItem -> ItemType.TYPE_EDIT
            is SpinnerItem -> ItemType.TYPE_SPINNER
            is SliderItem -> ItemType.TYPE_SLIDER
            is GroupItem -> ItemType.TYPE_GROUP
            else -> ItemType.TYPE_TEXT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder = when (viewType) {
            ItemType.TYPE_SWITCH -> BindingHolder(ItemConfigSwitchBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_EDIT -> BindingHolder(ItemConfigEditBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_SPINNER -> BindingHolder(ItemConfigSpinnerBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_SLIDER -> BindingHolder(ItemConfigSliderBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_GROUP -> BindingHolder(ItemConfigGroupBinding.inflate(layoutInflater, parent, false))
            else -> BindingHolder(ItemConfigTextBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(vh: BindingHolder, position: Int, parent: ViewGroup) {
            vh.binding.root.setPadding(h = 16.dp, v = 8.dp)
            when (val item = getItem(position)) {
                is GroupItem -> {
                    val holder = vh.binding as ItemConfigGroupBinding
                    holder.groupTitle.text = item.title
                    // MD3 风格：展开向上 (180度)，折叠向下 (0度)
                    // ic_expand_more 默认指向下
                    holder.groupIndicator.animate()
                        .rotation(if (item.isExpanded) 180f else 0f)
                        .setDuration(300)
                        .start()
                }
                is SwitchItem -> {
                    val holder = vh.binding as ItemConfigSwitchBinding
                    holder.itemTitle.text = item.title
                    
                    if (item.desc != null) {
                        holder.itemDesc.visibility = View.VISIBLE
                        holder.itemDesc.text = item.desc
                    } else {
                        holder.itemDesc.visibility = View.GONE
                    }

                    holder.itemSwitch.setOnCheckedChangeListener(null)
                    holder.itemSwitch.isChecked = item.checked
                    holder.itemSwitch.setOnCheckedChangeListener { _, isChecked -> item.checked = isChecked }
                }
                is EditItem -> {
                    val holder = vh.binding as ItemConfigEditBinding
                    holder.itemTitle.text = item.name
                    holder.itemEdit.inputType = item.inputType
                    holder.itemEdit.setText(item.value)

                    holder.itemEdit.setOnClickListener {
                        holder.itemEdit.setSelection(holder.itemEdit.text?.length ?: 0)
                    }
                    holder.itemEdit.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            holder.itemEdit.setSelection(holder.itemEdit.text?.length ?: 0)
                        }
                    }

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
                    holder.itemSlider.clearOnSliderTouchListeners()
                    
                    holder.itemSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                        override fun onStartTrackingTouch(slider: Slider) {}
                        override fun onStopTrackingTouch(slider: Slider) {
                            item.currentValue = slider.value
                        }
                    })
                    holder.itemSlider.addOnChangeListener { _, value, fromUser ->
                        if (fromUser) {
                            holder.itemValueText.text = value.toInt().toString()
                        }
                    }
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
