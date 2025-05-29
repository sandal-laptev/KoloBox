package ru.mokolomyagi.kolobox.smb

import com.hierynomus.smbj.share.File
import ru.mokolomyagi.kolobox.data.SmbFileEntry

interface SmbClient {
    fun listFiles(path: String): List<SmbFileEntry>
    fun downloadFile(remotePath: String, localDest: File): Boolean
    fun uploadFile(localFile: File, remoteDestPath: String): Boolean
    fun deleteFile(remotePath: String): Boolean
}