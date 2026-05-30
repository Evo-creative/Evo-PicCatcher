package com.pic.catcher.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.pic.catcher.R
import com.pic.catcher.base.BaseActivity
import com.pic.catcher.config.ModuleConfig
import com.pic.catcher.databinding.LayoutMainBinding
import com.pic.catcher.ui.view.ResolutionGuideView
import com.pic.catcher.util.RootUtil
import com.lu.magic.util.thread.AppExecutor

class MainActivity : BaseActivity() {
    private lateinit var binding: LayoutMainBinding
    private var resolutionGuideView: ResolutionGuideView? = null
    private var onResolutionGuideDismissed: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        checkRootOnStart()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(HomeFragment::class.java)
                R.id.nav_settings -> switchFragment(SettingsFragment::class.java)
                R.id.nav_presets -> switchFragment(PresetsFragment::class.java)
            }
            true
        }
        // 默认选中首页
        if (supportFragmentManager.fragments.isEmpty()) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun switchFragment(fragmentClass: Class<out Fragment>) {
        val tag = fragmentClass.name
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.primaryNavigationFragment
        val newFragment = supportFragmentManager.findFragmentByTag(tag) ?: fragmentClass.newInstance()

        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }

        if (!newFragment.isAdded) {
            transaction.add(R.id.mainContainer, newFragment, tag)
        } else {
            transaction.show(newFragment)
        }

        transaction.setPrimaryNavigationFragment(newFragment)
        transaction.setReorderingAllowed(true)
        transaction.commitNow()
    }

    private fun checkRootOnStart() {
        // 使用正确的 io 线程执行
        AppExecutor.io().execute {
            val config = ModuleConfig.getInstance()
            val result = RootUtil.checkRootStatus()
            if (config.shellAuthType.isEmpty() && result.status == RootUtil.Status.AUTHORIZED) {
                config.shellAuthType = "Root"
            }
            config.save()
        }
    }

    fun showResolutionGuide(width: Int, height: Int, onDismiss: (() -> Unit)? = null) {
        if (resolutionGuideView != null) {
            hideResolutionGuide()
            return
        }
        
        this.onResolutionGuideDismissed = onDismiss
        val navHeight = binding.bottomNavigation.height
        
        val guideView = ResolutionGuideView(this).apply {
            id = View.generateViewId()
            
            // 使用 CoordinatorLayout 参数实现居中并避开底部
            val lp = CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.CENTER
            // 抵消导航栏高度，确保“视觉居中”
            lp.bottomMargin = navHeight
            layoutParams = lp

            // 设置高层级阴影
            elevation = 32f
            translationZ = 32f

            setResolution(width, height)
            // 设置移动边界限制
            setBottomInset(navHeight)
            
            alpha = 0f
            scaleX = 0.85f
            scaleY = 0.85f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in))
                .start()
        }
        
        binding.coordinatorLayout.addView(guideView)
        guideView.bringToFront() // 强制到最前
        resolutionGuideView = guideView
        
        guideView.setOnClickListener {
            hideResolutionGuide()
        }
    }

    fun hideResolutionGuide() {
        val view = resolutionGuideView ?: return
        resolutionGuideView = null 
        
        view.animate()
            .alpha(0f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(250)
            .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in))
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.coordinatorLayout.removeView(view)
                    onResolutionGuideDismissed?.invoke()
                    onResolutionGuideDismissed = null
                }
            })
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resolutionGuideView = null
    }
}
