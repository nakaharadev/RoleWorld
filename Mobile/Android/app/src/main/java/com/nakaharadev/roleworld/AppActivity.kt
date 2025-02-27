package com.nakaharadev.roleworld

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity
import com.nakaharadev.roleworld.config.Config
import com.nakaharadev.roleworld.fragment.AuthFragment
import java.io.File

class AppActivity : FragmentActivity() {
    private lateinit var backgroundMediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.app)

        if (!configIsExists()) {
            initialLaunch()
        } else {
            Config.load(getConfigFile())
        }

        prepareWindowBackground()
        //findViewById<VideoView>(R.id.auth_bg).start()
    }

    override fun onStart() {
        //findViewById<VideoView>(R.id.auth_bg).start()

        super.onStart()
    }

    private fun prepareWindowBackground() {
        val view = findViewById<VideoView>(R.id.auth_bg)
        view.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)

        view.setOnPreparedListener { mediaPlayer ->
            backgroundMediaPlayer = mediaPlayer

            val videoRatio = backgroundMediaPlayer.videoWidth /
                    backgroundMediaPlayer.videoHeight.toFloat()

            val screenRatio = view.width / view.height.toFloat()
            val scaleX = videoRatio / screenRatio
            if (scaleX >= 1f) {
                view.scaleX = scaleX
            } else {
                view.scaleY = 1f / scaleX
            }

            backgroundMediaPlayer.start()
            backgroundMediaPlayer.pause()
        }

        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.dark_background)
        view.setVideoURI(uri)
        view.setOnCompletionListener {
            view.start()
        }

        AuthFragment.onSetBackgroundModeCallback = {
            if (it == "video")
                backgroundMediaPlayer.start()
        }
    }

    private fun initialLaunch() {
        AuthFragment.onAuthCallback = {

        }
    }

    /**
     * check main_conf.json file and create if not exists
     * @return `true` - if config is exists
     * `false` - in any other case
     */
    private fun configIsExists(): Boolean {
        try {
            val file = getConfigFile()
            if (file.exists()) return true

            file.createNewFile()

            return false
        } catch (e: Exception) {
            e.printStackTrace()

            return false
        }
    }

    private fun getConfigFile(): File {
        return File("${filesDir.path}/main_config.json")
    }
}