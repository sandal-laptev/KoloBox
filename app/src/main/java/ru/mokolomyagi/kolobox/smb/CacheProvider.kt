package ru.mokolomyagi.kolobox.smb

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object CacheProvider {
    private var simpleCache: SimpleCache? = null

    fun getCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "media_cache")
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, NoOpCacheEvictor(), databaseProvider)
        }
        return simpleCache!!
    }

    fun clearCache(context: Context) {
        getCache(context).release()
        File(context.cacheDir, "media_cache").deleteRecursively()
    }
}