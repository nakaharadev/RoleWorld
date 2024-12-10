package com.nakaharadev.roleworld

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import android.widget.ViewFlipper
import androidx.annotation.IdRes
import com.nakaharadev.roleworld.controllers.CharacterDataController
import com.nakaharadev.roleworld.controllers.InnerNotificationsController
import com.nakaharadev.roleworld.controllers.MenuController
import com.nakaharadev.roleworld.file_tasks.ReadCharactersFileTask
import com.nakaharadev.roleworld.file_tasks.ReadImageFileTask
import com.nakaharadev.roleworld.file_tasks.SaveImageFileTask
import com.nakaharadev.roleworld.file_tasks.UpdateCharactersFileTask
import com.nakaharadev.roleworld.file_tasks.UpdateFriendsFileTask
import com.nakaharadev.roleworld.models.Character
import com.nakaharadev.roleworld.models.Friend
import com.nakaharadev.roleworld.models.OtherUser
import com.nakaharadev.roleworld.network.model.requests.ValueRequest
import com.nakaharadev.roleworld.network.model.responses.AddResponse
import com.nakaharadev.roleworld.network.model.responses.GetAvatarResponse
import com.nakaharadev.roleworld.network.model.responses.GetCharacterResponse
import com.nakaharadev.roleworld.network.model.responses.GetCharactersResponse
import com.nakaharadev.roleworld.network.model.responses.GetUserResponse
import com.nakaharadev.roleworld.network.tasks.GetUserTask
import com.nakaharadev.roleworld.network.model.responses.SearchResponse
import com.nakaharadev.roleworld.network.model.responses.UpdateResponse
import com.nakaharadev.roleworld.network.tasks.AddToFriendsTask
import com.nakaharadev.roleworld.network.tasks.GetAvatarTask
import com.nakaharadev.roleworld.network.tasks.GetCharacterTask
import com.nakaharadev.roleworld.network.tasks.GetCharactersTask
import com.nakaharadev.roleworld.network.tasks.SearchTask
import com.nakaharadev.roleworld.network.tasks.SendNewCharacterTask
import com.nakaharadev.roleworld.network.tasks.UpdateAvatarTask
import com.nakaharadev.roleworld.network.tasks.UpdateCharacterAvatarTask
import com.nakaharadev.roleworld.network.tasks.UpdateUserDataTask
import com.nakaharadev.roleworld.services.FileManagerService
import com.nakaharadev.roleworld.services.NetworkService
import com.nakaharadev.roleworld.ui.AnimatedImageView
import com.nakaharadev.roleworld.ui.CharacterLayout
import com.nakaharadev.roleworld.ui.FriendLayout
import com.nakaharadev.roleworld.ui.ImageClipperView
import com.nakaharadev.roleworld.ui.SearchUserLayout
import okhttp3.internal.http.HttpMethod
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AppActivity : Activity() {
    private val RESULT_GET_AVATAR = 1
    private val RESULT_GET_CHARACTER_AVATAR = 2
    private val RESULT_UPDATE_CHARACTER_AVATAR = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Converter.init(this)

        initGlobalVariables()

        startService(Intent(this, NetworkService::class.java))
        startService(Intent(this, FileManagerService::class.java))

        loadCharacters()
        syncCharacters()
        initMainView()
    }

    override fun onStart() {
        super.onStart()

        initBg()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return
        if (data == null) return

        if (requestCode == RESULT_GET_AVATAR) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

            loadUserAvatar(bitmap)
        } else if (requestCode == RESULT_GET_CHARACTER_AVATAR) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

            loadCharacterAvatar(bitmap)
        } else if (requestCode == RESULT_UPDATE_CHARACTER_AVATAR) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

            updateCharacterAvatar(bitmap)
        }
    }

    private fun initGlobalVariables() {
        GlobalVariablesContainer.add("cacheDir", cacheDir)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadCharacterAvatar(avatar: Bitmap) {
        val clipper = findViewById<ImageClipperView>(R.id.image_clipper)
        clipper.setImage(avatar)

        var xPos = 0f

        findViewById<RelativeLayout>(R.id.clipper_scale_swiper).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xPos = event.x

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    val delta = event.x - xPos
                    xPos = event.x

                    clipper.changeSize(delta.toInt() * 2)

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }

        findViewById<ImageView>(R.id.clipper_back_button).setOnClickListener {
            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 0
        }

        findViewById<ImageView>(R.id.clipper_done_button).setOnClickListener {
            val clipped = clipper.getClipped()

            findViewById<AnimatedImageView>(R.id.navbar_character_avatar)?.setImageBitmap(clipped)

            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 0
        }

        findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 1
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadUserAvatar(bitmap: Bitmap) {
        val clipper = findViewById<ImageClipperView>(R.id.image_clipper)
        clipper.setImage(bitmap)

        var xPos = 0f

        findViewById<RelativeLayout>(R.id.clipper_scale_swiper).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xPos = event.x

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    val delta = event.x - xPos
                    xPos = event.x

                    clipper.changeSize(delta.toInt() * 2)

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }

        findViewById<ImageView>(R.id.clipper_back_button).setOnClickListener {
            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 0
        }

        findViewById<ImageView>(R.id.clipper_done_button).setOnClickListener {
            val clipped = clipper.getClipped()

            if (UserData.roundedAvatar == null) {
                findViewById<AnimatedImageView>(R.id.profile_avatar).setImageBitmap(null, false)
            }

            UserData.roundedAvatar = clipped

            val loadBar = findViewById<ImageView>(R.id.load_avatar_bar)
            loadBar.visibility = View.VISIBLE
            if (loadBar.drawable is Animatable) {
                (loadBar.drawable as AnimatedVectorDrawable).start()
            }

            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 0

            updateAvatar(UserData.roundedAvatar!!)
        }

        findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 1
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateCharacterAvatar(avatar: Bitmap) {
        val clipper = findViewById<ImageClipperView>(R.id.image_clipper)
        clipper.setImage(avatar)

        var xPos = 0f

        findViewById<RelativeLayout>(R.id.clipper_scale_swiper).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xPos = event.x

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    val delta = event.x - xPos
                    xPos = event.x

                    clipper.changeSize(delta.toInt() * 2)

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }

        findViewById<ImageView>(R.id.clipper_back_button).setOnClickListener {
            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 2
        }

        findViewById<ImageView>(R.id.clipper_done_button).setOnClickListener {
            val clipped = clipper.getClipped()
            val character = CharacterDataController.characterObj

            NetworkService.addTask(UpdateCharacterAvatarTask(clipped, character)) {
                runOnUiThread {
                    findViewById<AnimatedImageView>(R.id.character_data_avatar).setImageBitmap(clipped)

                    InnerNotificationsController.showNotification(getString(R.string.done), InnerNotificationsController.DONE)

                    FileManagerService.addTask(SaveImageFileTask("${filesDir.path}/${character.name}_avatar.png", clipped))
                }
            }

            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 2
        }

        findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 1
    }

    private fun loadAvatar() {
        FileManagerService.addReadTask(ReadImageFileTask("${filesDir.path}/user_avatar.png")) {
            it as Bitmap? ?: return@addReadTask

            UserData.roundedAvatar = it
            runOnUiThread {
                findViewById<AnimatedImageView>(R.id.open_profile).setImageBitmap(UserData.roundedAvatar, false)
                findViewById<AnimatedImageView>(R.id.profile_avatar).setImageBitmap(UserData.roundedAvatar, false)
            }
        }
    }

    private fun loadCharacters() {
        FileManagerService.addReadTask(ReadCharactersFileTask(filesDir.path)) {
            it as Character

            UserData.characters[it.id] = it
            UserData.charactersId.add(it.id)
            runOnUiThread {
                addCharacterToUI(it)
            }
        }
    }

    private fun syncCharacters() {
        NetworkService.addTask(GetCharactersTask(UserData.id)) { getResponse ->
            getResponse as GetCharactersResponse

            val charactersList = ArrayList<String>()
            if (charactersList.isEmpty() || charactersList[0].isEmpty())
                return@addTask

            for (elem in getResponse.characters.split(" ")) {
                charactersList.add(elem)
            }

            UserData.charactersId = charactersList

            for (id in UserData.charactersId) {
                if (UserData.characters[id] == null) {
                    val response = App.networkApi.getCharacter(id).execute().body()!!

                    val character = Character()
                    character.id = id
                    character.name = response.name
                    character.sex = response.sex
                    character.desc = response.desc
                    character.bio = response.bio

                    NetworkService.addTask(GetAvatarTask(id, GetAvatarTask.AVATAR_TYPE_CHARACTER)) {
                        it as GetAvatarResponse

                        character.avatar = it.avatar

                        UserData.characters[character.id] = character

                        FileManagerService.addTask(UpdateCharactersFileTask("${filesDir.path}/characters.json", character))
                        FileManagerService.addTask(SaveImageFileTask("${filesDir.path}/${character.name}_avatar.png", character.avatar!!))

                        runOnUiThread {
                            addCharacterToUI(character)
                        }
                    }
                }
            }
        }
    }

    private fun initBg() {
        val view = findViewById<VideoView>(R.id.main_layout_bg)
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

        view.setOnErrorListener { mp, what, extra ->
            if (extra and MediaPlayer.MEDIA_ERROR_UNSUPPORTED != 0) {
                Log.e("Video error", "unsupported")
            }
            if (extra and MediaPlayer.MEDIA_ERROR_IO != 0) {
                Log.e("Video error", "IO")
            }
            if (extra and MediaPlayer.MEDIA_ERROR_MALFORMED != 0) {
                Log.e("Video error", "malformed")
            }
            if (extra and MediaPlayer.MEDIA_ERROR_TIMED_OUT != 0) {
                Log.e("Video error", "timed_out")
            }

            Log.e("Video error", "mp=${mp} what=${what} extra=${extra}")
            return@setOnErrorListener true
        }

        view.setOnInfoListener { mp, what, extra ->
            Log.i("Video info", "mp=${mp} what=${what} extra=${extra}")
            return@setOnInfoListener true
        }

        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.auth_bg)
        view.setVideoURI(uri)
        view.setOnCompletionListener {
            view.start()
        }
        view.start()
    }

    private fun initMainView() {
        setContentView(R.layout.main_layout)

        loadAvatar()

        val colors = HashMap<Int, Int>()
        colors[InnerNotificationsController.DONE] = getColor(R.color.green)
        colors[InnerNotificationsController.ERROR] = getColor(R.color.red)

        InnerNotificationsController.init(this, findViewById(R.id.inner_notification_view), colors)

        for (id: String in UserData.charactersId) {
            val character = UserData.characters[id] ?: continue
            addCharacterToUI(character)
        }

        loadChats(isInit = true)
        initMenu()
    }

    private fun initMenu() {
        MenuController.setCallback(object: MenuController.MenuStateCallback() {
            override fun onBeforeChangeState(oldState: Int): HashMap<String, View>? {
                val map = HashMap<String, View>()
                map["menu"] = findViewById(R.id.menu_layout)
                map["main"] = findViewById(R.id.main_view_layout)
                map["navBar"] = findViewById(R.id.navigation_bar)
                map["bg"] = findViewById(R.id.main_layout_bg)

                if (oldState == MenuController.MENU_STATE_CLOSE) {
                    findViewById<ImageView>(R.id.menu_button).setImageDrawable(resources.getDrawable(R.drawable.menu_icon))
                } else {
                    findViewById<ImageView>(R.id.menu_button).setImageDrawable(resources.getDrawable(R.drawable.opened_menu_icon))
                }

                return map
            }

            override fun onChangeAnimValue(value: Float, viewsMap: HashMap<String, View>?) {
                viewsMap?.get("menu")?.translationX = value - MenuController.getMaxValue()

                var params = viewsMap?.get("main")?.layoutParams as LinearLayout.LayoutParams
                params.marginStart = (value + (value / (MenuController.getMaxValue() / Converter.dpToPx(10f)))).toInt()
                params.topMargin = value.toInt()
                params.marginEnd = (value / (MenuController.getMaxValue() / Converter.dpToPx(10f))).toInt()
                params.bottomMargin = (value / (MenuController.getMaxValue() / Converter.dpToPx(10f))).toInt()
                viewsMap["main"]?.layoutParams = params
                viewsMap["main"]?.background?.alpha = ((1.0f - (value / MenuController.getMaxValue() * .3f)) * 255).toInt()
                viewsMap["main"]?.setPadding(0, (MenuController.getMaxValue() - value).toInt(), 0, 0)

                params = viewsMap["navBar"]?.layoutParams as LinearLayout.LayoutParams
                params.marginStart = (value + (value / (MenuController.getMaxValue() / Converter.dpToPx(10f)))).toInt()
                params.bottomMargin = (value / (MenuController.getMaxValue() / Converter.dpToPx(10f))).toInt()
                params.marginEnd = (value / (MenuController.getMaxValue() / Converter.dpToPx(10f))).toInt()
                viewsMap["navBar"]?.layoutParams = params

                val drawable = (viewsMap["navBar"]?.background as StateListDrawable).getStateDrawable(0) as GradientDrawable
                val cornerValue = value / MenuController.getMaxValue() * Converter.dpToPx(15f)
                drawable.cornerRadii = floatArrayOf(Converter.dpToPx(15f), Converter.dpToPx(15f), Converter.dpToPx(15f), Converter.dpToPx(15f), cornerValue, cornerValue, cornerValue, cornerValue, Converter.dpToPx(15f), Converter.dpToPx(15f), Converter.dpToPx(15f), Converter.dpToPx(15f))

                viewsMap["bg"]?.alpha = value / MenuController.getMaxValue()
            }

            override fun onAfterChangeState(newState: Int, viewsMap: HashMap<String, View>?) {}
        })

        MenuController.addButtonCallback(R.id.open_profile, this::loadProfile)
        MenuController.addButtonCallback(R.id.open_chat, this::loadChats)
        MenuController.addButtonCallback(R.id.open_characters, this::loadCharactersView)
        MenuController.addButtonCallback(R.id.open_settings, this::loadSettings)

        MenuController.setMinAnimValue(0f)
        MenuController.setMaxAnimValue(70f)
        MenuController.setMenuLayout(findViewById(R.id.menu_layout))
        MenuController.setAnimDuration(300)
        MenuController.init(findViewById(R.id.menu_button))
    }

    private fun loadProfile() {
        findViewById<TextView>(R.id.profile_nickname).text = UserData.nickname
        findViewById<TextView>(R.id.profile_email).text = UserData.email

        val spannableStr = if (UserData.showId.isNotEmpty())
            SpannableString(UserData.showId)
        else
            SpannableString(UserData.id)

        spannableStr.setSpan(UnderlineSpan(), 0, spannableStr.length, 0)
        findViewById<TextView>(R.id.profile_id).text = spannableStr

        findViewById<TextView>(R.id.profile_id).setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("user_id", (it as TextView).text)
            clipboardManager.setPrimaryClip(clipData)
        }

        findViewById<TextView>(R.id.profile_id).setOnLongClickListener {
            showChangeIdDialog()

            return@setOnLongClickListener true
        }

        findViewById<ViewFlipper>(R.id.main_flipper).displayedChild = 1

        findViewById<TextView>(R.id.profile_account_exit).setOnClickListener {
            exitFromAccount()
        }

        findViewById<ImageView>(R.id.profile_avatar).setOnLongClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, RESULT_GET_AVATAR)

            return@setOnLongClickListener true
        }

        findViewById<EditText>(R.id.profile_nickname).setOnLongClickListener { nicknameView ->
            nicknameView as EditText

            nicknameView.isFocusable = true
            nicknameView.isFocusableInTouchMode = true
            nicknameView.isClickable = true

            nicknameView.setBackgroundResource(R.drawable.current_edit_profile_field)

            nicknameView.requestFocus()
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(nicknameView, InputMethodManager.SHOW_IMPLICIT)

            nicknameView.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    UserData.nickname = nicknameView.text.toString()

                    nicknameView.isFocusable = false
                    nicknameView.isFocusableInTouchMode = false
                    nicknameView.isClickable = false

                    nicknameView.setBackgroundColor(resources.getColor(R.color.transparent))

                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(nicknameView.windowToken, 0)

                    NetworkService.addTask(UpdateUserDataTask(
                        UserData.id,
                        UserData.nickname,
                        UpdateUserDataTask.UPDATE_TYPE_NICKNAME
                    )) {
                        val preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        val editor = preferences.edit()
                        editor.putString("nickname", UserData.nickname)
                        editor.apply()

                        runOnUiThread {
                            InnerNotificationsController.showNotification(
                                getString(R.string.done),
                                InnerNotificationsController.DONE
                            )
                        }
                    }
                }

                return@setOnEditorActionListener true
            }

            return@setOnLongClickListener true
        }
    }

    private fun showChangeIdDialog() {
        val flipper = findViewById<ViewFlipper>(R.id.navbar_flipper)
        val currentDisplayed = flipper.displayedChild
        flipper.displayedChild = 3

        val input = findViewById<EditText>(R.id.change_show_id_input)

        input.setBackgroundResource(R.color.transparent)
        if (UserData.showId.isNotEmpty()) {
            input.setText(UserData.showId)
        }

        findViewById<ImageView>(R.id.change_show_id_done).setOnClickListener {
            val value = input.text.toString()

            it as ImageView

            if (UserData.showId == value) {
                flipper.displayedChild = currentDisplayed
                return@setOnClickListener
            }

            it.setImageResource(R.drawable.round_load_bar)
            (it.drawable as AnimatedVectorDrawable).start()

            NetworkService.addTask(UpdateUserDataTask(
                UserData.id,
                value,
                UpdateUserDataTask.UPDATE_TYPE_SHOW_ID
            )) { response ->
                response as UpdateResponse
                if (response.status == 409) {
                    runOnUiThread {
                        input.setBackgroundResource(R.drawable.navbar_input_error)
                        input.setText("")
                        InnerNotificationsController.showNotification(getString(R.string.id_already_exists), InnerNotificationsController.ERROR)
                    }

                    return@addTask
                }

                val preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("show_id", value)
                editor.apply()

                runOnUiThread {
                    flipper.displayedChild = currentDisplayed

                    (it.drawable as AnimatedVectorDrawable).stop()
                    it.setImageResource(R.drawable.minim_done_icon)

                    val spannableStr = SpannableString(value)
                    spannableStr.setSpan(UnderlineSpan(), 0, spannableStr.length, 0)
                    findViewById<TextView>(R.id.profile_id).text = spannableStr

                    UserData.showId = value

                    InnerNotificationsController.showNotification(getString(R.string.done), InnerNotificationsController.DONE)
                }
            }
        }
    }

    private fun exitFromAccount() {
        val flipper = findViewById<ViewFlipper>(R.id.navbar_flipper)
        val currentDisplayedChild = flipper.displayedChild

        flipper.displayedChild = 2

        findViewById<ImageView>(R.id.navbar_account_exit_ok).setOnClickListener {
            val preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.remove("nickname")
            editor.apply()

            val avatarFile = File("${filesDir.path}/user_avatar.png")
            if (avatarFile.exists()) {
                avatarFile.delete()
            }

            for (id in UserData.charactersId) {
                val character = UserData.characters[id]

                File("${filesDir.path}/${character?.name}_avatar.png").delete()
            }

            File("${filesDir.path}/characters.json").delete()

            startActivity(Intent(this, LauncherActivity::class.java))
        }

        findViewById<ImageView>(R.id.navbar_account_exit_close).setOnClickListener {
            flipper.displayedChild = currentDisplayedChild
        }
    }

    private fun loadChats(isInit: Boolean = false) {
        if (!isInit) {
            findViewById<ViewFlipper>(R.id.main_flipper).displayedChild = 0
        }

        findViewById<ImageView>(R.id.new_chat).setOnClickListener {
            newChat()
        }
    }

    private fun newChat() {
        findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 3

        findViewById<LinearLayout>(R.id.new_chat_friends_list).removeAllViews()

        findViewById<ImageView>(R.id.close_new_chat).setOnClickListener {
            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 0
        }

        findViewById<ImageView>(R.id.new_chat_search).setOnClickListener {
            val value = findViewById<EditText>(R.id.new_chat_search_input).text.toString()

            findViewById<LinearLayout>(R.id.new_chat_friends_list).removeAllViews()

            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.search_indicator, null) as LinearLayout
            findViewById<LinearLayout>(R.id.new_chat_friends_list).addView(view)

            (findViewById<ImageView>(R.id.new_chat_search_load_indicator_loadbar)
                .drawable as AnimatedVectorDrawable).start()

            NetworkService.addTask(SearchTask(ValueRequest(value))) {
                it as SearchResponse

                val users = it.users
                if (users.isEmpty()) {
                    runOnUiThread {
                        findViewById<LinearLayout>(R.id.new_chat_friends_list).removeAllViews()
                    }

                    return@addTask
                }

                for (id in users) {
                    NetworkService.addTask(GetUserTask(id)) { response ->
                        response as GetUserResponse

                        if (response.avatar.isNotEmpty()) {
                            NetworkService.addTask(GetAvatarTask(id, GetAvatarTask.AVATAR_TYPE_USER)) { avatar ->
                                avatar as GetAvatarResponse

                                val user = OtherUser(
                                    id,
                                    response.showId,
                                    response.nickname,
                                    avatar.avatar,
                                    response.characters
                                )

                                runOnUiThread {
                                    addUserToSearchList(user)
                                }
                            }
                        } else {
                            val user = OtherUser(
                                id,
                                response.showId,
                                response.nickname,
                                null,
                                response.characters
                            )

                            runOnUiThread {
                                addUserToSearchList(user)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addUserToSearchList(user: OtherUser) {
        val list = findViewById<LinearLayout>(R.id.new_chat_friends_list)
        list.removeView(findViewById(R.id.new_chat_search_load_indicator))

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.search_user_layout, null) as SearchUserLayout
        view.setUserData(user)
        view.setOnClickListener {
            showUserData(user)
        }
        view.findViewById<ImageView>(R.id.search_user_avatar).setImageBitmap(user.avatar)
        view.findViewById<TextView>(R.id.search_user_name).text = user.nickname

        list.addView(view)
    }

    private fun showUserData(user: OtherUser) {
        findViewById<ViewFlipper>(R.id.new_chats_list_flipper).displayedChild = 1

        findViewById<ImageView>(R.id.close_user_data).setOnClickListener {
            findViewById<ViewFlipper>(R.id.new_chats_list_flipper).displayedChild = 0
        }

        if (user.avatar != null)
            findViewById<ImageView>(R.id.user_data_avatar).setImageBitmap(user.avatar)

        findViewById<TextView>(R.id.add_to_friends).setOnClickListener {
            addToFriends(user)
        }

        findViewById<TextView>(R.id.user_data_nickname).text = user.nickname
        findViewById<TextView>(R.id.user_data_id).text = user.showId.ifEmpty { user.id }

        if (user.characters.isNotEmpty()) {
            val list = findViewById<LinearLayout>(R.id.user_data_characters_list)
            list.removeAllViews()

            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.load_characters_indicator, null) as LinearLayout
            list.addView(view)

            (findViewById<ImageView>(R.id.load_characters_indicator_loadbar)
                .drawable as AnimatedVectorDrawable).start()

            for (id in user.characters.split(" ")) {
                NetworkService.addTask(GetCharacterTask(id)) {
                    it as GetCharacterResponse

                    val character = Character()
                    character.id = id
                    character.name = it.name
                    character.sex = it.sex
                    character.desc = it.desc
                    character.bio = it.bio

                    NetworkService.addTask(GetAvatarTask(id, GetAvatarTask.AVATAR_TYPE_CHARACTER)) { avatarResponse ->
                        avatarResponse as GetAvatarResponse

                        character.avatar = avatarResponse.avatar

                        runOnUiThread {
                            addCharacterToList(findViewById(R.id.user_data_characters_list), character, R.id.load_characters_indicator, null)
                        }
                    }
                }
            }
        }
    }

    private fun addToFriends(user: OtherUser) {
        NetworkService.addTask(AddToFriendsTask(UserData.id, user.id)) {
            val friend = Friend(user, true)
            UserData.friends.add(friend)

            FileManagerService.addTask(UpdateFriendsFileTask("${filesDir.path}/friends.json", friend, UpdateFriendsFileTask.UPDATE_MODE_ADD))
            if (user.avatar != null)
                FileManagerService.addTask(SaveImageFileTask("${filesDir.path}/${user.id}.png", user.avatar))

            runOnUiThread {
                addFriendToList(Friend(user, true))
            }
        }
    }

    private fun addFriendToList(friend: Friend) {
        val list = findViewById<LinearLayout>(R.id.profile_friends_list)

        val view = LayoutInflater.from(this).inflate(R.layout.friend, null) as FriendLayout
        view.setFriend(friend)
        view.findViewById<ImageView>(R.id.friend_avatar).setImageBitmap(friend.user.avatar)
        view.findViewById<TextView>(R.id.friend_name).text = friend.user.nickname

        view.setOnClickListener {

        }

        list.addView(view)
    }

    private fun getCharacterView(): CharacterLayout {
        return LayoutInflater.from(this).inflate(R.layout.character_view, null) as CharacterLayout
    }

    private fun addCharacterToList(list: LinearLayout, character: Character, @IdRes emptyViewId: Int, callback: (() -> Unit)?) {
        list.removeView(findViewById(emptyViewId))

        val view = getCharacterView()

        view.setCharacterId(character.id)
        if (callback != null) {
            view.setOnClickListener {
                callback()
            }
        }
        view.findViewById<ImageView>(R.id.character_avatar).setImageBitmap(character.avatar)
        view.findViewById<TextView>(R.id.character_name).text = character.name

        list.addView(view)
    }

    private fun loadCharactersView() {
        findViewById<ViewFlipper>(R.id.main_flipper).displayedChild = 2

        findViewById<ImageView>(R.id.new_character).setOnClickListener {
            val navBarFlipper = findViewById<ViewFlipper>(R.id.navbar_flipper)
            val currentDisplayed = navBarFlipper.displayedChild
            navBarFlipper.displayedChild = 1

            findViewById<AnimatedImageView>(R.id.navbar_character_avatar).setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, RESULT_GET_CHARACTER_AVATAR)
            }

            findViewById<EditText>(R.id.input_character_nickname).setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val character = Character()
                    character.name = v.text.toString()
                    character.avatar = (findViewById<ImageView>(R.id.navbar_character_avatar).drawable as BitmapDrawable).bitmap

                    NetworkService.addTask(SendNewCharacterTask(character)) {
                        it as AddResponse

                        character.id = it.id

                        FileManagerService.addTask(UpdateCharactersFileTask("${filesDir.path}/characters.json", character))
                        FileManagerService.addTask(SaveImageFileTask("${filesDir.path}/${character.name}_avatar.png", character.avatar!!))
                        UserData.characters[character.id] = character

                        runOnUiThread {
                            InnerNotificationsController.showNotification(
                                getString(R.string.done),
                                InnerNotificationsController.DONE
                            )
                            navBarFlipper.displayedChild = currentDisplayed
                            addCharacterToUI(character)
                        }
                    }
                }

                return@setOnEditorActionListener true
            }
        }
    }

    private fun addCharacterToUI(character: Character) {
        val list = findViewById<LinearLayout>(R.id.characters_list)
        val profileList = findViewById<LinearLayout>(R.id.profile_characters_list)

        addCharacterToList(list, character, R.id.characters_list_empty) {
            openCharacterData(character)
        }

        addCharacterToList(profileList, character, R.id.profile_characters_list_empty) {
            openCharacterData(character)
        }
    }

    private fun openCharacterData(character: Character) {
        findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 2

        findViewById<ImageView>(R.id.close_character_data).setOnClickListener {
            findViewById<ViewFlipper>(R.id.clipper_flipper).displayedChild = 0
        }

        CharacterDataController.init(this, findViewById(R.id.character_data_layout), character, this::runOnUiThread) { view, c ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, RESULT_UPDATE_CHARACTER_AVATAR)
        }
    }

    private fun loadSettings() {
        findViewById<ViewFlipper>(R.id.main_flipper).displayedChild = 3
    }

    private fun saveAvatar(bitmap: Bitmap) {
        Thread {
            val fos = FileOutputStream("${filesDir.path}/user_avatar.png")
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val data = bos.toByteArray()
            bos.close()

            fos.write(data)
            fos.flush()
            fos.close()
        }.start()
    }

    private fun updateAvatar(bitmap: Bitmap) {
        NetworkService.addTask(UpdateAvatarTask(bitmap)) {
            runOnUiThread {
                val loadBar = findViewById<ImageView>(R.id.load_avatar_bar)
                if (loadBar.drawable is Animatable) {
                    (loadBar.drawable as AnimatedVectorDrawable).stop()
                }
                loadBar.visibility = View.GONE

                findViewById<ImageView>(R.id.profile_avatar).setImageBitmap(UserData.roundedAvatar)
                findViewById<ImageView>(R.id.open_profile).setImageBitmap(UserData.roundedAvatar)

                saveAvatar(UserData.roundedAvatar!!)

                InnerNotificationsController.showNotification(
                    getString(R.string.done),
                    InnerNotificationsController.DONE
                )
            }
        }
    }
}