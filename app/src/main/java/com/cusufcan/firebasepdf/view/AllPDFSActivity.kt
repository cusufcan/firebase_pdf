package com.cusufcan.firebasepdf.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.cusufcan.firebasepdf.adapter.PDFFilesAdapter
import com.cusufcan.firebasepdf.databinding.ActivityAllPdfsBinding
import com.cusufcan.firebasepdf.model.PDFFile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AllPDFSActivity : AppCompatActivity(), PDFFilesAdapter.PDFClickListener {
    private lateinit var binding: ActivityAllPdfsBinding
    private lateinit var databaseReference: DatabaseReference

    private lateinit var adapter: PDFFilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initRecyclerView()
        getAllPDFs()
    }

    private fun init() {
        binding = ActivityAllPdfsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = Firebase.database.reference.child("pdfs")
    }

    private fun initRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PDFFilesAdapter(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getAllPDFs() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<PDFFile>()
                snapshot.children.forEach {
                    val pdfFile = it.getValue(PDFFile::class.java)
                    if (pdfFile != null) tempList.add(pdfFile)
                }
                if (tempList.isEmpty()) Toast.makeText(
                    this@AllPDFSActivity, "No data found", Toast.LENGTH_SHORT
                ).show()

                adapter.submitList(tempList)
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AllPDFSActivity, error.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onPDFClicked(pdfFile: PDFFile) {
        val intent = Intent(this, PDFViewerActivity::class.java)
        intent.putExtra("fileName", pdfFile.fileName)
        intent.putExtra("downloadUrl", pdfFile.downloadUrl)
        startActivity(intent)
    }
}