package com.nakaharadev.roleworld.network.tasks

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.nakaharadev.roleworld.App
import com.nakaharadev.roleworld.GlobalVariablesContainer
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.UserData
import com.nakaharadev.roleworld.models.Character
import com.nakaharadev.roleworld.network.model.AbstractRequest
import com.nakaharadev.roleworld.network.model.AbstractResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SendNewCharacterTask(private val character: Character) : AbstractTask() {
    override lateinit var callback: (AbstractResponse) -> Unit

    @Throws(Exception::class)
    override fun task() {
        val character = Character()
        character.name = this.character.name
        character.avatar = this.character.avatar

        val cacheDir = GlobalVariablesContainer.get("cacheDir") as File? ?: return

        val cache = File("${cacheDir.path}/character_avatar")
        cache.createNewFile()

        val bos = ByteArrayOutputStream()
        character.avatar?.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val data = bos.toByteArray()

        val fos = FileOutputStream(cache)
        fos.write(data)
        fos.flush()
        fos.close()

        val requestFile = cache.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("character_avatar", cache.name, requestFile)

        val response = App.networkApi.addCharacter(UserData.id, character.name, body).execute()
        callback(response.body()!!)
    }
}