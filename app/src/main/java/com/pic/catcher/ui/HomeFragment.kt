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
import com.pic.catcher.AppBuildInfo
import com.pic.catcher.BuildConfig
import com.pic.catcher.R
import com.pic.catcher.SelfHook
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.databinding.FragmentHomeBinding
import com.pic.catcher.databinding.ItemInfoRowBinding
import com.pic.catcher.util.ShellUtil
import rikka.shizuku.Shizuku

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
    }

    private fun initShellStatus() {
        val context = context ?: return
        val isShizukuAvailable = ShellUtil.isShizukuAvailable()
        val hasPermission = ShellUtil.hasShizukuPermission()
        val isSui = ShellUtil.isSui()

        if (hasPermission) {
            val colorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorTertiaryContainer, Color.LTGRAY)
            val onColorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnTertiaryContainer, Color.BLACK)

            binding.shellStatusCard.setCardBackgroundColor(onColorContainer)
            binding.ivShellStatusIcon.setImageResource(R.drawable.ic_icon_check)
            binding.ivShellStatusIcon.imageTintList = ColorStateList.valueOf(colorContainer)
            binding.tvShellStatusTitle.text = "Shell"
            binding.tvShellStatusTitle.setTextColor(colorContainer)
            binding.tvShellStatusDesc.text = if (isSui) "Sui 已授权" else "Shizuku 已授权"
            binding.tvShellStatusDesc.setTextColor(colorContainer)
            binding.shellStatusCard.setOnClickListener(null)
        } else {
            val colorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorErrorContainer, Color.RED)
            val onColorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnErrorContainer, Color.WHITE)

            binding.shellStatusCard.setCardBackgroundColor(colorContainer)
            binding.ivShellStatusIcon.setImageResource(R.drawable.ic_icon_warning)
            binding.ivShellStatusIcon.imageTintList = ColorStateList.valueOf(onColorContainer)
            
            if (isShizukuAvailable) {
                binding.tvShellStatusTitle.text = if (isSui) "Sui 未授权" else "Shizuku 未授权"
                binding.tvShellStatusDesc.text = "点击申请 Shell 授权"
            } else {
                binding.tvShellStatusTitle.text = "Shell 服务未运行"
                binding.tvShellStatusDesc.text = "请启动 Shizuku 或安装 Sui"
            }
            
            binding.tvShellStatusTitle.setTextColor(onColorContainer)
            binding.tvShellStatusDesc.setTextColor(onColorContainer)
            
            binding.shellStatusCard.setOnClickListener {
                if (isShizukuAvailable) {
                    try {
                        Shizuku.requestPermission(1001)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // 如果连服务都没开启，引导去 Shizuku 官网或提示
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/"))
                        startActivity(intent)
                    } catch (ignored: Exception) {}
                }
            }
        }
    }

    private fun initStatus() {
        val isActive = SelfHook.getInstance().isModuleEnable
        val context = activity ?: return
        
        // 动态获取应用版本号，这样就不用手动改了
        val versionName = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            BuildConfig.VERSION_NAME
        }

        if (isActive) {
            // 获取动态核心色
            val primaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY)
            val onPrimaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.BLACK)
            
            // 按照要求调换颜色：背景使用文字色(onPrimaryColor)，文字和图标使用容器色(primaryColor)
            binding.statusCard.setCardBackgroundColor(onPrimaryColor)
            binding.ivStatusIcon.setImageResource(R.drawable.ic_icon_check)
            binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.tvStatusTitle.text = "已激活"
            binding.tvStatusTitle.setTextColor(primaryColor)
            binding.tvVersion.setTextColor(primaryColor)
        } else {
            // 获取动态错误色
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
    }

    private fun initInfo() {
        // 设备型号: OnePlus KB2000 (OnePlus8T)
        val deviceBinding = ItemInfoRowBinding.bind(binding.itemDeviceModel.root)
        deviceBinding.tvLabel.text = "设备型号"
        deviceBinding.tvValue.text = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})"

        // 系统版本
        val systemBinding = ItemInfoRowBinding.bind(binding.itemSystemVersion.root)
        systemBinding.tvLabel.text = "系统版本"
        systemBinding.tvValue.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        // 代码分支
        val branchBinding = ItemInfoRowBinding.bind(binding.itemBranch.root)
        branchBinding.tvLabel.text = "代码分支"
        branchBinding.tvValue.text = buildInfo.branch

        // 构建时间: 2025-06-09 17:53:53 GMT+08:00
        val timeBinding = ItemInfoRowBinding.bind(binding.itemBuildTime.root)
        timeBinding.tvLabel.text = "构建时间"
        timeBinding.tvValue.text = buildInfo.buildTime
    }

    private fun initGithub() {
        binding.githubCard.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Evo-creative/Evo-PicCatcher"))
                startActivity(intent)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
