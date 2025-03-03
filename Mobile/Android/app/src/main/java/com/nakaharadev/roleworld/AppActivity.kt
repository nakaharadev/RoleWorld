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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.app)

        if (!configIsExists()) {
            initialLaunch()
        } else {
            Config.load(getConfigFile())
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