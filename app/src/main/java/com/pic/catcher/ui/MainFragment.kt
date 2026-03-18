package com.pic.catcher.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.kxt.toElseString
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
import com.lu.magic.util.thread.AppExecutor
import com.pic.catcher.BuildConfig
import com.pic.catcher.R
import com.pic.catcher.adapter.AbsListAdapter
import com.pic.catcher.adapter.CommonListAdapter
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.base.LifecycleAutoViewBinding
import com.pic.catcher.config.AppConfigUtil
import com.pic.catcher.databinding.FragmentMainBinding
import com.pic.catcher.databinding.ItemIconTextBinding
import com.pic.catcher.plugin.PicExportManager
import com.pic.catcher.route.AppRouter
import com.pic.catcher.util.ext.layoutInflate


class MainFragment : BaseFragment() {
    private var itemBinding: ItemIconTextBinding by LifecycleAutoViewBinding<MainFragment, ItemIconTextBinding>()
    private var mainBinding: FragmentMainBinding by LifecycleAutoViewBinding<MainFragment, FragmentMainBinding>()
    private val buildInfo = com.pic.catcher.AppBuildInfo.of()

    private val donateCardId = 10086

    private var mListAdapter: CommonListAdapter<Int, ItemBindingViewHolder>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentMainBinding.inflate(inflater, container, false).let {
            mainBinding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rippleRadius = SizeUtil.dp2px(resources, 8f).toInt()

        mListAdapter = object : CommonListAdapter<Int, ItemBindingViewHolder>() {
            init {
                setData(arrayListOf(1, 2, 3))
            }


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemBindingViewHolder {
                itemBinding = ItemIconTextBinding.inflate(parent.layoutInflate, parent, false)

                return object : ItemBindingViewHolder(itemBinding) {
                    init {
                        itemBinding.layoutItem.setOnClickListener {
                            val itemValue = getItem(layoutPosition)

                            when (itemValue) {
                                1 -> {
                                    clickModuleCard()
                                }

                                2 -> {
                                    TipViewUtil.showLong(context, getString(R.string.app_module_des_format, PicExportManager.getInstance().exportDir.parent))
                                }

                                3 -> {
                                    AppRouter.routeConfigPage(context)
                                }
                            }
                        }

                    }
                }
            }

            override fun onBindViewHolder(vh: ItemBindingViewHolder, position: Int, parent: ViewGroup) {
                val itemValue = getItem(position)
                if (itemValue != 0) {
                    applyCommonItemRipple(vh.binding.layoutItem)
                }

                // 彻底移除提交哈希 (Sub3)
                vh.binding.tvItemTitleSub3.visibility = View.GONE

                val context = vh.itemView.context
                
                // 彻底解决 Unresolved reference 问题：使用底层反射方案查找属性 ID
                // 这种方案不依赖任何 R 类文件引用，100% 解决编译红线
                fun getAttrId(attrName: String): Int {
                    // 优先从 Material 库查找，其次从系统查找
                    var id = context.resources.getIdentifier(attrName, "attr", "com.google.android.material")
                    if (id == 0) id = context.resources.getIdentifier(attrName, "attr", "android")
                    return id
                }

                fun resolveColor(attrName: String, fallback: Int): Int {
                    val attrId = getAttrId(attrName)
                    if (attrId == 0) return fallback
                    val typedValue = TypedValue()
                    return if (context.theme.resolveAttribute(attrId, typedValue, true)) {
                        if (typedValue.resourceId != 0) context.getColor(typedValue.resourceId) else typedValue.data
                    } else {
                        fallback
                    }
                }

                // 动态获取 M3 核心颜色
                val colorPrimary = resolveColor("colorPrimary", Color.BLUE)
                val colorOnPrimary = resolveColor("colorOnPrimary", Color.WHITE)
                val colorOnSurface = resolveColor("colorOnSurface", Color.BLACK)
                val colorOnSurfaceVariant = resolveColor("colorOnSurfaceVariant", Color.GRAY)
                val colorSurfaceContainerLow = resolveColor("colorSurfaceContainerLow", Color.LTGRAY)

                if (itemValue == 1) {
                    // 主状态卡片：背景设为系统主色 (colorPrimary)
                    vh.binding.layoutItem.setCardBackgroundColor(colorPrimary)
                    
                    vh.binding.tvItemEvoTitle.visibility = View.VISIBLE
                    
                    // 背景为主色，文字和图标使用对应的反色 (colorOnPrimary)
                    vh.binding.tvItemTitle.setTextColor(colorOnPrimary)
                    vh.binding.ivItemIcon.imageTintList = ColorStateList.valueOf(colorOnPrimary)
                    vh.binding.tvItemEvoTitle.setTextColor(colorOnPrimary)
                    vh.binding.tvItemTitleSub.setTextColor(colorOnPrimary)
                    vh.binding.tvItemTitleSub2.setTextColor(colorOnPrimary)
                    vh.binding.tvItemTitleSub4.setTextColor(colorOnPrimary)
                    
                    vh.binding.tvItemTitleSub2.text = getString(R.string.app_code_branch, buildInfo.branch)
                    vh.binding.tvItemTitleSub4.text = getString(R.string.app_build_time_format, buildInfo.buildTime)
                    vh.binding.tvItemTitleSub2.visibility = View.VISIBLE
                    vh.binding.tvItemTitleSub4.visibility = View.VISIBLE
                } else {
                    // 普通卡片：默认浅色动态背景
                    vh.binding.layoutItem.setCardBackgroundColor(colorSurfaceContainerLow)
                    
                    vh.binding.tvItemEvoTitle.visibility = View.GONE
                    vh.binding.tvItemTitleSub2.visibility = View.GONE
                    vh.binding.tvItemTitleSub4.visibility = View.GONE
                    
                    // 恢复图标颜色为主色
                    vh.binding.ivItemIcon.imageTintList = ColorStateList.valueOf(colorPrimary)
                    
                    // 恢复文字对比色：标题用主文本色，提示用次级文本色
                    vh.binding.tvItemTitle.setTextColor(colorOnSurface)
                    vh.binding.tvItemTitleSub.setTextColor(colorOnSurfaceVariant)
                }

                when (itemValue) {
                    1 -> {
                        if (com.pic.catcher.SelfHook.getInstance().isModuleEnable) {
                            vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_check)
                            vh.binding.tvItemTitle.setText(R.string.module_have_active)
                            applyModuleStateRipple(vh.binding.layoutItem, true)
                        } else {
                            vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_warning)
                            vh.binding.tvItemTitle.setText(R.string.module_not_active)
                            applyModuleStateRipple(vh.binding.layoutItem, false)
                        }
                        vh.binding.tvItemTitleSub.text = (getString(R.string.module_version) + "：" + getVersionText())
                    }

                    2 -> {
                        vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_des)
                        vh.binding.tvItemTitle.setText(R.string.app_use_help)
                        vh.binding.tvItemTitleSub.setText(R.string.click_here_to_des)
                    }

                    3 -> {
                        vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_edit)
                        vh.binding.tvItemTitle.setText(R.string.app_config)
                        vh.binding.tvItemTitleSub.setText(R.string.click_here_to_edit_config)
                    }

