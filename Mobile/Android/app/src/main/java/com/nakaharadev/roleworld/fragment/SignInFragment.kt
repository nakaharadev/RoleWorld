package com.nakaharadev.roleworld.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.nakaharadev.roleworld.R

class SignInFragment(@LayoutRes layout: Int, private val callback: (String) -> Unit) : DefaultFragment(layout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickableTextSpan()
    }

    private fun initClickableTextSpan() {
        val newAccountHint = findViewById<TextView>(R.id.create_new_account_hint)
        newAccountHint.movementMethod = LinkMovementMethod.getInstance()

        val spannable = SpannableString(newAccountHint.text)
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    callback("to_sign_up")
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = ds.linkColor
                    ds.isUnderlineText = false
                }
            },
            newAccountHint.text.length - "создать аккаунт".length,
            newAccountHint.text.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        newAccountHint.text = spannable
    }
}