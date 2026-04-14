package com.gymtracker.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

// ─── Model ───────────────────────────────────────────────────────────────────

data class UpdateInfo(
    @SerializedName("versionCode")   val versionCode: Int,
    @SerializedName("versionName")   val versionName: String,
    @SerializedName("releaseNotes")  val releaseNotes: String = "",
    @SerializedName("downloadUrl")   val downloadUrl: String,
    @SerializedName("mandatory")     val mandatory: Boolean = false
)

// ─── Checker ─────────────────────────────────────────────────────────────────

object UpdateChecker {

    /**
     * Points to the version.json at the root of the GitHub repository.
     * The release.yml workflow updates this file automatically on every
     * tagged release, so the in-app checker always finds the latest version.
     *
     * Format of version.json:
     * {
     *   "versionCode": 2,
     *   "versionName": "1.1.0",
     *   "releaseNotes": "• Fixed workout timer drift\n• 130 new exercises added",
     *   "downloadUrl": "https://github.com/milanb-hub/GymBuddy/releases/download/v1.1.0/gymbuddy-1.1.0.apk",
     *   "mandatory": false
     * }
     */
    const val VERSION_JSON_URL =
        "https://raw.githubusercontent.com/IpadApe/GymBuddy/main/version.json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Returns [UpdateInfo] if a newer version is available, null if already up to date.
     * Throws [IOException] on network failure or unexpected server response —
     * callers must catch and show an error state rather than silently showing "up to date".
     */
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? =
        withContext(Dispatchers.IO) {
            // Timestamp + no-cache header busts GitHub CDN cache on every check
            val bustUrl = "$VERSION_JSON_URL?t=${System.currentTimeMillis()}"
            val request = Request.Builder()
                .url(bustUrl)
                .header("Cache-Control", "no-cache, no-store")
                .header("Pragma", "no-cache")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Server returned HTTP ${response.code} for $VERSION_JSON_URL")
            }
            val body = response.body?.string()
                ?: throw IOException("Empty response body from $VERSION_JSON_URL")
            val info = gson.fromJson(body, UpdateInfo::class.java)
            if (info.versionCode > currentVersionCode) info else null
        }
}
