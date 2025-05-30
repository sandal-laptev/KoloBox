package ru.mokolomyagi.kolobox.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.hierynomus.smbj.share.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mokolomyagi.kolobox.R
import ru.mokolomyagi.kolobox.smb.CacheProvider
import ru.mokolomyagi.kolobox.smb.SMBJSmbClient
import ru.mokolomyagi.kolobox.smb.SmbDataSource
import ru.mokolomyagi.kolobox.smb.SmbSettingsManager
import ru.mokolomyagi.kolobox.ui.MediaDisplayListener

class VideoPlayerFragment : Fragment() {

    companion object {
        private const val ARG_SMB_PATH = "smb_path"

        fun newInstance(smbPath: String): VideoPlayerFragment {
            val fragment = VideoPlayerFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_SMB_PATH, smbPath)
            }
            return fragment
        }
    }

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var smbSettingsManager: SmbSettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_video_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerView = view.findViewById(R.id.playerView)
        smbSettingsManager = SmbSettingsManager(requireContext())
        val smbClient = SMBJSmbClient(smbSettingsManager)

        val smbPath = arguments?.getString(ARG_SMB_PATH)
        if (smbPath.isNullOrEmpty()) {
            showError("Путь не указан")
            return
        }

        // Скрыть системные панели (если нужно)
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val smbFile = smbClient.openSmbFile(smbPath)
                withContext(Dispatchers.Main) {
                    playVideo(smbFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showError("Ошибка загрузки: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        parentFragmentManager.popBackStack()
    }

    @OptIn(UnstableApi::class)
    private fun playVideo(smbFile: File) {
        val cache = CacheProvider.getCache(requireContext())

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory { SmbDataSource(smbFile) }
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaItem = MediaItem.fromUri("smb://${smbFile.fileName}")
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            playerView.player = exoPlayer
            playerView.useController = true
            playerView.controllerShowTimeoutMs = 3000
            playerView.controllerAutoShow = false
            playerView.hideController()
            playerView.setOnClickListener {
                playerView.showController()
            }

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.play()
        }

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    (parentFragment as? MediaDisplayListener)?.onMediaDisplayFinished()
                }
            }
        })
    }

    fun onVisible() {
        player?.seekTo(0)
        player?.play()
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}

