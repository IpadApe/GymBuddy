package com.gymtracker.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
     * Returns [UpdateInfo] if a newer version is available, null otherwise.
     * Never throws — network failures return null silently.
     */
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(VERSION_JSON_URL).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val info = gson.fromJson(body, UpdateInfo::class.java)
                    if (info.versionCode > currentVersionCode) info else null
                } else null
            } catch (_: Exception) {
                null
            }
        }
}
