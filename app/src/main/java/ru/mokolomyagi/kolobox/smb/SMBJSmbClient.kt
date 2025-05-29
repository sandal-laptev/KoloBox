package ru.mokolomyagi.kolobox.smb

import androidx.core.net.toUri
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import ru.mokolomyagi.kolobox.data.SmbFileEntry
import java.util.EnumSet

class SMBJSmbClient(private val smbSettings: SmbSettingsManager) : SmbClient {

    private val client = SMBClient()

    fun getSession(connection: Connection): Session {
        return connection.authenticate(
            AuthenticationContext(
                smbSettings.getLogin(),
                smbSettings.getPassword()?.toCharArray(),
                smbSettings.getDomain()
            )
        )
    }

    fun getShare(session: Session, path: String? = null): DiskShare {
        if (path != null) {
            val (shareName, relativePath) = getPathAdapter(path)
            return session.connectShare(shareName) as? DiskShare
                ?: throw IllegalStateException("Could not connect to share: $shareName")
        } else {
            return (session.connectShare(smbSettings.getShare()) as? DiskShare)!!
        }
    }

    fun getPathAdapter(path: String): Pair<String, String> {
        val uri = path.toUri()
        val shareName = uri.pathSegments.firstOrNull()
            ?: throw IllegalArgumentException("Share name not found in path")
        val relativePath = uri.pathSegments.drop(1).joinToString("/")
        return Pair(shareName, relativePath)
    }

    fun getFolder(share: DiskShare, path: String): List<FileIdBothDirectoryInformation> {
        return share.list(path) ?: return emptyList()
    }

    override fun listFiles(path: String): List<SmbFileEntry> {
        client.use { smbClient ->
            smbClient.connect(smbSettings.getIp()).use { connection ->
                val (shareName, relativePath) = getPathAdapter(path)
                return getFolder(getShare(getSession(connection), shareName), relativePath).map {
                    val isDir = (it.fileAttributes and 0x10L) != 0L // эта ебень, потому что — это
                    // FileIdBothDirectoryInformation, и fileAttributes у него имеет тип Long, а не Set<FileAttributes>.
                    // Флаг директории в SMB соответствует значению 0x10 (FILE_ATTRIBUTE_DIRECTORY).
                    SmbFileEntry(it.fileName, isDir, it.endOfFile)
                }
            }
        }
    }

    override fun downloadFile(
        remotePath: String,
        localDest: File
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun uploadFile(
        localFile: File,
        remoteDestPath: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    fun openSmbFile(fullPath: String): File {
        val (shareName, relativePath) = getPathAdapter(fullPath)
        val connection = client.connect(smbSettings.getIp())
        val share = getShare(getSession(connection), shareName)
        val accessMask = EnumSet.of(
            AccessMask.FILE_READ_DATA,
            AccessMask.FILE_READ_ATTRIBUTES,
            AccessMask.READ_CONTROL
        )
        val fileAttributes = EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL)
        val createOptions = EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE)

        return share.openFile(
            relativePath,
            accessMask,
            fileAttributes,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            createOptions
        )
    }

    override fun deleteFile(remotePath: String): Boolean {
        TODO("Not yet implemented")
    }

    // другие методы...
}