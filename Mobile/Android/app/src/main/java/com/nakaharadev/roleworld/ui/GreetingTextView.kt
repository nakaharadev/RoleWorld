package com.nakaharadev.roleworld.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.core.animation.addListener
import com.nakaharadev.roleworld.R

class GreetingTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : DelayShowTextView(context, attrs, defStyleAttr, defStyleRes) {
    fun newText(text: String) {
        animateTextChange(text)
    }

    private fun animateTextChange(newText: String) {
        var hideAnimator = ValueAnimator.ofArgb(
            resources.getColor(R.color.text),
            resources.getColor(R.color.window_background)
        )
        hideAnimator.duration = 300
        hideAnimator.addUpdateListener {
            setTextColor(it.animatedValue as Int)
        }

        hideAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                setTextColor(resources.getColor(R.color.text))

                showText(newText)
            }
        })

        hideAnimator.start()
    }
}