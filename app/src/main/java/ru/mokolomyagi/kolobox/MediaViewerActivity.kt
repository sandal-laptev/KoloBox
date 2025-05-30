package ru.mokolomyagi.kolobox

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ru.mokolomyagi.kolobox.ui.fragments.MediaCarouselFragment

class MediaViewerActivity : AppCompatActivity() {

    private var initialRotationEnabled = true // только для возврата в MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_viewer)

        // Получаем, но не применяем ориентацию
        initialRotationEnabled = intent.getBooleanExtra("rotation_enabled", true)

        val mediaUrl = intent.getStringExtra("media_url") ?: ""
        val fragment = MediaCarouselFragment.newInstance(mediaUrl)

        enterImmersiveMode()

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val resultIntent = Intent().apply {
            putExtra("restore_orientation", intent.getIntExtra("original_orientation", -1))
        }
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
    }

    private fun enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            WindowInsetsControllerCompat(window, window.decorView).let {
                it.hide(WindowInsetsCompat.Type.systemBars())
                it.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }

        supportActionBar?.hide()
    }
}
