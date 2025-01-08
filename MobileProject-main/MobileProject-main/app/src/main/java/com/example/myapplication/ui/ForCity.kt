package com.example.myapplication.ui


import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.myapplication.FilePathProvider
import java.io.File
import com.example.myapplication.data.City
import java.io.IOException
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.WorkbookFactory




fun readCities(context: Context, fileName: String): List<City> {
    val cityList = mutableListOf<City>()

    val projection = arrayOf(MediaStore.Files.FileColumns._ID)
    val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(fileName)

    val cursor = context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)

            Log.d("FileOperations", "Found URI: $uri")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)
                    val iterator = sheet.iterator()

                    if (iterator.hasNext()) {
                        iterator.next() // Skip header row if present
                    }

                    while (iterator.hasNext()) {
                        val row = iterator.next()
                        val cityIdCell = row.getCell(0)
                        val cityNameCell = row.getCell(1)

                        if (cityIdCell == null || cityNameCell == null) continue

                        val cityId = cityIdCell.numericCellValue.toIntOrNull() ?: continue
                        val cityName = cityNameCell.stringCellValue ?: continue

                        cityList.add(City(cityId, cityName))
                    }

                    workbook.close()
                } catch (e: Exception) {
                    Log.e("FileOperations", "Failed to read workbook: ${e.message}")
                }
            } ?: run {
                Log.e("FileOperations", "Failed to get InputStream for file.")
            }
        } else {
            Log.e("FileOperations", "File not found in MediaStore.")
        }
    } ?: run {
        Log.e("FileOperations", "Failed to query MediaStore.")
    }

    return cityList
}




fun Double.toIntOrNull(): Int? {
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        null
    }
}
fun readCitiesFromFile(filePath: String): List<City> {
    val cityList = mutableListOf<City>()

    try {
        FileInputStream(filePath).use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            val iterator = sheet.iterator()

            if (iterator.hasNext()) {
                iterator.next() // Skip header row if present
            }

            while (iterator.hasNext()) {
                val row = iterator.next()
                val cityIdCell = row.getCell(0)
                val cityNameCell = row.getCell(1)

                if (cityIdCell == null || cityNameCell == null) continue

                val cityId = cityIdCell.numericCellValue.toIntOrNull() ?: continue
                val cityName = cityNameCell.stringCellValue ?: continue

                cityList.add(City(cityId, cityName))
            }

            workbook.close()
        } ?: run {
            Log.e("FileOperations", "Failed to get InputStream for file.")
        }
    } catch (e: Exception) {
        Log.e("FileOperations", "Failed to read file: ${e.message}")
    }

    return cityList
}
