package com.nakaharadev.roleworld

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import com.nakaharadev.roleworld.animators.AuthAnimator
import com.nakaharadev.roleworld.file_tasks.SaveImageFileTask
import com.nakaharadev.roleworld.network.model.requests.AuthRequest
import com.nakaharadev.roleworld.network.model.responses.AuthResponse
import com.nakaharadev.roleworld.network.model.responses.GetAvatarResponse
import com.nakaharadev.roleworld.network.tasks.AuthTask
import com.nakaharadev.roleworld.network.tasks.GetAvatarTask
import com.nakaharadev.roleworld.services.FileManagerService
import com.nakaharadev.roleworld.services.NetworkService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream


class AuthActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.auth_layout)

        Converter.init(this)

        startService(Intent(this, NetworkService::class.java))
        startService(Intent(this, FileManagerService::class.java))

        initAuthChooser()
        initSignIn()
        initSignUp()
    }

    override fun onStart() {
        super.onStart()

        initAuthBg()
    }

    private fun initAuthBg() {
        val view = findViewById<VideoView>(R.id.auth_bg)
        view.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)

        view.setOnPreparedListener { mediaPlayer ->
            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = view.width / view.height.toFloat()
            val scaleX = videoRatio / screenRatio
            if (scaleX >= 1f) {
                view.scaleX = scaleX
            } else {
                view.scaleY = 1f / scaleX
            }
        }

        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.auth_bg)
        view.setVideoURI(uri)
        view.setOnCompletionListener {
            view.start()
        }
        view.start()
    }

    private fun initAuthChooser() {
        findViewById<TextView>(R.id.sign_in).setOnClickListener {
            AuthAnimator.chooserToAuth(
                findViewById(R.id.auth_chooser),
                findViewById(R.id.auth_layout),
                findViewById(R.id.sign_in_layout)
            )
        }

        findViewById<TextView>(R.id.sign_up).setOnClickListener {
            AuthAnimator.chooserToAuth(
                findViewById(R.id.auth_chooser),
                findViewById(R.id.auth_layout),
                findViewById(R.id.sign_up_layout),
                true
            )
        }

        findViewById<TextView>(R.id.sign_in_sign_up).setOnClickListener {
            AuthAnimator.signInToSignUp(
                findViewById(R.id.auth_layout),
                findViewById(R.id.sign_in_layout),
                findViewById(R.id.sign_up_layout)
            )
        }

        findViewById<TextView>(R.id.sign_up_sign_in).setOnClickListener {
            AuthAnimator.signUpToSignIn(
                findViewById(R.id.auth_layout),
                findViewById(R.id.sign_in_layout),
                findViewById(R.id.sign_up_layout)
            )
        }
    }

    private fun initSignIn() {
        var showPassword = false

        findViewById<ImageView>(R.id.sign_in_show_password).setOnClickListener {
            it as ImageView

            val passwordInput = findViewById<EditText>(R.id.sign_in_password)

            if (showPassword) {
                it.setImageDrawable(getDrawable(R.drawable.show_password))
                passwordInput.transformationMethod = PasswordTransformationMethod()
            } else {
                it.setImageDrawable(getDrawable(R.drawable.hide_password))
                passwordInput.transformationMethod = object : TransformationMethod {
                    override fun getTransformation(source: CharSequence?, view: View?): CharSequence { return source ?: "" }
                    override fun onFocusChanged(view: View?, sourceText: CharSequence?, focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {}
                }
            }

            passwordInput.setSelection(passwordInput.length())

            showPassword = !showPassword
        }

        findViewById<EditText>(R.id.sign_in_password).setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val email =  findViewById<EditText>(R.id.sign_in_email).text.toString()
                val password = v.text.toString()

                findViewById<LinearLayout>(R.id.auth_load_indicator).visibility = View.VISIBLE
                val loadBar = findViewById<ImageView>(R.id.auth_load_bar)
                if (loadBar.drawable is Animatable) {
                    (loadBar.drawable as AnimatedVectorDrawable).start()
                }

                val request = AuthRequest.SignIn(
                    email,
                    password
                )

                NetworkService.addTask(AuthTask(AuthTask.SIGN_IN_MODE, request)) {
                    it as AuthResponse

                    if (it.status == 404) {
                        AuthAnimator.showErrorMessage(
                            resources.getString(R.string.user_not_found),
                            resources.getDrawable(R.drawable.error_bg),
                            findViewById(R.id.sign_in_error),
                            listOf(findViewById(R.id.sign_in_email))
                        )
                    } else if (it.status == 506) {
                        AuthAnimator.showErrorMessage(
                            resources.getString(R.string.invalid_password),
                            resources.getDrawable(R.drawable.error_bg),
                            findViewById(R.id.sign_in_error),
                            listOf(findViewById(R.id.sign_in_password))
                        )
                    } else if (it.status == 200) {
                        UserData.id = it.userId

                        if (it.showId.isEmpty()) {
                            UserData.showId = UserData.id
                        } else {
                            UserData.showId = it.showId
                        }

                        UserData.nickname = it.nickname
                        UserData.password = password
                        UserData.email = email

                        loadUserAvatar()
                        runOnUiThread { finishAuth() }
                    }
                }
            }

            return@setOnEditorActionListener true
        }
    }

    private fun initSignUp() {
        var showPassword = false
        var showPasswordRepeat = false

        findViewById<ImageView>(R.id.sign_up_show_password).setOnClickListener {
            it as ImageView

            val passwordInput = findViewById<EditText>(R.id.sign_up_password)

            if (showPassword) {
                it.setImageDrawable(getDrawable(R.drawable.show_password))
                passwordInput.transformationMethod = PasswordTransformationMethod()
            } else {
                it.setImageDrawable(getDrawable(R.drawable.hide_password))
                passwordInput.transformationMethod = object : TransformationMethod {
                    override fun getTransformation(source: CharSequence?, view: View?): CharSequence { return source ?: "" }
                    override fun onFocusChanged(view: View?, sourceText: CharSequence?, focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {}
                }
            }

            passwordInput.setSelection(passwordInput.length())

            showPassword = !showPassword
        }

        findViewById<ImageView>(R.id.sign_up_show_repeat_password).setOnClickListener {
            it as ImageView

            val passwordInput = findViewById<EditText>(R.id.sign_up_repeat_password)

            if (showPasswordRepeat) {
                it.setImageDrawable(getDrawable(R.drawable.show_password))
                passwordInput.transformationMethod = PasswordTransformationMethod()
            } else {
                it.setImageDrawable(getDrawable(R.drawable.hide_password))
                passwordInput.transformationMethod = object : TransformationMethod {
                    override fun getTransformation(source: CharSequence?, view: View?): CharSequence { return source ?: "" }
                    override fun onFocusChanged(view: View?, sourceText: CharSequence?, focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {}
                }
            }

            passwordInput.setSelection(passwordInput.length())

            showPasswordRepeat = !showPasswordRepeat
        }
        findViewById<EditText>(R.id.sign_up_repeat_password).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                findViewById<LinearLayout>(R.id.auth_load_indicator).visibility = View.VISIBLE
                val loadBar = findViewById<ImageView>(R.id.auth_load_bar)
                if (loadBar.drawable is Animatable) {
                    (loadBar.drawable as AnimatedVectorDrawable).start()
                }

                val password = findViewById<TextView>(R.id.sign_up_password)
                val repeatPassword = findViewById<TextView>(R.id.sign_up_repeat_password)

                if (password.text.toString() != repeatPassword.text.toString()) {
                    password.text = ""
                    repeatPassword.text = ""

                    AuthAnimator.showErrorMessage(
                        resources.getString(R.string.passwords_not_equals),
                        resources.getDrawable(R.drawable.error_bg),
                        findViewById(R.id.sign_up_error),
                        listOf(
                            findViewById(R.id.sign_up_password),
                            findViewById(R.id.sign_up_repeat_password)
                        )
                    )

                    return@setOnEditorActionListener false
                }

                UserData.nickname = findViewById<EditText>(R.id.sign_up_nickname).text.toString()
                UserData.email = findViewById<EditText>(R.id.sign_up_email).text.toString()
                UserData.password = password.text.toString()


                val request = AuthRequest.SignUp(
                    UserData.nickname,
                    UserData.email,
                    UserData.password
                )

                NetworkService.addTask(AuthTask(AuthTask.SIGN_UP_MODE, request)) {
                    it as AuthResponse

                    if (it.status == 506) {
                        runOnUiThread {
                            AuthAnimator.showErrorMessage(
                                resources.getText(R.string.user_already_exists).toString(),
                                resources.getDrawable(R.drawable.error_bg),
                                findViewById(R.id.sign_up_error),
                                listOf(findViewById(R.id.sign_up_email))
                            )
                        }
                    } else {
                        UserData.id = it.userId
                        UserData.showId = it.userId

                        runOnUiThread { finishAuth() }
                    }
                }
            }
            return@setOnEditorActionListener true
        }
    }

    private fun loadUserAvatar() {
        NetworkService.addTask(GetAvatarTask(UserData.id, GetAvatarTask.AVATAR_TYPE_USER)) {
            it as GetAvatarResponse

            UserData.roundedAvatar = it.avatar

            saveAvatarToFile()
        }
    }

    private fun saveAvatarToFile() {
        FileManagerService.addTask(SaveImageFileTask("${filesDir.path}/user_avatar.png", UserData.roundedAvatar!!))
    }

    private fun finishAuth() {
        val preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("user_id", UserData.id)
        editor.putString("show_id", UserData.showId)
        editor.putString("nickname", UserData.nickname)
        editor.putString("email", UserData.email)
        editor.putString("password", UserData.password)
        editor.apply()

        startActivity(Intent(this, LauncherActivity::class.java))
    }
}