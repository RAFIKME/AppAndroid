package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.CustomNavigationView
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_PERMISSIONS = 123
        const val REQUEST_CODE_PICK_FILE = 456
    }

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                CustomNavigationView()
            }
        }

        // Initialize file picker launcher
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleFileUri(uri, this)
                }
            }
        }

        // Check and request permissions
        if (!hasExternalStoragePermissions()) {
            requestExternalStoragePermissions()
        } else {
            // Perform file operations if permissions are granted
            if (isExternalStorageWritable()) {
                // Further file operations can be added here if needed
            } else {
                Log.e("MainActivity", "External storage is not writable.")
            }
        }
    }

    private fun hasExternalStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExternalStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission granted, perform file operations if needed
            } else {
                // Permission denied
                Log.e("MainActivity", "Permission denied.")
            }
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleFileUri(uri: Uri, context: Context) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            try {
                val workbook = WorkbookFactory.create(inputStream)
                // Process the workbook as needed
            } catch (e: Exception) {
                Log.e("FileOperations", "Failed to read workbook: ${e.message}")
            }
        } ?: run {
            Log.e("FileOperations", "Failed to open InputStream for URI.")
        }
    }
}
