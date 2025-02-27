package com.nakaharadev.roleworld.config

import com.nakaharadev.roleworld.util.json
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object Config {
    var userName = ""
    var id = ""
    var showId = ""

    fun load(file: File) {
        val reader = FileReader(file)

        val json = JSONObject(reader.readText())
        userName = json.getString("userName")
        id = json.getString("userName")
        showId = json.getString("showId")
    }

    fun write(file: File) {
        val writer = FileWriter(file)
        writer.write(toString())
        writer.flush()
    }

    override fun toString(): String {
        return json(
            "userName" to userName,
            "id" to id,
            "showId" to showId
        ).toString(4)
    }
}