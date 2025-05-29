package ru.mokolomyagi.kolobox.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.mokolomyagi.kolobox.ui.fragments.MediaCarouselFragment
import ru.mokolomyagi.kolobox.data.MediaItemData
import ru.mokolomyagi.kolobox.data.MediaType
import ru.mokolomyagi.kolobox.ui.fragments.ImageViewerFragment
import ru.mokolomyagi.kolobox.ui.fragments.VideoPlayerFragment

class MediaCarouselAdapter(
    fragmentActivity: MediaCarouselFragment,
    private val items: List<MediaItemData>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = items.size

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return when (item.type) {
            MediaType.IMAGE -> ImageViewerFragment.Companion.newInstance(item.url)
            MediaType.VIDEO -> VideoPlayerFragment.Companion.newInstance(item.url)
        }
    }
}