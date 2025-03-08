package com.nakaharadev.roleworld.ui

import android.animation.ValueAnimator
import com.nakaharadev.roleworld.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.EditText
import androidx.core.content.ContextCompat


class AuthInputView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyleAttr) {
    private val Int.dp: Int
        get() {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

    private val Float.dp: Float
        get() {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this,
                context.resources.displayMetrics
            )
        }

    private val Int.sp: Int
        get() {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                this.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

    private val Float.sp: Float
        get() {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                this,
                context.resources.displayMetrics
            )
        }

    /* attrs */
    private val icon: Drawable?
    private val hint: String

    /* paints */
    private val hintPaint = Paint()

    /* hint text positions */
    private val hintPos = TextPos()
    private val hintPosFocused = TextPos()
    private val hintPosAnimated = TextPos()

    /* text sizes */
    private val hintTitledTextSize = 14f.sp

    /* "is" variables */
    private var isInitiated = false
    private var isFilled = false
    private var isError = false

    /* error data */
    private var errorAutoClear = false

    private var onInputCallback: () -> Unit = {}

    /* password visibility */
    private var isPasswordInput = false
    private var passwordIsVisible = false
    private var passwordVisibilityIconRect = Rect()
    private val passwordVisibilityShow: Drawable
    private val passwordVisibilityHide: Drawable

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AuthInputView)

        icon = attributes.getDrawable(R.styleable.AuthInputView_icon)
        hint = attributes.getString(R.styleable.AuthInputView_hint) ?: ""

        attributes.recycle()

        setBackgroundResource(R.drawable.auth_input_background)

        passwordVisibilityShow = resources.getDrawable(R.drawable.show_password)
        passwordVisibilityHide = resources.getDrawable(R.drawable.hide_password)

        hintPaint.color = resources.getColor(R.color.hint)
        hintPaint.textSize = textSize
    }

    fun setError(autoClear: Boolean = true) {
        isError = true
        errorAutoClear = autoClear

        animateIconColor(resources.getColor(R.color.dark_hint), resources.getColor(R.color.red))
    }

    fun setOnInputCallback(callback: () -> Unit) {
        onInputCallback = callback
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text?.isNotEmpty() == true)
            isFilled = true

        super.setText(text, type)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        animateHint(focused)
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (errorAutoClear) {
            isError = false
            errorAutoClear = false

            animateIconColor(resources.getColor(R.color.red), resources.getColor(R.color.dark_hint))
        }

        if (onInputCallback != null)
            onInputCallback()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (
                    event.x > passwordVisibilityIconRect.left &&
                    event.y > passwordVisibilityIconRect.top &&
                    event.x < passwordVisibilityIconRect.right &&
                    event.y < passwordVisibilityIconRect.bottom
                ) {
                    inputType = if (!passwordIsVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    passwordIsVisible = !passwordIsVisible
                    invalidate()

                    return true
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInitiated) {
            hintPos.set(paddingStart + 10f.dp, 43f.dp)
            hintPosFocused.set(paddingStart.toFloat(), 15f.dp)
            hintPosAnimated.set(
                if (isFilled)
                    hintPosFocused
                else
                    hintPos
            )

            if (inputType and (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) != 1) {
                isPasswordInput = true
                setPadding(paddingLeft, paddingTop, 50.dp, paddingBottom)
            }

            passwordVisibilityIconRect = Rect(width - 35.dp, 27.dp, width - 10.dp, height - 7.dp)

            isInitiated = true
        }

        if (isFilled && text.isNotEmpty())
            hintPosAnimated.set(hintPosFocused)

        super.onDraw(canvas)

        icon?.setBounds(10.dp, 25.dp, 40.dp, height - 5.dp)
        icon?.draw(canvas)

        if (isPasswordInput) {
            if (passwordIsVisible) {
                passwordVisibilityHide.setBounds(
                    passwordVisibilityIconRect.left,
                    passwordVisibilityIconRect.top,
                    passwordVisibilityIconRect.right,
                    passwordVisibilityIconRect.bottom
                )
                passwordVisibilityHide.draw(canvas)
            } else {
                passwordVisibilityShow.setBounds(
                    passwordVisibilityIconRect.left,
                    passwordVisibilityIconRect.top,
                    passwordVisibilityIconRect.right,
                    passwordVisibilityIconRect.bottom
                )
                passwordVisibilityShow.draw(canvas)
            }
        }

        canvas.drawText(hint, hintPosAnimated.x, hintPosAnimated.y, hintPaint)
    }

    private fun animateHint(focused: Boolean) {
        if (text.isNotEmpty())
            return

        val animator = if (focused) {
            ValueAnimator.ofFloat(1.0f, 0.0f)
        } else {
            ValueAnimator.ofFloat(0.0f, 1.0f)
        }
        animator.duration = 300
        animator.addUpdateListener {
            hintPosAnimated.set(
                hintPosFocused.x + (hintPos.x - hintPosFocused.x) * it.animatedValue as Float,
                hintPosFocused.y + (hintPos.y - hintPosFocused.y) * it.animatedValue as Float
            )

            hintPaint.textSize = hintTitledTextSize + (textSize - hintTitledTextSize) * it.animatedValue as Float

            postInvalidate()
        }

        animator.start()
    }

    private fun animateIconColor(startColor: Int, endColor: Int) {
        val animator = ValueAnimator.ofArgb(startColor, endColor)
        animator.duration = 300
        animator.addUpdateListener {
            icon?.setTint(it.animatedValue as Int)

            postInvalidate()
        }
        animator.start()
    }

    private class TextPos(var x: Float = 0f, var y: Float = 0f) {
        fun set(x: Float, y: Float) {
            this.x = x
            this.y = y
        }

        fun set(pos: TextPos) {
            x = pos.x
            y = pos.y
        }
    }
}