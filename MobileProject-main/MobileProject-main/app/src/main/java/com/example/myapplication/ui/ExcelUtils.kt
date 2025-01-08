package com.example.myapplication.ui

import android.content.Context
import java.io.File
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.ProductViewModel
import com.example.myapplication.viewmodel.ShopViewModel
import java.io.IOException
import java.io.FileOutputStream
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.myapplication.FilePathProvider
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

fun saveCart(
    context: Context,
    headerName: String, // New parameter for headerName
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel,
    shopViewModel: ShopViewModel
) {
    // Get the file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, "Check.xlsx")
    val xlsxFile = File(filePath)

    try {
        val workbook: Workbook = if (xlsxFile.exists()) {
            // If file exists, read it
            FileInputStream(xlsxFile).use { fis ->
                XSSFWorkbook(fis)
            }
        } else {
            // If file doesn't exist, create a new workbook
            XSSFWorkbook()
        }

        val sheet: Sheet = workbook.getSheet("Orders") ?: workbook.createSheet("Orders")

        // Find the last row index
        val lastRowNum = sheet.lastRowNum
        var rowIndex = if (lastRowNum == 0) 0 else lastRowNum + 1

        // If it's a new file, write the header
        if (rowIndex == 0) {
            val headerRow: Row = sheet.createRow(rowIndex++)
            headerRow.createCell(0).setCellValue("Shop Name")
            headerRow.createCell(1).setCellValue("Product Name")
            headerRow.createCell(2).setCellValue("Count")
            headerRow.createCell(3).setCellValue("Discounted Price")
            headerRow.createCell(4).setCellValue("Total")
        }

        val itemsByShop = cartViewModel.getItemsByShop()

        var grandTotalForAllShops = 0.0 // Track the grand total for all shops
        val shopTotals = mutableListOf<Double>() // List to keep track of each shop's total

        for (shopId in itemsByShop.keys) {
            val shopItems = itemsByShop[shopId] ?: emptyList()

            var totalForShop = 0.0 // Track the total for the current shop

            for (item in shopItems) {
                val productName = productViewModel.getProductName(item.productId) ?: "Unknown Product"
                val productPrice = productViewModel.getProductPrice(item.productId) ?: 0.0
                val discountedPrice = productPrice * (1 - item.discountPercent / 100)
                val totalPrice = discountedPrice * item.count

                // Create a new row in the sheet
                val row: Row = sheet.createRow(rowIndex++)
                row.createCell(0).setCellValue(headerName) // Use headerName for Shop Name
                row.createCell(1).setCellValue(productName)
                row.createCell(2).setCellValue(item.count.toDouble())
                row.createCell(3).setCellValue(formatPrice(discountedPrice))
                row.createCell(4).setCellValue(formatPrice(totalPrice))

                // Update total for shop
                totalForShop += totalPrice
            }

            // Add shop total row
            val count = 0
            val shopSummaryRow: Row = sheet.createRow(rowIndex++)
            shopSummaryRow.createCell(0).setCellValue("Խանութի ընդհանուր") // Shop total in Armenian
            shopSummaryRow.createCell(1).setCellValue("Ընդհանուր")
            shopSummaryRow.createCell(2).setCellValue(count.toDouble())
            shopSummaryRow.createCell(3).setCellValue("1")
            shopSummaryRow.createCell(4).setCellValue(formatPrice(totalForShop))

            // Add the shop total to the list
            shopTotals.add(totalForShop)

            // Update grand total
        }

        // Write the updated workbook to the file
        FileOutputStream(xlsxFile).use { fileOut ->
            workbook.write(fileOut)
        }

        workbook.close()

        Toast.makeText(context, "Պատվերները պահպանված են", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        Toast.makeText(context, "Չհաջողվեց պահպանել պատվերները", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}



fun openFile(context: Context, fileName: String) {
    // Get the file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    if (file.exists()) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open XLSX with"))
        } catch (e: Exception) {
            // Handle errors gracefully
            Toast.makeText(context, "Չհաջողվեց բացել ֆայլը: ${e.message}", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "Ֆայլը առկա չէ: $fileName", Toast.LENGTH_LONG).show()
    }
}
// Helper function to read the first record from the file



fun clearFile(context: Context, fileName: String) {
    // Get the file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    if (!file.exists()) {
        Toast.makeText(context, "Ֆայլը առկա չէ: $fileName", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val inputStream = FileInputStream(file)
        val workbook = WorkbookFactory.create(inputStream) as XSSFWorkbook
        val sheet = workbook.getSheetAt(0)

        // Create a new workbook to hold the header only
        val newWorkbook = XSSFWorkbook()
        val newSheet = newWorkbook.createSheet("Orders")

        // Copy the header row
        val headerRow = sheet.getRow(0)
        if (headerRow != null) {
            val newHeaderRow = newSheet.createRow(0)
            headerRow.forEachIndexed { index, cell ->
                val newCell = newHeaderRow.createCell(index)
                newCell.setCellValue(cell.stringCellValue)
            }
        }

        // Write the new workbook to the file
        FileOutputStream(file).use { outputStream ->
            newWorkbook.write(outputStream)
        }

        workbook.close()
        newWorkbook.close()

        Toast.makeText(context, "Պատվերները հեռացված են", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Չհաջողվեց հեռացնել պատվերները: ${e.message}", Toast.LENGTH_LONG).show()
    }
}



fun processAndSaveFile(
    context: Context,
    inputFileName: String,
    outputFileName: String
) {
    // Define base and output directory paths
    val baseDir = File("/storage/emulated/0/Documents/Boyarskoe/")
    val outDir = File(baseDir, "Out")

    // Create the Out directory if it doesn't exist
    if (!outDir.exists()) {
        outDir.mkdirs()
    }

    // Get the file paths using FilePathProvider
    val inputFilePath = FilePathProvider.getFilePath(context, inputFileName)
    val outputFilePath1 = FilePathProvider.getFilePath(context, outputFileName)
    val outputFilePath2 = FilePathProvider.getFilePath(context, "Check1.xlsx") // Hardcoded or derived output file name

    val inputFile = File(inputFilePath)
    if (!inputFile.exists()) {
        Toast.makeText(context, "Input file not found: $inputFileName", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        // Read and process the input Excel file
        val workbook = XSSFWorkbook(FileInputStream(inputFile))
        val sheet = workbook.getSheetAt(0)

        // Maps to store aggregated data
        val productDataForCheck = mutableMapOf<String, Pair<Int, Double>>()
        val productDataForCheck1 = mutableMapOf<String, Pair<Int, Double>>()

        // Iterate through rows (starting from 1 to skip header row)
        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            val shopName = row.getCell(0)?.stringCellValue?.trim() ?: continue
            val productName = row.getCell(1)?.stringCellValue?.trim() ?: continue
            val count = row.getCell(2)?.numericCellValue?.toIntOrNull() ?: continue
            val totalStr = row.getCell(4)?.stringCellValue?.trim() ?: continue

            // Extract numeric value from totalStr
            val total = totalStr.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: continue

            // Update productDataForCheck (includes all records)
            val currentDataForCheck = productDataForCheck[productName] ?: Pair(0, 0.0)
            val updatedCountForCheck = currentDataForCheck.first + count
            val updatedTotalForCheck = currentDataForCheck.second + total
            productDataForCheck[productName] = Pair(updatedCountForCheck, updatedTotalForCheck)

            // Update productDataForCheck1 (excludes "Խանութի ընդհարուր")
            if (shopName != "Խանութի ընդհանուր") {
                val currentDataForCheck1 = productDataForCheck1[productName] ?: Pair(0, 0.0)
                val updatedCountForCheck1 = currentDataForCheck1.first + count
                val updatedTotalForCheck1 = currentDataForCheck1.second + total
                productDataForCheck1[productName] = Pair(updatedCountForCheck1, updatedTotalForCheck1)
            }
        }

        // Create new workbook for Check.xlsx (includes all data)
        val checkWorkbook = XSSFWorkbook()
        val checkSheet = checkWorkbook.createSheet("Summary")

        // Create header row for Check.xlsx
        val headerRowCheck = checkSheet.createRow(0)
        headerRowCheck.createCell(0).setCellValue("Product Name")
        headerRowCheck.createCell(1).setCellValue("Count")
        headerRowCheck.createCell(2).setCellValue("Total") // Updated column name

        // Populate the new sheet for Check.xlsx
        var rowIndexCheck = 1
        var totalPriceCheck = 0.0
        for ((productName, data) in productDataForCheck) {
            val row = checkSheet.createRow(rowIndexCheck++)
            row.createCell(0).setCellValue(productName)
            row.createCell(1).setCellValue(data.first.toDouble())
            row.createCell(2).setCellValue(data.second) // Set numeric value
            totalPriceCheck += data.second
        }

        // Add summary row to Check.xlsx
        val summaryRowCheck = checkSheet.createRow(rowIndexCheck)
        summaryRowCheck.createCell(0).setCellValue("Ընդհանուր") // Total in Armenian
        summaryRowCheck.createCell(1).setCellValue("") // Leave Count cell empty
        summaryRowCheck.createCell(4).setCellValue(totalPriceCheck) // Set numeric total

        // Create new workbook for Check1.xlsx (excludes "Խանութի ընդհարուր")
        val check1Workbook = XSSFWorkbook()
        val check1Sheet = check1Workbook.createSheet("Summary")

        // Create header row for Check1.xlsx
        val headerRowCheck1 = check1Sheet.createRow(0)
        headerRowCheck1.createCell(0).setCellValue("Product Name")
        headerRowCheck1.createCell(1).setCellValue("Count")
        headerRowCheck1.createCell(2).setCellValue("Total") // Updated column name

        // Populate the new sheet for Check1.xlsx
        var rowIndexCheck1 = 1
        var totalPriceCheck1 = 0.0
        for ((productName, data) in productDataForCheck1) {
            val row = check1Sheet.createRow(rowIndexCheck1++)
            row.createCell(0).setCellValue(productName)
            row.createCell(1).setCellValue(data.first.toDouble())
            row.createCell(2).setCellValue(data.second) // Set numeric value
            totalPriceCheck1 += data.second
        }

        // Add summary row to Check1.xlsx
        val summaryRowCheck1 = check1Sheet.createRow(rowIndexCheck1)
        summaryRowCheck1.createCell(0).setCellValue("Ընդհանուր") // Total in Armenian
        summaryRowCheck1.createCell(1).setCellValue("") // Leave Count cell empty

        // Apply the same price style for Check1.xlsx
        val totalPriceCellCheck1 = summaryRowCheck1.createCell(2)
        totalPriceCellCheck1.setCellValue(formatPrice(totalPriceCheck1)) // Use formatted string

        // Save output files
        val outputFileCheck = File(outputFilePath1)
        FileOutputStream(outputFileCheck).use { outputStream ->
            checkWorkbook.write(outputStream)
        }

        val outputFileCheck1 = File(outputFilePath2)
        FileOutputStream(outputFileCheck1).use { outputStream ->
            check1Workbook.write(outputStream)
        }

        // Close workbooks
        checkWorkbook.close()
        check1Workbook.close()
        workbook.close()

        // Copy the Check1.xlsx and input file to the Out directory
        val outFileCheck1 = File(outDir, "Check1.xlsx")
        val outFileInput = File(outDir, inputFile.name)

        // Copy files to the Out directory
        outputFileCheck1.copyTo(outFileCheck1, overwrite = true)
        inputFile.copyTo(outFileInput, overwrite = true)

        // Send email with attachments
        sendEmailWithAttachments(context, outFileCheck1.name, outFileInput.name)

    } catch (e: Exception) {
        Toast.makeText(context, "Չհաջողվեց: ${e.message}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}


fun sendEmailWithAttachments(context: Context, vararg fileNames: String) {
    // Define the base directory path as the 'Out' directory
    val outDir = File("/storage/emulated/0/Documents/Boyarskoe/Out")

    // Ensure the base directory exists
    if (!outDir.exists() || !outDir.isDirectory) {
        Toast.makeText(context, "Output directory does not exist.", Toast.LENGTH_SHORT).show()
        return
    }

    // Map file names to file objects in the 'Out' directory
    val files = fileNames.map { fileName ->
        File(outDir, fileName)
    }

    // Log file paths for debugging
    files.forEach { file ->
        Log.d("SendEmail", "File path: ${file.absolutePath}, Exists: ${file.exists()}")
    }

    // Create an intent for sending an email with attachments
    val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"  // MIME type for Excel files
        putExtra(Intent.EXTRA_EMAIL, arrayOf("melkonyanaram101@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Excel Files")
        putExtra(Intent.EXTRA_TEXT, "Please find the attached files.")

        // Get URIs for the files
        val uris = files.map { file ->
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Grant read permission
    }

    try {
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}







// Function to extract numeric value from a currency formatted string

