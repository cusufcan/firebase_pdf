package com.cusufcan.firebasepdf.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.cusufcan.firebasepdf.R
import com.cusufcan.firebasepdf.databinding.ActivityMainBinding
import com.cusufcan.firebasepdf.model.PDFFile
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var launcher: ActivityResultLauncher<String>

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private var pdfFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        registerListeners()
        registerLaunchers()
    }

    private fun init() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = Firebase.database.reference.child("pdfs")
        storageReference = Firebase.storage.reference.child("pdfs")
    }

    private fun registerListeners() {
        binding.selectPdfButton.setOnClickListener {
            launcher.launch("application/pdf")
        }

        binding.uploadBtn.setOnClickListener {
            if (pdfFileUri != null) {
                uploadPDFFileToFirebase()
            } else {
                Toast.makeText(this, "Please select pdf first", Toast.LENGTH_SHORT).show()

            }
        }

        binding.showAllBtn.setOnClickListener {
            val intent = Intent(this, AllPDFSActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerLaunchers() {
        launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            pdfFileUri = it
            val fileName =
                it?.let { DocumentFile.fromSingleUri(this, it)?.name }
                    ?: resources.getString(R.string.no_file_selected)
            binding.fileName.text = fileName
        }
    }

    private fun uploadPDFFileToFirebase() {
        val fileName = binding.fileName.text.toString()
        val mStorageRef = storageReference.child("${System.currentTimeMillis()}/$fileName")

        pdfFileUri?.let {
            mStorageRef.putFile(it).addOnSuccessListener {
                mStorageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val pdfFile = PDFFile(fileName, downloadUri.toString())
                    databaseReference.push().key?.let { pushkey ->
                        databaseReference.child(pushkey).setValue(pdfFile).addOnSuccessListener {
                            pdfFileUri = null
                            binding.fileName.text =
                                resources.getString(R.string.no_file_selected)
                            Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show()

                            if (binding.progressBar.isShown) binding.progressBar.visibility =
                                View.GONE
                        }.addOnFailureListener { error ->
                            Toast.makeText(this, error.message.toString(), Toast.LENGTH_SHORT)
                                .show()

                            if (binding.progressBar.isShown) binding.progressBar.visibility =
                                View.GONE
                        }
                    }
                }
            }.addOnProgressListener { uploadTask ->
                val uploadingPercent =
                    uploadTask.bytesTransferred / uploadTask.totalByteCount * 100
                binding.progressBar.progress = uploadingPercent.toInt()
                if (!binding.progressBar.isShown) binding.progressBar.visibility = View.VISIBLE

            }.addOnFailureListener { error ->
                Toast.makeText(this, error.message.toString(), Toast.LENGTH_SHORT).show()

                if (binding.progressBar.isShown) binding.progressBar.visibility = View.GONE
            }
        }
    }
}