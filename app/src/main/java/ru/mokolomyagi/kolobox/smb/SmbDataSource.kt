package ru.mokolomyagi.kolobox.smb

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import com.hierynomus.smbj.share.File
import java.io.InputStream
import kotlin.math.min

@UnstableApi
class SmbDataSource(
    private val smbFile: File
) : BaseDataSource(/* isNetwork = */ true) {

    private var inputStream: InputStream? = null
    private var bytesRemaining: Long = 0
    private var currentDataSpec: DataSpec? = null

    override fun open(dataSpec: DataSpec): Long {
        currentDataSpec = dataSpec

        val offset = dataSpec.position
        val length = dataSpec.length

        inputStream = smbFile.inputStream.apply {
            skip(offset)
        }

        bytesRemaining = if (length == C.LENGTH_UNSET.toLong()) {
            smbFile.fileInformation.standardInformation.endOfFile - offset
        } else {
            length
        }

        transferInitializing(dataSpec)
        transferStarted(dataSpec)

        return bytesRemaining
    }

    override fun getUri(): Uri? {
        return currentDataSpec?.uri
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val toRead = min(bytesRemaining, readLength.toLong()).toInt()
        val bytesRead = inputStream?.read(buffer, offset, toRead) ?: -1

        if (bytesRead > 0) {
            bytesRemaining -= bytesRead
            bytesTransferred(bytesRead)
        }

        return bytesRead
    }

    override fun close() {
        try {
            inputStream?.close()
        } finally {
            inputStream = null
            currentDataSpec = null
        }
    }
}
