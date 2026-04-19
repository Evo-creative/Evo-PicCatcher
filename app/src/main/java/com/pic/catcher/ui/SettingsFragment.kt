package com.pic.catcher.ui

import android.content.pm.PackageManager
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
import com.pic.catcher.R
import com.pic.catcher.adapter.BindingListAdapter
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.bean.EditItem
import com.pic.catcher.bean.ItemType
import com.pic.catcher.bean.SpinnerItem
import com.pic.catcher.bean.SwitchItem
import com.pic.catcher.bean.TextItem
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.databinding.FragmentSettingsBinding
import com.pic.catcher.databinding.ItemConfigEditBinding
import com.pic.catcher.databinding.ItemConfigSpinnerBinding
import com.pic.catcher.databinding.ItemConfigSwitchBinding
import com.pic.catcher.databinding.ItemConfigTextBinding
import com.pic.catcher.ui.config.PicFormat
import com.pic.catcher.util.ShellUtil
import com.pic.catcher.util.ext.dp
import com.pic.catcher.util.ext.setPadding
import com.pic.catcher.util.ext.toDoubleElse
import com.pic.catcher.util.ext.toIntElse
import rikka.shizuku.Shizuku

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var mAdapter: ConfigListAdapter
    private lateinit var moduleConfig: ModuleConfig
    private lateinit var mConfigSourceText: String

    // Shizuku 权限请求监听器
    private val ON_REQUEST_PERMISSION_RESULT = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1001) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Shizuku 权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Shizuku 权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        try {
            Shizuku.addRequestPermissionResultListener(ON_REQUEST_PERMISSION_RESULT)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            Shizuku.removeRequestPermissionResultListener(ON_REQUEST_PERMISSION_RESULT)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
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
            
            val items = mutableListOf<ItemType>(
                SwitchItem(getString(R.string.config_catch_net_pic), moduleConfig.isCatchNetPic).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchNetPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_webview_pic), moduleConfig.isCatchWebViewPic).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchWebViewPic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(getString(R.string.config_catch_glide_pic), moduleConfig.isCatchGlidePic).apply {
                    addPropertyChangeListener { 
                        moduleConfig.isCatchGlidePic = checked 
                        updateConfig()
                    }
                },
                SwitchItem(
                    getString(R.string.config_save_to_internal),
                    moduleConfig.isSaveToInternal,
                    if (moduleConfig.isSaveToInternal) getString(R.string.config_save_to_internal_on) else getString(R.string.config_save_to_internal_off)
                ).apply {
                    addPropertyChangeListener {
                        if ("checked" == it.propertyName) {
                            moduleConfig.isSaveToInternal = checked
                            desc = if (checked) getString(R.string.config_save_to_internal_on) else getString(R.string.config_save_to_internal_off)
                            
                            if (checked && !ShellUtil.hasShizukuPermission()) {
                                if (ShellUtil.isShizukuAvailable()) {
                                    try {
                                        Shizuku.requestPermission(1001)
                                    } catch (e: Throwable) {
                                        Toast.makeText(context, "请求 Shizuku 权限失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "未检测到 Shizuku，将尝试使用 Root 权限（需要系统已 Root）", Toast.LENGTH_LONG).show()
                                }
                            }
                            
                            mAdapter.notifyDataSetChanged()
                            updateConfig()
                        }
                    }
                }
            )

            // 添加 Shizuku 状态显示
            items.add(TextItem("Shizuku 状态", if (ShellUtil.hasShizukuPermission()) "已授权" else if (ShellUtil.isShizukuAvailable()) "未授权 (点击申请)" else "未运行"))

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
        }
        binding.listView.adapter = mAdapter
        
        // 处理 TextItem 点击申请权限
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val item = mAdapter.getItem(position)
            if (item is TextItem && item.name == "Shizuku 状态") {
                if (ShellUtil.isShizukuAvailable()) {
                    if (!ShellUtil.hasShizukuPermission()) {
                        try {
                            Shizuku.requestPermission(1001)
                        } catch (e: Throwable) {
                            Toast.makeText(context, "请求 Shizuku 权限失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Shizuku 权限已授予", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Shizuku 未运行", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateConfig() {
        moduleConfig.save()
    }

    inner class ConfigListAdapter : BindingListAdapter<ItemType>() {
        override fun getViewTypeCount(): Int = 4
        override fun getItemViewType(position: Int): Int = when (getItem(position)) {
            is SwitchItem -> ItemType.TYPE_SWITCH
            is EditItem -> ItemType.TYPE_EDIT
            is SpinnerItem -> ItemType.TYPE_SPINNER
            else -> ItemType.TYPE_TEXT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder = when (viewType) {
            ItemType.TYPE_SWITCH -> BindingHolder(ItemConfigSwitchBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_EDIT -> BindingHolder(ItemConfigEditBinding.inflate(layoutInflater, parent, false))
            ItemType.TYPE_SPINNER -> BindingHolder(ItemConfigSpinnerBinding.inflate(layoutInflater, parent, false))
            else -> BindingHolder(ItemConfigTextBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(vh: BindingHolder, position: Int, parent: ViewGroup) {
            vh.binding.root.setPadding(h = 16.dp, v = 8.dp)
            when (val item = getItem(position)) {
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
                is TextItem -> {
                    val holder = vh.binding as ItemConfigTextBinding
                    holder.itemTitle.text = item.name
                    holder.itemValue.text = item.value
                }
            }
        }
    }
}
