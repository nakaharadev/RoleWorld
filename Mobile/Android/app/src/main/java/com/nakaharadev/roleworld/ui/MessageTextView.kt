package com.nakaharadev.roleworld.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView

class MessageTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : TextView(context, attrs, defStyleAttr, defStyleRes) {
    private var shouldDecompose = false

    var text: String
        set(value) {
            shouldDecompose = true
            setText(value)
        }
        get() { return getText().toString() }

    private fun decompose() {
        var result = ""

        val lines = getText().split('\n')
        for (line in lines) {
            if (paint.measureText(line) < width)
                result += "$line${if (lines.size > 1) "\n" else ""}"
            else {
                val split = line.split(' ')
                var newLine = ""
                for (word in split) {
                    if (paint.measureText("$newLine $word") > width) {
                        result += "${newLine.trim()}\n"
                        newLine = word
                    } else {
                        newLine += " $word"
                    }
                }

                result += newLine.trim()
            }
        }

        setText(result)
    }

    override fun onDraw(canvas: Canvas) {
        if (shouldDecompose) {
            decompose()
            shouldDecompose = false
        }

        super.onDraw(canvas)
    }
}