package ru.mokolomyagi.kolobox.smb

import android.content.Context
import android.util.Log
import androidx.core.content.edit

class SmbSettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("smb_settings", Context.MODE_PRIVATE)

    fun isConfigured(): Boolean {
        Log.d("KoloBox", "isConfigured: ")
        return !getIp().isNullOrBlank() &&
                !getLogin().isNullOrBlank() &&
                !getPassword().isNullOrBlank() &&
                !getShare().isNullOrBlank()
    }

    fun getIp(): String? = prefs.getString("ip", null)
    fun getLogin(): String? = prefs.getString("login", null)
    fun getPassword(): String? = prefs.getString("password", null)
    fun getDomain(): String? = prefs.getString("domain", "")
    fun getShare(): String? = prefs.getString("share", null)

    fun getSmbUrl(): String {
        val path = getShare()?.trimStart('/')
        return "smb://${getIp()}/$path/"
    }

    fun save(ip: String, login: String, password: String, domain: String, share: String) {
        prefs.edit {
            putString("ip", ip)
            putString("login", login)
            putString("password", password)
            putString("domain", domain)
            putString("share", share)
        }
    }
}
