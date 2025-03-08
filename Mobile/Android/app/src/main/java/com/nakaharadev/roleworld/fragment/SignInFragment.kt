package com.nakaharadev.roleworld.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.LayoutRes
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.network.data.request.AuthRequest
import com.nakaharadev.roleworld.network.data.response.AuthResponse
import com.nakaharadev.roleworld.network.task.AuthTask
import com.nakaharadev.roleworld.service.NetworkService
import com.nakaharadev.roleworld.ui.AuthInputView

class SignInFragment(@LayoutRes layout: Int, private val callback: (String, Any?) -> Unit) : DefaultFragment(layout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickableTextSpan()
        initOnInputCallback()
        initDoneButton()
    }

    private fun initOnInputCallback() {
        val flipper = findViewById<ViewFlipper>(R.id.sign_in_error_flipper)

        findViewById<AuthInputView>(R.id.sign_in_login_input).setOnInputCallback {
           if (flipper.displayedChild != 0)
               flipper.displayedChild = 0
        }

        findViewById<AuthInputView>(R.id.sign_in_password_input).setOnInputCallback {
            if (flipper.displayedChild != 0)
                flipper.displayedChild = 0
        }
    }

    private fun initDoneButton() {
        val login = findViewById<AuthInputView>(R.id.sign_in_login_input)
        val password = findViewById<AuthInputView>(R.id.sign_in_password_input)

        findViewById<TextView>(R.id.sign_in_done).setOnClickListener {
            if (
                login.text.isEmpty()        ||
                password.text.isEmpty()
            ) {
                if (login.text.isEmpty()) login.setError()
                if (password.text.isEmpty()) password.setError()

                findViewById<TextView>(R.id.sign_in_error_msg).text = "Не все поля заполнены"
                findViewById<ViewFlipper>(R.id.sign_in_error_flipper).displayedChild = 1

                return@setOnClickListener
            }

            Log.i("SignIn", "Auth")

            NetworkService.addTask(AuthTask(AuthTask.SIGN_IN_MODE, AuthRequest.SignIn(
                login.text.toString(),
                password.text.toString()
            ))) {
                it as AuthResponse

                if (it.status != 200) {
                    if (it.statusStr == "User not found") {
                        activity?.runOnUiThread {
                            login.setError()

                            findViewById<TextView>(R.id.sign_in_error_msg).text = "Пользователь не найден"
                            findViewById<ViewFlipper>(R.id.sign_in_error_flipper).displayedChild = 1
                        }
                    }
                    if (it.statusStr == "Invalid password") {

                        activity?.runOnUiThread {
                            password.setError()

                            findViewById<TextView>(R.id.sign_in_error_msg).text = "Неверный пароль"
                            findViewById<ViewFlipper>(R.id.sign_in_error_flipper).displayedChild = 1
                        }
                    }

                    return@addTask
                } else {
                    requireActivity().runOnUiThread { callback("sign_in", hashMapOf("nickname" to it.nickname, "id" to it.id)) }
                }
            }
        }
    }

    private fun initClickableTextSpan() {
        val newAccountHint = findViewById<TextView>(R.id.create_new_account_hint)
        newAccountHint.movementMethod = LinkMovementMethod.getInstance()

        val spannable = SpannableString(newAccountHint.text)
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    callback("to_sign_up", null)
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