package ru.mokolomyagi.kolobox

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mokolomyagi.kolobox.databinding.ActivityMainBinding
import ru.mokolomyagi.kolobox.smb.SMBJSmbClient
import ru.mokolomyagi.kolobox.data.SmbFileEntry
import ru.mokolomyagi.kolobox.smb.SmbSettingsManager
import ru.mokolomyagi.kolobox.ui.adapters.FileAdapter
import java.util.LinkedList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var smbSettings: SmbSettingsManager
    private lateinit var smbClient: SMBJSmbClient
    private lateinit var adapter: FileAdapter
    private val pathStack = LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smbSettings = SmbSettingsManager(this)
        if (!smbSettings.isConfigured()) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        smbClient = SMBJSmbClient(smbSettings)

        adapter = FileAdapter(
            emptyList(),
            onFileClick = { file ->
                val fullPath = "${pathStack.joinToString("/")}/${file.name}"
                val isVideo = file.name.endsWith(".mp4", ignoreCase = true) ||
                        file.name.endsWith(".mkv", ignoreCase = true) ||
                        file.name.endsWith(".avi", ignoreCase = true)

                val intent = Intent(this, MediaViewerActivity::class.java).apply {
                    putExtra("media_type", if (isVideo) "video" else "image")
                    putExtra("media_url", fullPath)
                }
                startActivity(intent)
            },
            onFolderClick = { folder ->
                pathStack.add(folder.name.trimEnd('/'))
                val newPath = pathStack.joinToString("/")
                loadFolder(newPath)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val initialPath = smbSettings.getShare()?.trimEnd('/')
        pathStack.add(initialPath.toString())
        loadFolder(initialPath.toString())
    }

    private fun loadFolder(path: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val files = smbClient.listFiles(path).sortedWith(
                    compareByDescending<SmbFileEntry> { it.isDirectory }.thenBy { it.name.lowercase() }
                )

                withContext(Dispatchers.Main) {
                    adapter.updateItems(files)
                    updateBreadcrumbs()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка загрузки: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateBreadcrumbs() {
        val layout = binding.breadcrumbLayout
        layout.removeAllViews()

        // Добавим кнопку "Назад" если можно вернуться
        if (pathStack.size > 1) {
            val backButton = MaterialButton(this).apply {
                text = "Назад"
                setOnClickListener {
                    pathStack.removeLast()
                    val path = pathStack.joinToString("/")
                    loadFolder(path)
                }
            }
            layout.addView(backButton)
        }

        val fullPathList = mutableListOf<Pair<String, String>>()
        var current = ""
        pathStack.forEach { segment ->
            current = if (current.isEmpty()) segment else "$current/$segment"
            fullPathList.add(Pair(segment, current))
        }

        fullPathList.forEachIndexed { index, (name, path) ->
            val textView = MaterialTextView(this).apply {
                text = if (index == 0) name else " / $name"
                setPadding(8, 0, 8, 0)
                setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        android.R.color.holo_blue_light
                    )
                )
                setOnClickListener {
                    while (pathStack.joinToString("/") != path) {
                        pathStack.removeLast()
                    }
                    loadFolder(path)
                }
            }
            layout.addView(textView)
        }

        binding.breadcrumbScroll.post {
            binding.breadcrumbScroll.fullScroll(android.view.View.FOCUS_RIGHT)
        }
    }
}
