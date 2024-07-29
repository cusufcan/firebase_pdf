package com.cusufcan.firebasepdf.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cusufcan.firebasepdf.databinding.PdfItemBinding
import com.cusufcan.firebasepdf.model.PDFFile

class PDFFilesAdapter(private val listener: PDFClickListener) :
    ListAdapter<PDFFile, PDFFilesAdapter.PDFFilesViewHolder>(PDFDiffCallback()) {
    inner class PDFFilesViewHolder(private val binding: PdfItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                listener.onPDFClicked(getItem(adapterPosition))
            }
        }

        fun bind(data: PDFFile) {
            binding.fileName.text = data.fileName
        }
    }

    class PDFDiffCallback : DiffUtil.ItemCallback<PDFFile>() {
        override fun areItemsTheSame(oldItem: PDFFile, newItem: PDFFile) =
            oldItem.downloadUrl == newItem.downloadUrl

        override fun areContentsTheSame(oldItem: PDFFile, newItem: PDFFile) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDFFilesViewHolder {
        val binding = PdfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PDFFilesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PDFFilesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface PDFClickListener {
        fun onPDFClicked(pdfFile: PDFFile)
    }
}