package com.cusufcan.firebasepdf.view

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cusufcan.firebasepdf.databinding.ActivityPdfviewerBinding
import com.cusufcan.firebasepdf.helper.DownloadProgressUpdater
import com.cusufcan.firebasepdf.helper.STATUS_FAILED
import com.cusufcan.firebasepdf.helper.STATUS_SUCCESS
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class PDFViewerActivity : AppCompatActivity(), DownloadProgressUpdater.DownloadProgressListener {
    private lateinit var binding: ActivityPdfviewerBinding
    private lateinit var downloadManager: DownloadManager

    private var downloadProgressUpdater: DownloadProgressUpdater? = null

    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        binding = ActivityPdfviewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        snackbar = Snackbar.make(binding.mainLayout, "", Snackbar.LENGTH_INDEFINITE)

        val fileName = intent.extras?.getString("fileName")
        val downloadUrl = intent.extras?.getString("downloadUrl")

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream = URL(downloadUrl).openStream()
            withContext(Dispatchers.Main) {
                binding.pdfView.fromStream(inputStream).onRender { pages ->
                    if (pages >= 1) binding.progressBar.visibility = View.GONE
                }.load()
            }
        }

        binding.floatingActionButton.setOnClickListener {
            downloadPdf(fileName, downloadUrl)
        }
    }

    private fun downloadPdf(fileName: String?, downloadUrl: String?) {
        try {
            val downloadUri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(fileName).setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, File.separator + fileName
                )

            val downloadId = downloadManager.enqueue(request)
            downloadProgressUpdater = DownloadProgressUpdater(downloadManager, downloadId, this)
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                downloadProgressUpdater?.run()
            }
            snackbar.show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun updateProgress(progress: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (progress) {
                STATUS_SUCCESS -> {
                    snackbar.setText("Downloading... ${progress.toInt()}%")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@PDFViewerActivity, "Download completed", Toast.LENGTH_SHORT)
                        .show()
                    snackbar.dismiss()
                }

                STATUS_FAILED -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@PDFViewerActivity, "Download failed", Toast.LENGTH_SHORT)
                        .show()
                    snackbar.dismiss()
                }

                else -> {
                    snackbar.setText("Downloading... ${progress.toInt()}%")
                }
            }
        }
    }
}