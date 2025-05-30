package ru.mokolomyagi.kolobox.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mokolomyagi.kolobox.R
import ru.mokolomyagi.kolobox.smb.GlideApp
import ru.mokolomyagi.kolobox.smb.SMBJSmbClient
import ru.mokolomyagi.kolobox.smb.SmbSettingsManager
import ru.mokolomyagi.kolobox.ui.MediaDisplayListener
import java.io.File

class ImageViewerFragment : Fragment() {

    companion object {
        private const val ARG_SMB_URL = "smb_url"

        fun newInstance(smbUrl: String): ImageViewerFragment {
            return ImageViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SMB_URL, smbUrl)
                }
            }
        }
    }

    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_image_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoView: PhotoView = view.findViewById(R.id.photoView)
        val smbUrl = arguments?.getString(ARG_SMB_URL) ?: return

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val smbClient = SMBJSmbClient(SmbSettingsManager(requireContext()))
                val smbFile = smbClient.openSmbFile(smbUrl)

                val tempFile = File.createTempFile("image_", ".tmp", requireContext().cacheDir)
                smbFile.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // После загрузки изображения
                withContext(Dispatchers.Main) {
                    GlideApp.with(this@ImageViewerFragment)
                        .load(tempFile)
                        .into(photoView)

                    // Уведомим через 5 секунд
                    photoView.postDelayed({
                        (parentFragment as? MediaDisplayListener)?.onMediaDisplayFinished()
                    }, 5000)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Можно показать заглушку или сообщение об ошибке
            }
        }
    }

    override fun onDestroyView() {
        job?.cancel()
        super.onDestroyView()
    }
}
