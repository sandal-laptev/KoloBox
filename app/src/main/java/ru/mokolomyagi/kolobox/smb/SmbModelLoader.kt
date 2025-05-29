package ru.mokolomyagi.kolobox.smb

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class SmbModelLoader(
    private val smbClient: SMBJSmbClient
) : ModelLoader<String, InputStream> {

    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream> {
        val key = ObjectKey(model)
        val stream = smbClient.openSmbFile(model).inputStream
        return ModelLoader.LoadData(key, InputStreamFetcher(stream))
    }

    override fun handles(model: String): Boolean = model.startsWith("smb://")
}