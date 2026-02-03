package com.example.clicker

import android.animation.ValueAnimator
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.random.Random

class FloatingPointsAnimator(private val context: Context) {

    /**
     * Показывает анимацию всплывающих очков
     * @param anchorView - виджет, относительно которого появляются очки (флаг)
     * @param points - количество очков для отображения
     * @param rootView - корневой View для добавления анимации
     */
    fun show(anchorView: View, points: Int, rootView: ViewGroup) {
        // Создаем TextView для отображения очков
        val popup = createPointsTextView(points)
        
        // Добавляем TextView в корневой View
        rootView.addView(popup)
        
        // Ждем отрисовки, чтобы получить правильные размеры
        popup.post {
            // Рассчитываем начальную позицию
            val (x, y) = calculateStartPosition(popup, anchorView)
            
            // Устанавливаем позицию
            popup.x = x
            popup.y = y
            
            // Настраиваем случайные параметры анимации
            val rotation = Random.nextFloat() * 10 - 5 // от -5 до 5 градусов
            val horizontalSpeed = Random.nextFloat() * 40 - 20 // от -20 до 20 пикселей
            
            popup.rotation = rotation
            
            // Запускаем анимацию
            startAnimation(popup, horizontalSpeed, rootView)
        }
    }

    /**
     * Создает TextView для отображения очков
     */
    private fun createPointsTextView(points: Int): TextView {
        return TextView(context).apply {
            text = "+$points"
            setTextColor(context.resources.getColor(R.color.success_color))
            textSize = 24f
            typeface = typeface // Используем текущий шрифт
            setShadowLayer(6f, 2f, 2f, 0x88000000.toInt())
            alpha = 0f
            
            // Параметры layout
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
        }
    }

    /**
     * Рассчитывает начальную позицию для очков
     */
    private fun calculateStartPosition(popup: TextView, anchorView: View): Pair<Float, Float> {
        val popupWidth = popup.measuredWidth
        val popupHeight = popup.measuredHeight

        // Получаем глобальные координаты anchorView
        val anchorLocation = IntArray(2)
        anchorView.getLocationOnScreen(anchorLocation)
        
        // Получаем глобальные координаты корневого View
        val rootLocation = IntArray(2)
        (popup.parent as View).getLocationOnScreen(rootLocation)
        
        // Размеры anchorView
        val anchorWidth = anchorView.width
        val anchorHeight = anchorView.height
        
        // Центральная точка anchorView
        val centerX = anchorLocation[0] + anchorWidth / 2
        val centerY = anchorLocation[1] + anchorHeight / 2
        
        // Конвертируем в координаты относительно корневого View
        val centerXRelative = centerX - rootLocation[0]
        val centerYRelative = centerY - rootLocation[1]
        
        // Случайное отклонение от центра
        val maxDeviationX = anchorWidth * 0.4f
        val maxDeviationY = anchorHeight * 0.4f
        
        val deviationX = Random.nextFloat() * maxDeviationX * 2 - maxDeviationX
        val deviationY = Random.nextFloat() * maxDeviationY * 2 - maxDeviationY
        
        // Финальная позиция с учетом размера текста
        val finalX = centerXRelative + deviationX - popupWidth / 2
        val finalY = centerYRelative + deviationY - popupHeight / 2
        
        return finalX to finalY
    }

    /**
     * Запускает анимацию очков
     */
    private fun startAnimation(popup: TextView, horizontalSpeed: Float, rootView: ViewGroup) {
        // Анимация появления
        popup.animate()
            .alpha(1f)
            .setDuration(150)
            .withEndAction {
                // Анимация движения
                animateMovement(popup, horizontalSpeed, rootView)
            }
            .start()
    }

    /**
     * Анимация движения очков
     */
    private fun animateMovement(popup: TextView, horizontalSpeed: Float, rootView: ViewGroup) {
        val startX = popup.x
        val startY = popup.y
        
        // Создаем аниматор для плавного движения
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200L
            interpolator = AccelerateInterpolator()
            
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                
                // Вертикальное движение с ускорением
                val verticalOffset = -progress * 250f
                
                // Горизонтальное движение с замедлением
                val horizontalOffset = horizontalSpeed * (1 - progress)
                
                // Прозрачность уменьшается во второй половине анимации
                val alpha = if (progress < 0.5f) 1f else 2f * (1 - progress)
                
                popup.x = startX + horizontalOffset
                popup.y = startY + verticalOffset
                popup.alpha = alpha
                
                // Масштаб: сначала немного увеличивается, потом уменьшается
                val scale = 1f + 0.2f * Math.sin(progress * Math.PI).toFloat()
                popup.scaleX = scale
                popup.scaleY = scale
            }
            
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationCancel(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
                
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Удаляем View после завершения анимации
                    rootView.removeView(popup)
                }
            })
        }
        
        animator.start()
    }
}