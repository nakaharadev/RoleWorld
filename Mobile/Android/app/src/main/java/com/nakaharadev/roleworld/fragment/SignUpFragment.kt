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
import com.nakaharadev.roleworld.ui.AuthInputView

class SignUpFragment(@LayoutRes layout: Int, private val callback: (String) -> Unit) : DefaultFragment(layout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToSignInBtn()
        initClickableTextSpan()
        initDoneButton()
    }

    private fun initDoneButton() {
        val nickname = findViewById<AuthInputView>(R.id.sign_up_nickname_input)
        val login = findViewById<AuthInputView>(R.id.sign_up_login_input)
        val password = findViewById<AuthInputView>(R.id.sign_up_password_input)
        val repeatPassword = findViewById<AuthInputView>(R.id.sign_up_repeat_password_input)

        findViewById<TextView>(R.id.sign_up_done).setOnClickListener {
            if (nickname.text.isEmpty()) nickname.setError()
            if (login.text.isEmpty()) login.setError()
            if (password.text.isEmpty()) password.setError()
            if (repeatPassword.text.isEmpty()) repeatPassword.setError()

            if (password.text != repeatPassword.text) {
                password.setError()
                repeatPassword.setError()
            }
        }
    }

    private fun initToSignInBtn() {
        findViewById<TextView>(R.id.auth_to_sign_in).setOnClickListener {
            callback("to_sign_in")
        }
    }

    private fun initClickableTextSpan() {
        val newAccountHint = findViewById<TextView>(R.id.create_new_account_hint)
        newAccountHint.movementMethod = LinkMovementMethod.getInstance()

        val spannable = SpannableString(newAccountHint.text)
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {

                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = ds.linkColor
                    ds.isUnderlineText = false
                }
            },
            newAccountHint.text.length - "пользовательское соглашение".length,
            newAccountHint.text.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        newAccountHint.text = spannable
    }
}