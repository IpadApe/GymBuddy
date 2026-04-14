package com.gymtracker.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object AppInstaller {

    /**
     * Enqueues an APK download via DownloadManager and installs it automatically
     * when the download completes. Shows a toast on failure.
     */
    fun downloadAndInstall(context: Context, downloadUrl: String, versionName: String) {
        if (downloadUrl.isBlank()) {
            Toast.makeText(context, "No download link available for this update", Toast.LENGTH_LONG).show()
            return
        }

        val fileName = "staystrong-$versionName.apk"
        val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        // Delete old APK files to avoid confusion
        destDir?.listFiles()?.filter { it.name.endsWith(".apk") }?.forEach { it.delete() }

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("StayStrong $versionName")
            .setDescription("Downloading update…")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        Toast.makeText(context, "Downloading update…", Toast.LENGTH_SHORT).show()

        // Register a one-shot receiver that fires when the download completes
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id != downloadId) return
                ctx.unregisterReceiver(this)

                // Check whether the download actually succeeded
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)
                if (cursor.moveToFirst()) {
                    val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val reasonCol = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                    val status = cursor.getInt(statusCol)
                    val reason = cursor.getInt(reasonCol)
                    cursor.close()

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val apkFile = File(
                            ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            fileName
                        )
                        if (apkFile.exists()) {
                            installApk(ctx, apkFile)
                        } else {
                            Toast.makeText(ctx, "Download completed but file not found", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val msg = when (reason) {
                            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Not enough storage space"
                            DownloadManager.ERROR_FILE_ERROR -> "Storage error"
                            DownloadManager.ERROR_HTTP_DATA_ERROR -> "Network error — check your connection"
                            404 -> "Update file not found on server"
                            else -> "Download failed (error $reason)"
                        }
                        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    cursor.close()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    /** Triggers the system install prompt for a downloaded APK file. */
    fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }
}
