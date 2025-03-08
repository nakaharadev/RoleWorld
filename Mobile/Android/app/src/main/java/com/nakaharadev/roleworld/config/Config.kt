package com.nakaharadev.roleworld.config

import com.nakaharadev.roleworld.util.json
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object Config {
    var nickname = ""
    var id = ""
    var theme = ""
    var lang = ""

    fun load(file: File) {
        val reader = FileReader(file)

        val json = JSONObject(reader.readText())

        nickname = json.getString("nickname")
        id = json.getString("id")
        theme = json.getString("theme")
        lang = json.getString("lang")
    }

    fun write(file: File) {
        val writer = FileWriter(file)
        writer.write(toString())
        writer.flush()
    }

    override fun toString(): String {
        return json(
            "nickname" to nickname,
            "id" to id,
            "theme" to theme,
            "lang" to lang,
        ).toString(4)
    }
}