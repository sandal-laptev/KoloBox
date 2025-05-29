package ru.mokolomyagi.kolobox.smb

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

class SmbModelLoaderFactory(
    private val smbClient: SMBJSmbClient
) : ModelLoaderFactory<String, InputStream> {
    override fun build(
        multiFactory: MultiModelLoaderFactory
    ): ModelLoader<String, InputStream> = SmbModelLoader(smbClient)

    override fun teardown() = Unit
}