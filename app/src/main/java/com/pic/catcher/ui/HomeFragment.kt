package com.pic.catcher.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lu.magic.util.thread.AppExecutor
import com.pic.catcher.AppBuildInfo
import com.pic.catcher.BuildConfig
import com.pic.catcher.R
import com.pic.catcher.SelfHook
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.databinding.FragmentHomeBinding
import com.pic.catcher.databinding.ItemInfoRowBinding
import com.pic.catcher.util.RootUtil
import android.widget.Toast

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private val buildInfo = AppBuildInfo.of()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInfo()
        initGithub()
    }

    override fun onResume() {
        super.onResume()
        initStatus()
        initShellStatus()

        // 核心修复：如果配置显示已授权但当前进程尚未连接，静默尝试以刷新状态（特别是 suName）
        val config = ModuleConfig.getInstance()
        if (config.rootStatus == "AUTHORIZED" && !RootUtil.hasRootPermission()) {
            AppExecutor.io().execute {
                val result = RootUtil.checkRootStatus()
                if (result.status == RootUtil.Status.AUTHORIZED) {
                    activity?.runOnUiThread {
                        if (config.suManagerName != result.suName) {
                            config.suManagerName = result.suName
                            config.save()
                            initShellStatus()
                        }
                    }
                }
            }
        }
    }

    private fun initShellStatus() {
        val context = context ?: return
        val config = ModuleConfig.getInstance()
        
        // 动态探测并保存 SU 名称 (Magisk/KernelSU/APatch)
        val suName = if (config.suManagerName == "Root" || config.suManagerName.isEmpty()) {
            val detected = RootUtil.probeSuManagerName()
            if (detected != config.suManagerName) {
                config.suManagerName = detected
                config.save()
            }
            detected
        } else {
            config.suManagerName
        }
        
        val status = config.rootStatus
        val colorTertiaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorTertiaryContainer, Color.LTGRAY)
        val onColorTertiaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnTertiaryContainer, Color.BLACK)
        val colorErrorContainer = errorColorContainer(context)
        val onColorErrorContainer = onColorErrorContainer(context)

        // 设置动态标题
        binding.tvShellStatusTitle.text = suName

        when (status) {
            "AUTHORIZED" -> {
                binding.shellStatusCard.setCardBackgroundColor(onColorTertiaryContainer)
                binding.ivShellStatusIcon.setImageResource(R.drawable.ic_icon_check)
                binding.ivShellStatusIcon.imageTintList = ColorStateList.valueOf(colorTertiaryContainer)
                binding.tvShellStatusTitle.setTextColor(colorTertiaryContainer)
                binding.tvShellStatusDesc.text = "已授权"
                binding.tvShellStatusDesc.setTextColor(colorTertiaryContainer)
            }
            else -> {
                binding.shellStatusCard.setCardBackgroundColor(colorErrorContainer)
                binding.ivShellStatusIcon.setImageResource(R.drawable.ic_icon_warning)
                binding.ivShellStatusIcon.imageTintList = ColorStateList.valueOf(onColorErrorContainer)
                binding.tvShellStatusTitle.setTextColor(onColorErrorContainer)
                binding.tvShellStatusDesc.text = when(status) {
                    "DENIED" -> "已拒绝授权"
                    "NOT_FOUND" -> "未找到 SU 环境"
                    else -> "等待授权"
                }
                binding.tvShellStatusDesc.setTextColor(onColorErrorContainer)
            }
        }

        // 解压逻辑：点击仅播放水波纹，不执行操作
        binding.shellStatusCard.isClickable = true
        binding.shellStatusCard.setOnClickListener { } 
        // 长按触发授权选择（防误触）
        binding.shellStatusCard.setOnLongClickListener {
            showAuthSelectionDialog()
            true
        }
    }

    private fun errorColorContainer(context: android.content.Context) = 
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorErrorContainer, Color.RED)
    
    private fun onColorErrorContainer(context: android.content.Context) = 
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnErrorContainer, Color.WHITE)

    private fun showAuthSelectionDialog() {
        val items = arrayOf("Root 授权")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("手动触发授权")
            .setItems(items) { _, which ->
                if (which == 0) requestRoot()
            }
            .show()
    }

    private fun requestRoot() {
        Toast.makeText(context, "正在尝试请求 Root 权限...", Toast.LENGTH_SHORT).show()
        AppExecutor.io().execute {
            val result = RootUtil.checkRootStatus()
            activity?.runOnUiThread {
                val config = ModuleConfig.getInstance()
                config.rootStatus = result.status.name
                config.suManagerName = result.suName
                if (result.status == RootUtil.Status.AUTHORIZED) {
                    config.shellAuthType = "Root"
                    Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show()
                }
                config.save()
                initShellStatus()
            }
        }
    }

    private fun initStatus() {
        val isActive = SelfHook.getInstance().isModuleEnable
        val context = activity ?: return
        val versionName = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            BuildConfig.VERSION_NAME
        }

        if (isActive) {
            val primaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY)
            val onPrimaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.BLACK)
            binding.statusCard.setCardBackgroundColor(onPrimaryColor)
            binding.ivStatusIcon.setImageResource(R.drawable.ic_icon_check)
            binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.tvStatusTitle.text = "已激活"
            binding.tvStatusTitle.setTextColor(primaryColor)
            binding.tvVersion.setTextColor(primaryColor)
        } else {
            val errorColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorErrorContainer, Color.RED)
            val onErrorColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnErrorContainer, Color.WHITE)
            binding.statusCard.setCardBackgroundColor(errorColor)
            binding.ivStatusIcon.setImageResource(R.drawable.ic_icon_warning)
            binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(onErrorColor)
            binding.tvStatusTitle.text = "未激活"
            binding.tvStatusTitle.setTextColor(onErrorColor)
            binding.tvVersion.setTextColor(onErrorColor)
        }
        binding.tvVersion.text = "版本: $versionName"
        binding.statusCard.setOnClickListener { } // 纯解压动画
    }

    private fun initInfo() {
        val deviceBinding = ItemInfoRowBinding.bind(binding.itemDeviceModel.root)
        deviceBinding.tvLabel.text = "设备型号"
        deviceBinding.tvValue.text = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})"

        val systemBinding = ItemInfoRowBinding.bind(binding.itemSystemVersion.root)
        systemBinding.tvLabel.text = "系统版本"
        systemBinding.tvValue.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val branchBinding = ItemInfoRowBinding.bind(binding.itemBranch.root)
        branchBinding.tvLabel.text = "代码分支"
        branchBinding.tvValue.text = buildInfo.branch

        val timeBinding = ItemInfoRowBinding.bind(binding.itemBuildTime.root)
        timeBinding.tvLabel.text = "构建时间"
        timeBinding.tvValue.text = buildInfo.buildTime
    }

    private fun initGithub() {
        binding.githubCard.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Evo-creative/Evo-PicCatcher"))
                startActivity(intent)
            } catch (e: Exception) { }
        }
    }
}
