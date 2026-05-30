package com.pic.catcher.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.google.android.material.color.MaterialColors
import com.pic.catcher.util.ext.dp
import kotlin.math.max
import kotlin.math.min

/**
 * 分辨率指引框 (MD3 风格)
 * 支持拖动，边缘限制，仅描边中空设计。
 */
class ResolutionGuideView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var originalWidth: Int = 0
    private var originalHeight: Int = 0
    private var displayWidth: Int = 0
    private var displayHeight: Int = 0
    private var isExceedingScreen = false

    // 主描边画笔
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f.dp
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f.dp
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f.dp
        textAlign = Paint.Align.CENTER
        alpha = 180
    }

    private var lastX = 0f
    private var lastY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isDragging = false
    private var bottomInset = 0

    fun setBottomInset(inset: Int) {
        this.bottomInset = inset
        // 重新请求布局以应用新的边界
        requestLayout()
    }

    private val rect = RectF()

    fun setResolution(width: Int, height: Int) {
        this.originalWidth = width
        this.originalHeight = height
        
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        
        val screenW = displayMetrics.widthPixels
        val screenH = displayMetrics.heightPixels
        
        isExceedingScreen = width > screenW || height > screenH
        
        if (isExceedingScreen) {
            // 超出屏幕时的提示框大小
            displayWidth = 280f.dp.toInt()
            displayHeight = 120f.dp.toInt()
        } else {
            // 正常预览：缩放以便在屏幕上显示，同时保持比例
            val scale = min(0.85f, min(screenW.toFloat() / width, screenH.toFloat() / height))
            displayWidth = (width * scale).toInt()
            displayHeight = (height * scale).toInt()
        }
        
        layoutParams?.let {
            it.width = displayWidth
            it.height = displayHeight
        }
        requestLayout() // 确保强制父容器重新测量，以保持 Gravity.CENTER 居中
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                lastX = event.rawX
                lastY = event.rawY
                isDragging = false
                isPressed = true
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                
                if (Math.abs(event.rawX - startX) > 10 || Math.abs(event.rawY - startY) > 10) {
                    isDragging = true
                }
                
                var newX = translationX + dx
                var newY = translationY + dy
                
                // 边界限制 (限制在父布局内)
                val parentView = parent as? View
                if (parentView != null) {
                    val halfW = width / 2f
                    val halfH = height / 2f
                    val parentW = parentView.width
                    val parentH = parentView.height
                    
                    val centerX = parentW / 2f
                    val centerY = parentH / 2f
                    
                    val minX = -centerX + halfW
                    val maxX = centerX - halfW
                    val minY = -centerY + halfH
                    // 修正：将底部导航栏高度从最大可移动高度中扣除
                    val maxY = (parentH - bottomInset) / 2f - halfH
                    
                    newX = max(minX, min(maxX, newX))
                    newY = max(minY, min(maxY, newY))
                }

                translationX = newX
                translationY = newY

                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.action == MotionEvent.ACTION_UP && !isDragging) {
                    performClick()
                }
                isDragging = false
                isPressed = false
                invalidate()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas) {
        val color = if (isExceedingScreen) {
            // 使用标准的 android.R.attr.colorError 或兜底红色
            MaterialColors.getColor(context, android.R.attr.colorError, android.graphics.Color.RED)
        } else {
            MaterialColors.getColor(context, android.R.attr.colorPrimary, android.graphics.Color.BLUE)
        }
        
        paint.color = color
        textPaint.color = color
        hintPaint.color = color
        fillPaint.color = color

        if (isExceedingScreen) {
            drawExceedingWarning(canvas)
            return
        }

        // MD3 容器标准圆角：28dp
        val radius = 28f.dp
        rect.set(paint.strokeWidth, paint.strokeWidth, width - paint.strokeWidth, height - paint.strokeWidth)

        // 1. 绘制触摸反馈背景 (半透明填充)
        if (isPressed || isDragging) {
            fillPaint.alpha = 40 // 约 15% 透明度
            canvas.drawRoundRect(rect, radius, radius, fillPaint)
        }
        
        // 2. 绘制中空的框 (仅描边，不填充)
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    private fun drawExceedingWarning(canvas: Canvas) {
        // 警告框圆角稍微加大以示区别：32dp
        val radius = 32f.dp
        rect.set(paint.strokeWidth, paint.strokeWidth, width - paint.strokeWidth, height - paint.strokeWidth)
        
        // 警告框同样采用中空设计，仅描边
        canvas.drawRoundRect(rect, radius, radius, paint)
        
        val title = "分辨率超出屏幕，无法预览"
        val hint = "点击此处关闭"
        
        val centerY = height / 2f
        canvas.drawText(title, width / 2f, centerY - 5f.dp, textPaint)
        canvas.drawText(hint, width / 2f, centerY + 15f.dp, hintPaint)
    }
}
