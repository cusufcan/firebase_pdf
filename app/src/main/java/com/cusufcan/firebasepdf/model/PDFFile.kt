package com.cusufcan.firebasepdf.model

data class PDFFile(val fileName: String, val downloadUrl: String) {
    constructor() : this("", "")
}
