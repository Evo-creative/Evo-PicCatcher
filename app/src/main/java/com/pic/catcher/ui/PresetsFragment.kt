package com.pic.catcher.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pic.catcher.R
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.bean.PresetItem
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.databinding.FragmentPresetsBinding
import com.pic.catcher.databinding.ItemPresetBinding

class PresetsFragment : BaseFragment() {
    private lateinit var binding: FragmentPresetsBinding
    private lateinit var mAdapter: PresetsAdapter
    private val allPresets = PresetItem.getAllPresets()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPresetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        mAdapter = PresetsAdapter(allPresets.toMutableList()) { preset ->
            applyPreset(preset)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterPresets(s?.toString() ?: "")
            }
        })
    }

    private fun filterPresets(query: String) {
        val filtered = if (query.isEmpty()) {
            allPresets
        } else {
            allPresets.filter { 
                it.name.contains(query, true) || 
                it.apps.any { app -> app.contains(query, true) } || 
                it.category.contains(query, true) 
            }
        }
        mAdapter.updateData(filtered)
    }

    private fun applyPreset(preset: PresetItem) {
        val config = ModuleConfig.getInstance()
        
        // 1. 获取所有配置 Key
        val details = preset.details
        
        // 2. 直接操作 source JSONObject 确保 100% 应用成功
        val source = config.source
        
        // 预设是覆盖式的，我们先清理掉所有旧的抓取开关（可选，但这里我们按 details 提供的来）
        // 为了确保“确定性”，我们遍历 details 中的每一项进行覆盖
        details.forEach { (key, value) ->
            source.put(key, value)
        }

        config.save()
        Toast.makeText(context, getString(R.string.presets_applied_toast, preset.name) + "\n" + getString(R.string.toast_plugin_config_change_tip), Toast.LENGTH_LONG).show()
    }

    inner class PresetsAdapter(
        private var data: MutableList<PresetItem>,
        private val onApply: (PresetItem) -> Unit
    ) : RecyclerView.Adapter<PresetsAdapter.ViewHolder>() {

        fun updateData(newData: List<PresetItem>) {
            data.clear()
            data.addAll(newData)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemPresetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.binding.apply {
                tvPresetName.text = item.name
                chipCategory.text = item.category
                tvDescription.text = item.description
                tvApps.text = holder.itemView.context.getString(R.string.presets_apps_format, item.apps.joinToString(", "))
                
                // 设置详情内容
                tvDetails.text = item.getDetailText()
                detailsContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                
                // 箭头动画与 MD3 风格同步
                btnExpand.setIconResource(if (item.isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more)
                btnExpand.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(position)
                }

                btnApply.setOnClickListener { onApply(item) }
            }
        }

        override fun getItemCount(): Int = data.size

        inner class ViewHolder(val binding: ItemPresetBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
