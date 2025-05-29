package ru.mokolomyagi.kolobox.smb

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.InputStream

class InputStreamFetcher(
    private val inputStream: InputStream
) : DataFetcher<InputStream> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        callback.onDataReady(inputStream)
    }

    override fun cleanup() {
        inputStream.close()
    }

    override fun cancel() = Unit

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = DataSource.REMOTE
}