package ru.mokolomyagi.kolobox.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mokolomyagi.kolobox.R
import ru.mokolomyagi.kolobox.data.SmbFileEntry

class FileAdapter(
    private var items: List<SmbFileEntry>,
    private val onFileClick: (SmbFileEntry) -> Unit,
    private val onFolderClick: (SmbFileEntry) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewName)
        val iconView: ImageView = view.findViewById(R.id.iconView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.name

        holder.iconView.setImageResource(
            if (item.isDirectory) R.drawable.ic_folder else R.drawable.ic_draft
        )

        holder.itemView.setOnClickListener {
            if (item.isDirectory) {
                onFolderClick(item)
            } else {
                onFileClick(item)
            }
        }
    }

    fun updateItems(newItems: List<SmbFileEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
