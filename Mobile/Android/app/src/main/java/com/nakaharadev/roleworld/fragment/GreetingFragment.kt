package com.nakaharadev.roleworld.fragment

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.config.Config
import com.nakaharadev.roleworld.ui.GreetingTextView

class GreetingFragment : DefaultFragment(R.layout.greeting_layout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showGreetingText()
    }

    private fun showGreetingText() {
        val greetingText = findViewById<GreetingTextView>(R.id.greeting_text)
        greetingText.showText("Привет, ${Config.nickname}")
        findViewById<RelativeLayout>(R.id.greeting_text_layout).setOnClickListener {
            greetingText.newText("Давай настроим твой аккаунт")
        }
    }
}