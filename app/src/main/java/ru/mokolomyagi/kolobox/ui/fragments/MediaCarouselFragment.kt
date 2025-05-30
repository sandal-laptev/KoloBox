package ru.mokolomyagi.kolobox.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mokolomyagi.kolobox.R
import ru.mokolomyagi.kolobox.data.MediaItemData
import ru.mokolomyagi.kolobox.data.MediaType
import ru.mokolomyagi.kolobox.smb.SMBJSmbClient
import ru.mokolomyagi.kolobox.smb.SmbSettingsManager
import ru.mokolomyagi.kolobox.ui.MediaDisplayListener
import ru.mokolomyagi.kolobox.ui.adapters.MediaCarouselAdapter
import java.net.URI
import java.nio.file.Paths

class MediaCarouselFragment : Fragment(), MediaDisplayListener {

    private var currentPosition: Int = 0
    private lateinit var viewPager: ViewPager2
    private lateinit var mediaItems: List<MediaItemData>

    companion object {
        private const val ARG_PATH = "smb_path"
        fun newInstance(path: String): MediaCarouselFragment {
            val fragment = MediaCarouselFragment()
            val args = Bundle()
            args.putString(ARG_PATH, path)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onMediaDisplayFinished() {
        val nextPosition = (currentPosition + 1) % mediaItems.size
        viewPager.post {
            viewPager.setCurrentItem(nextPosition, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_media_carousel, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.viewPager)

        val path = arguments?.getString(ARG_PATH) ?: return
        val pathSegments = path.split("/").dropLast(1)
        val parentDirectory = "/" + pathSegments.joinToString("/")

        lifecycleScope.launch(Dispatchers.IO) {
            val client = SMBJSmbClient(SmbSettingsManager(requireContext()))
            val allFiles = client.listFiles(parentDirectory)
            mediaItems = allFiles.filter {
                it.name.endsWith(".mp4") || it.name.endsWith(".jpg") || it.name.endsWith(".png")
            }.map {
                MediaItemData(
                    url = "$parentDirectory/${it.name}",
                    type = if (it.name.endsWith(".mp4")) MediaType.VIDEO else MediaType.IMAGE
                )
            }

            withContext(Dispatchers.Main) {
                val adapter = MediaCarouselAdapter(this@MediaCarouselFragment, mediaItems)
                viewPager.adapter = adapter

                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        currentPosition = position

                        val fragmentTag = "f$position"  // ViewPager2 по умолчанию тегирует фрагменты как "f0", "f1", ...
                        val fragment = childFragmentManager.findFragmentByTag(fragmentTag)
                        if (fragment is VideoPlayerFragment) {
                            fragment.onVisible()
                        }
                    }
                })
            }
        }
    }
}