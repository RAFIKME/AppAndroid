package com.example.myapplication

import android.content.Context
import android.os.Build

object FilePathProvider {
    fun getFilePath(context: Context, fileName: String): String {
        return if (Build.FINGERPRINT.contains("generic")) {
            "${context.filesDir}/$fileName"
        } else {
            "/storage/emulated/0/Documents/Boyarskoe/$fileName"  // Updated path
        }
    }
}
