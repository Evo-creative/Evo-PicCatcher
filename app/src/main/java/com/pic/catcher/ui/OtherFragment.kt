package com.pic.catcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pic.catcher.base.BaseFragment
import com.pic.catcher.databinding.FragmentLogBinding
import java.io.File

class OtherFragment : BaseFragment() {
    private lateinit var binding: FragmentLogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLogs()
    }

    private fun refreshLogs() {
        try {
            val logFile = File(activity?.getExternalFilesDir(null), "logs.txt")
            if (logFile.exists()) {
                // 读取所有行，只取最后 80 行
                val lines = logFile.readLines()
                val last80Lines = if (lines.size > 80) {
                    " (仅展示最后 80 行日志)\n" + "...\n" + lines.takeLast(80).joinToString("\n")
                } else {
                    lines.joinToString("\n")
                }
                
                binding.tvLogs.text = last80Lines
                
                // 默认滚动到末尾
                binding.verticalScrollView.post {
                    binding.verticalScrollView.fullScroll(View.FOCUS_DOWN)
                }
            } else {
                binding.tvLogs.text = "暂无日志文件。"
            }
        } catch (e: Exception) {
            binding.tvLogs.text = "无法读取日志: ${e.message}"
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLogs()
    }
}
