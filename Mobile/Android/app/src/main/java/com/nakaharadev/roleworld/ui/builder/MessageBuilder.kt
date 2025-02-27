package com.nakaharadev.roleworld.ui.builder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.ui.MessageTextView
import org.json.JSONObject

fun createMessageView(context: Context, json: JSONObject): View {
    val inflater = LayoutInflater.from(context)
    val view = if (json.getString("side") == "right")
        inflater.inflate(R.layout.right_message_view, LinearLayout(context))
    else
        inflater.inflate(R.layout.left_message_view, LinearLayout(context))

    view.findViewById<MessageTextView>(R.id.message_text).text = json.getString("text")
    view.findViewById<TextView>(R.id.sender_name).text = json.getString("name")
    view.findViewById<TextView>(R.id.sender_name).setTextColor(json.getInt("nameColor"))

    return view
}