package com.nakaharadev.roleworld.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import com.nakaharadev.roleworld.util.printCharactersWithDelay


open class DelayShowTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : TextView(context, attrs, defStyleAttr, defStyleRes) {
    fun showText(text: String) {
        printCharactersWithDelay(text, 50) {
            post {
                setText(it)
            }
        }
    }
}