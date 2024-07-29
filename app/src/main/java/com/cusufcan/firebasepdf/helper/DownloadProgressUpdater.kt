package com.cusufcan.firebasepdf.helper

import android.annotation.SuppressLint
import android.app.DownloadManager
import kotlinx.coroutines.delay

const val STATUS_SUCCESS = 100L
const val STATUS_FAILED = -100L

class DownloadProgressUpdater(
    private val downloadManager: DownloadManager,
    private val downloadId: Long,
    private val listener: DownloadProgressListener,
) {
    interface DownloadProgressListener {
        fun updateProgress(progress: Long)
    }

    private val query = DownloadManager.Query()
    private var totalBytes = 0

    init {
        query.setFilterById(downloadId)
    }

    @SuppressLint("Range")
    suspend fun run() {
        while (downloadId > 0) {
            delay(250)
            downloadManager.query(query).use {
                if (it.moveToFirst()) {
                    if (totalBytes <= 0) {
                        totalBytes =
                            it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    }

                    when (it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            listener.updateProgress(STATUS_SUCCESS)
                            return
                        }

                        DownloadManager.STATUS_FAILED -> {
                            listener.updateProgress(STATUS_FAILED)
                            return
                        }

                        else -> {
                            val bytesDownloadedSoFar =
                                it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val progress = bytesDownloadedSoFar * 100L / totalBytes
                            listener.updateProgress(progress = progress)
                        }
                    }
                }
            }
        }
    }
}