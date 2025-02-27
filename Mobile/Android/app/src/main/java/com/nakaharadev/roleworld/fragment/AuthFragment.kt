package com.nakaharadev.roleworld.fragment

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.ui.builder.createMessageView
import com.nakaharadev.roleworld.util.json

class AuthFragment : DefaultFragment(R.layout.auth_layout) {
    private lateinit var layout: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout = findViewById(R.id.auth_messages_layout)

        initChat()
    }

    private fun initChat() {
        findViewById<ImageView>(R.id.auth_send_message).setOnClickListener {
            onSetBackgroundModeCallback("video")

            val input = findViewById<EditText>(R.id.auth_message_input)

            var view = createMessageView(
                requireActivity(),
                json(
                    "nameColor" to resources.getColor(R.color.main_ui_2),
                    "name" to "Ты",
                    "text" to input.text,
                    "side" to "right"
                )
            )
            layout.addView(view)

            if (input.text.toString().lowercase().contains("""сук|бля|хуй|пизд""".toRegex(RegexOption.IGNORE_CASE))) {
                view = createMessageView(
                    requireActivity(),
                    json(
                        "nameColor" to resources.getColor(R.color.main_ui_1),
                        "name" to "Ники",
                        "text" to "Так.. давай без мата",
                        "side" to "left"
                    )
                )
                layout.addView(view)
            } else if (
                input.text.toString().lowercase().contains("кто") &&
                input.text.toString().lowercase().contains("ты") &&
                input.text.split(' ').size == 2) {
                view = createMessageView(
                    requireActivity(),
                    json(
                        "nameColor" to resources.getColor(R.color.main_ui_1),
                        "name" to "Ники",
                        "text" to "Я? Ники",
                        "side" to "left"
                    )
                )
                layout.addView(view)
            }

            input.text.clear()
        }

        helloMessage()
    }

    private fun helloMessage() {
        val view = createMessageView(
            requireActivity(),
            json(
                "nameColor" to resources.getColor(R.color.main_ui_1),
                "name" to "Ники",
                "text" to "Привет!\nТы тут впервые, или хочешь вернуться?",
                "side" to "left",
                "buttons" to arrayOf(
                    json(
                        "text" to "Я нью"
                    ),
                    json(
                        "text" to "Я олд"
                    )
                ),
                "buttonsDirection" to "horizontal"
            )
        )
        layout.addView(view)
    }

    companion object {
        lateinit var onAuthCallback: (settingsSet: AuthSettingsSet) -> Unit
        lateinit var onSetBackgroundModeCallback: (mode: String) -> Unit

        data class AuthSettingsSet(
            val userName: String,
            val email: String,
            val password: String,
            val theme: String,
            val lang: String
        )
    }
}