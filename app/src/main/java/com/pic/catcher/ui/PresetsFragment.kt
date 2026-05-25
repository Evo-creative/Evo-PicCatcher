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
        val source = config.source

        // 所有拦截开关的 Key 列表，用于在应用预设前进行全局清理
        val catchKeys = listOf(
            "isCatchSkiaPic", "isCatchNativeBitmapPic", "isCatchCanvasPic",
            "isCatchSurfacePic", "isCatchRenderNodePic", "isCatchHardwareRendererPic",
            "isCatchTextureViewPic", "isCatchWebViewPic", "isCatchComposePic",
            "isCatchFlutterPic", "isCatchReactNativePic", "isCatchLithoPic",
            "isCatchNetPic", "isCatchBitmapPic", "isCatchGlidePic", "isCatchCoilPic",
            "isCatchFrescoPic", "isCatchPicassoPic", "isCatchFilePic", "isCatchMoviePic",
            "isCatchDrawablePic", "isCatchImageViewPic", "isCatchImageDecoderPic"
        )

        // 1. 先将所有已知的抓取开关设为 false (互斥清理)
        catchKeys.forEach { key ->
            source.put(key, false)
        }

        // 2. 根据预设详情，开启需要的开关
        preset.details.forEach { (key, value) ->
            source.put(key, value)
        }

        // 3. 同步对象状态并持久化
        config.updateFrom(source)
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