                    donateCardId -> {
                        val donateCard = AppConfigUtil.config.mainUi?.donateCard
                        vh.binding.tvItemTitle.text = donateCard?.title.toElseString(
                            getString(R.string.donate)
                        )
                        vh.binding.tvItemTitleSub.text = donateCard?.des.toElseString(
                            getString(R.string.donate_description)
                        )
                        vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_dollar)
                    }

                }

            }

            private fun applyCommonItemRipple(v: View) {
                RectangleRippleBuilder(Color.TRANSPARENT, Color.GRAY, rippleRadius).let {
                    RippleApplyUtil.apply(v, it)
                }
            }

            private fun applyModuleStateRipple(v: View, enable: Boolean) {
                val contentColor = if (enable) {
                    view.context.getColor(R.color.app_primary)
                } else {
                    0xFFFF6027.toInt()
                }
                RectangleRippleBuilder(contentColor, Color.GRAY, rippleRadius).let {
                    RippleApplyUtil.apply(v, it)
                }
            }

        }

        mainBinding.listView.adapter = mListAdapter
    }

    private fun clickModuleCard() {
        val moduleCard = AppConfigUtil.config.mainUi?.moduleCard
        if (moduleCard == null || moduleCard.link.isNullOrBlank()) {
            AppRouter.routeReleasesNotePage(activity, "更新日记")
        } else {
            AppRouter.route(activity, moduleCard.link)
        }
    }

    private fun showDonateCard() {
        AppExecutor.executeMain {
            mListAdapter?.let { adapter ->
                if (adapter.dataList.contains(donateCardId)) {
                    return@executeMain
                }
                adapter.addData(donateCardId)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getVersionText(): String {
        return BuildConfig.VERSION_NAME
    }

    open class ItemBindingViewHolder(@JvmField var binding: ItemIconTextBinding) :
        AbsListAdapter.ViewHolder(binding.root)


}
