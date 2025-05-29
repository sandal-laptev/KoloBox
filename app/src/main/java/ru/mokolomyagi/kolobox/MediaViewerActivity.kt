package ru.mokolomyagi.kolobox

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ru.mokolomyagi.kolobox.ui.fragments.ImageViewerFragment
import ru.mokolomyagi.kolobox.ui.fragments.MediaCarouselFragment
import ru.mokolomyagi.kolobox.ui.fragments.VideoPlayerFragment

class MediaViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_viewer)

        val mediaType = intent.getStringExtra("media_type") ?: "video"
        val mediaUrl = intent.getStringExtra("media_url") ?: ""

//        val fragment = when (mediaType) {
//            "image" -> ImageViewerFragment.newInstance(mediaUrl)
//            "video" -> VideoPlayerFragment.newInstance(mediaUrl)
//            else -> throw IllegalArgumentException("Unknown media type")
//        }
        
        val fragment = MediaCarouselFragment.newInstance(mediaUrl)

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
        supportActionBar?.hide() // скрыть верхнюю панель, если она есть

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
