package com.nakaharadev.roleworld.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.LayoutRes
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.network.data.request.AuthRequest
import com.nakaharadev.roleworld.network.data.response.AuthResponse
import com.nakaharadev.roleworld.network.task.AuthTask
import com.nakaharadev.roleworld.service.NetworkService
import com.nakaharadev.roleworld.ui.AuthInputView

class SignUpFragment(@LayoutRes layout: Int, private val callback: (String, Any?) -> Unit) : DefaultFragment(layout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToSignInBtn()
        initClickableTextSpan()
        initOnInputCallback()
        initDoneButton()
    }

    private fun initOnInputCallback() {
        val flipper = findViewById<ViewFlipper>(R.id.sign_up_error_flipper)

        findViewById<AuthInputView>(R.id.sign_up_nickname_input).setOnInputCallback {
            if (flipper.displayedChild != 0)
                flipper.displayedChild = 0
        }

        findViewById<AuthInputView>(R.id.sign_up_login_input).setOnInputCallback {
            if (flipper.displayedChild != 0)
                flipper.displayedChild = 0
        }

        findViewById<AuthInputView>(R.id.sign_up_password_input).setOnInputCallback {
            if (flipper.displayedChild != 0)
                flipper.displayedChild = 0
        }

        findViewById<AuthInputView>(R.id.sign_up_repeat_password_input).setOnInputCallback {
            if (flipper.displayedChild != 0)
                flipper.displayedChild = 0
        }
    }

    private fun initDoneButton() {
        val nickname = findViewById<AuthInputView>(R.id.sign_up_nickname_input)
        val login = findViewById<AuthInputView>(R.id.sign_up_login_input)
        val password = findViewById<AuthInputView>(R.id.sign_up_password_input)
        val repeatPassword = findViewById<AuthInputView>(R.id.sign_up_repeat_password_input)

        findViewById<TextView>(R.id.sign_up_done).setOnClickListener {
            if (
                nickname.text.isEmpty()     ||
                login.text.isEmpty()        ||
                password.text.isEmpty()     ||
                repeatPassword.text.isEmpty()
            ) {
                if (nickname.text.isEmpty()) nickname.setError()
                if (login.text.isEmpty()) login.setError()
                if (password.text.isEmpty()) password.setError()
                if (repeatPassword.text.isEmpty()) repeatPassword.setError()

                findViewById<TextView>(R.id.sign_up_error_msg).text = "Не все поля заполнены"
                findViewById<ViewFlipper>(R.id.sign_up_error_flipper).displayedChild = 1

                return@setOnClickListener
            }

            if (password.text.toString() != repeatPassword.text.toString()) {
                password.setError()
                repeatPassword.setError()

                findViewById<TextView>(R.id.sign_up_error_msg).text = "Пароли не совпадают"
                findViewById<ViewFlipper>(R.id.sign_up_error_flipper).displayedChild = 1

                return@setOnClickListener
            }

            NetworkService.addTask(
                AuthTask(
                    AuthTask.SIGN_UP_MODE,
                    AuthRequest.SignUp(
                        nickname.text.toString(),
                        login.text.toString(),
                        password.text.toString()
                    )
                )
            ) {
                it as AuthResponse

                if (it.status != 200) {
                    if (it.statusStr == "User already exists") {
                        activity?.runOnUiThread {
                            login.setError()

                            findViewById<TextView>(R.id.sign_up_error_msg).text = "Пользователь уже существует"
                            findViewById<ViewFlipper>(R.id.sign_up_error_flipper).displayedChild = 1
                        }
                    }

                    return@addTask
                } else {
                    requireActivity().runOnUiThread { callback("sign_up", hashMapOf("nickname" to it.nickname, "id" to it.id)) }
                }
            }
        }
    }

    private fun initToSignInBtn() {
        findViewById<TextView>(R.id.auth_to_sign_in).setOnClickListener {
            callback("to_sign_in", null)
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