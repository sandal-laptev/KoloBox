package ru.mokolomyagi.kolobox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mokolomyagi.kolobox.databinding.ActivitySettingsBinding
import ru.mokolomyagi.kolobox.smb.SmbSettingsManager
import java.net.URI

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загружаем сохранённые значения
        val prefs = getSharedPreferences("smb_settings", MODE_PRIVATE)
        binding.ipInput.setText(prefs.getString("ip", ""))
        binding.loginInput.setText(prefs.getString("login", ""))
        binding.passwordInput.setText(prefs.getString("password", ""))
        binding.domainInput.setText(prefs.getString("domain", ""))
        binding.pathInput.setText(prefs.getString("share", ""))

        // Кнопка сохранить
        binding.saveButton.setOnClickListener {
            val ip = binding.ipInput.text.toString().trim()
            val login = binding.loginInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val domain = binding.domainInput.text.toString().trim()
            var share = binding.pathInput.text.toString().trim()

            if (!share.contains('/')) {
                share = "$share/"
            }

            share = share.replace("\\", "/")

            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    try {
                        val client = SMBClient()
                        val uri = URI("smb://$ip/$share")
                        val hostname = uri.host ?: ip // если нет хоста в uri, используем ip

                        client.use { smbClient ->
                            val connection: Connection = smbClient.connect(hostname)
                            connection.use {
                                val auth =
                                    AuthenticationContext(login, password.toCharArray(), domain)
                                val session: Session = connection.authenticate(auth)
                                session.use {
                                    val shareName = uri.path.trimStart('/').substringBefore('/')
                                    val diskShare = session.connectShare(shareName) as DiskShare
                                    diskShare.use {
                                        // Попытка открыть папку - если папка доступна, значит соединение успешно
                                        val folderName =
                                            uri.path.removePrefix("/$shareName/").trimEnd('/')
                                        // Если path указывает на корень share, folderName может быть пустым
                                        if (folderName.isNotEmpty()) {
                                            val exists = diskShare.folderExists(folderName)
                                            exists
                                        } else {
                                            true
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }

                Log.d("KoloBox", "SMB connection success: $success")

                if (success) {
                    // Сохраняем настройки
                    val settings = SmbSettingsManager(this@SettingsActivity)
                    if (share.isBlank()) {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Пожалуйста, введите путь до папки.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    } else {
                        Log.d("KoloBox", "Путь, введённый пользователем: $share")
                        settings.save(ip, login, password, domain, share)
                    }

                    // Переход в MainActivity
                    startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Не удалось подключиться по SMB. Проверьте данные.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }
}
