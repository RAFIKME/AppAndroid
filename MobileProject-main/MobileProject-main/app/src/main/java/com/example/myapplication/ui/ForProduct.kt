package com.example.myapplication.ui

import android.app.Activity
import android.content.Context
import java.io.File
import com.example.myapplication.data.Product
import java.io.IOException
import java.io.FileOutputStream
import android.widget.Toast
import java.io.FileInputStream
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.FilePathProvider
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import android.util.Log
import androidx.compose.ui.platform.LocalContext


fun readProducts(context: Context, fileName: String): List<Product> {
    val productList = mutableListOf<Product>()

    // Use FilePathProvider to get the correct file path
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    if (!file.exists()) {
        println("File not found: ${file.absolutePath}")
        return productList
    }

    try {
        FileInputStream(file).use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet: Sheet = workbook.getSheetAt(0) // Assuming data is in the first sheet

            // Skip header row
            for (rowIndex in 1..sheet.lastRowNum) { // Start from 1 to skip header row
                val row: Row = sheet.getRow(rowIndex) ?: continue

                // Read and validate Product Name
                val productName = row.getCell(0)?.toString()?.takeIf { it.isNotBlank() } ?: continue

                // Read and validate Product Price
                val productPrice = row.getCell(1)?.toString()?.toDoubleOrNull() ?: continue

                // Read and validate Photo Name
                val photoName = row.getCell(2)?.toString()?.takeIf { it.isNotBlank() } ?: continue

                // Read and validate Description
                val description = row.getCell(3)?.toString()?.takeIf { it.isNotBlank() } ?: ""

                // Add product to the list if all fields are valid
                productList.add(Product(0, productName, productPrice, photoName, description)) // Placeholder ID
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
    }

    // Sort by product name
    productList.sortBy { it.productName }

    // Assign sequential IDs starting from 1
    productList.forEachIndexed { index, product ->
        productList[index] = product.copy(productId = index + 1)
    }

    return productList
}


fun formatPrice(price: Double): String {
    // Assuming AMD currency and no decimal points
    return "${price.toInt()} AMD"
}

fun updateProducts(context: Context, fileName: String, newProducts: List<Product>) {
    // Get the correct file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, fileName)

    // Read existing products from the file
    val existingProducts = readProducts(context, filePath).toMutableList()

    // Update existing products or add new ones
    newProducts.forEach { newProduct ->
        val existingProduct = existingProducts.find { it.productName == newProduct.productName }
        if (existingProduct != null) {
            // Update existing product
            val index = existingProducts.indexOf(existingProduct)
            existingProducts[index] = newProduct // Replace with updated product details
        } else {
            // Add new product
            existingProducts.add(newProduct)
        }
    }

    // Save updated list back to the file
    saveProducts(context, filePath, existingProducts)
}


fun saveProducts(context: Context, fileName: String, products: List<Product>) {
    // Get the correct file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    try {
        FileOutputStream(file).use { fos ->
            val workbook: Workbook = XSSFWorkbook() // Create a new workbook
            val sheet: Sheet = workbook.createSheet("Products")

            // Create header row
            val headerRow: Row = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Product Name")
            headerRow.createCell(1).setCellValue("Product Price")
            headerRow.createCell(2).setCellValue("Photo Name")
            headerRow.createCell(3).setCellValue("Description")

            // Write products to sheet
            products.forEachIndexed { index, product ->
                val row: Row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(product.productName)
                row.createCell(1).setCellValue(product.productPrice)
                row.createCell(2).setCellValue(product.photoName)
                row.createCell(3).setCellValue(product.description)
            }

            workbook.write(fos)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    println("Products updated and saved successfully.")
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, context: Context) {
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var imageName by remember { mutableStateOf("") } // State to remember image name
    var description by remember { mutableStateOf("") } // State to remember description

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    // Check if productPrice contains only numeric values
                    val isNumericPrice = productPrice.all { it.isDigit() }
                    if (productName.isNotEmpty() && isNumericPrice && imageName.isNotEmpty()) {
                        addProduct(context, productName, productPrice, imageName, description)
                        onDismiss()
                    }
                }
            ) {
                Text("Ավելացնել")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Չեղարկել")
            }
        },
        title = {
            Text("Ավելացնել Ապրանք")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Անուն") }
                )
                TextField(
                    value = productPrice,
                    onValueChange = { newValue ->
                        // Allow only numeric input for productPrice
                        if (newValue.all { it.isDigit() }) {
                            productPrice = newValue
                        }
                    },
                    label = { Text("Գին") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Նկարագրություն") }
                )
                TextField(
                    value = imageName,
                    onValueChange = { newValue -> imageName = newValue },
                    label = { Text("Նկարի անունը") }
                )
            }
        }
    )
}


fun addProduct(context: Context, name: String, price: String, imageName: String, description: String) {
    // Determine the correct file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, "Products.xlsx")
    val xlsxFile = File(filePath)

    // Create or open the Excel file
    val workbook: Workbook = try {
        if (xlsxFile.exists()) {
            FileInputStream(xlsxFile).use { fis ->
                XSSFWorkbook(fis)
            }
        } else {
            XSSFWorkbook()
        }
    } catch (e: Exception) {
        // Handle error opening/creating file
        Toast.makeText(context, "Error opening/creating file: ${e.message}", Toast.LENGTH_LONG).show()
        return
    }

    val sheet: Sheet = workbook.getSheetAt(0) ?: workbook.createSheet("Products")

    // Create header row if the file is new
    if (sheet.firstRowNum == 0 && sheet.getRow(0) == null) {
        val headerRow: Row = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Product Name")
        headerRow.createCell(1).setCellValue("Product Price")
        headerRow.createCell(2).setCellValue("Photo Name")
        headerRow.createCell(3).setCellValue("Description") // Ensure header matches schema
    }

    // Validate and convert price
    val productPrice: Double = try {
        if (price.isBlank()) 0.0 else price.toDouble()
    } catch (e: NumberFormatException) {
        Toast.makeText(context, "Invalid price format: ${e.message}", Toast.LENGTH_LONG).show()
        return
    }

    // Add product entry
    val rowIndex = sheet.lastRowNum + 1
    val row: Row = sheet.createRow(rowIndex)
    row.createCell(0).setCellValue(name)
    row.createCell(1).setCellValue(productPrice)
    row.createCell(2).setCellValue(imageName)
    row.createCell(3).setCellValue(description) // Add description

    // Write to the file
    try {
        FileOutputStream(xlsxFile).use { fos ->
            workbook.write(fos)
        }
        // Ensure this is on the main thread
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, "Նոր ապրանքատեսակը ավելացված է", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        // Handle error saving file
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, "Չհաջողվեց ավելացնել ապրանքը: ${e.message}", Toast.LENGTH_LONG).show()
        }
    } finally {
        workbook.close()
    }

    // After adding the product, update the product list
    try {
        val newProducts = readProducts(context, "Products.xlsx")
        updateProducts(context, "Products.xlsx", newProducts)
    } catch (e: Exception) {
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, "Չհաջողվեց թարմացնել ապրանքների ցանկը: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun DeleteProductDialog(
    product: Product,
    fileName: String,
    onDismiss: () -> Unit,
    context: Context,
    onConfirmDelete: () -> Unit // Handle the confirmation logic here
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Ցանկանում եք ջնջել ապրանքատեսակը ցանկից") },
        text = {
            Text(product.productName)
        },
        confirmButton = {
            Button(
                onClick = {
                    deleteProduct(context, fileName, product) // Call function to handle deletion
                    onConfirmDelete() // Call the confirmation callback
                    onDismiss() // Close the dialog
                }
            ) {
                Text("Հաստատել")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) { // Close the dialog
                Text("Չեղարկել")
            }
        }
    )
}

fun deleteProduct(context: Context, fileName: String, productToDelete: Product) {
    // Determine the correct file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    if (!file.exists()) {
        Toast.makeText(context, "Ֆայլը չի գտնվել: $fileName", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val workbook = XSSFWorkbook(FileInputStream(file))
        val sheet = workbook.getSheetAt(0)
        val rowsToKeep = mutableListOf<Row>()
        var productFound = false

        // Iterate over rows and identify rows to keep
        for (rowIndex in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            val productNameCell = row.getCell(0) // Assuming product name is in the first cell
            if (productNameCell != null &&
                productNameCell.stringCellValue.trim().equals(productToDelete.productName.trim(), ignoreCase = true)) {
                // Skip the row that matches the product to delete
                productFound = true
            } else {
                // Keep rows that do not match
                rowsToKeep.add(row)
            }
        }

        if (productFound) {
            // Create a new workbook and sheet to write the updated rows
            val newWorkbook = XSSFWorkbook()
            val newSheet = newWorkbook.createSheet("Products")

            // Write rows to the new sheet
            for ((index, row) in rowsToKeep.withIndex()) {
                val newRow = newSheet.createRow(index)
                for (cellIndex in 0 until row.lastCellNum) {
                    val cell = row.getCell(cellIndex)
                    val newCell = newRow.createCell(cellIndex)
                    when (cell?.cellType) {
                        CellType.STRING -> newCell.setCellValue(cell.stringCellValue)
                        CellType.NUMERIC -> newCell.setCellValue(cell.numericCellValue)
                        CellType.BOOLEAN -> newCell.setCellValue(cell.booleanCellValue)
                        CellType.FORMULA -> newCell.setCellFormula(cell.cellFormula)
                        CellType.BLANK -> newCell.setBlank()
                        CellType.ERROR -> newCell.setCellErrorValue(cell.errorCellValue)
                        else -> newCell.setBlank()
                    }
                }
            }

            // Write the updated workbook to the file
            FileOutputStream(file).use { outputStream ->
                newWorkbook.write(outputStream)
            }
            newWorkbook.close()

            Toast.makeText(context, "Ապրանքը հաջողությամբ ջնջված է", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context, "Ապրանքը չի գտնվել", Toast.LENGTH_SHORT).show()
        }

        workbook.close()

    } catch (e: Exception) {
        Toast.makeText(context, "Չհաջողվեց ջնջել ապրանքը: ${e.message}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

@Composable
fun EditProductDialog(
    productName: String,
    productPrice: String,
    photoName: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var newProductName by remember { mutableStateOf(productName) }
    var newProductPrice by remember { mutableStateOf(productPrice) }
    var newDescription by remember { mutableStateOf(description) }
    var newPhotoName by remember { mutableStateOf(photoName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Փոփոխել ապրանքը") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = newProductName,
                    onValueChange = { newProductName = it },
                    label = { Text("Անվանումը") }
                )
                TextField(
                    value = newProductPrice,
                    onValueChange = { newProductPrice = it },
                    label = { Text("Արժեքը") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = newPhotoName,
                    onValueChange = { newPhotoName = it },
                    label = { Text("Նկար Գումար") }
                )
                TextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    label = { Text("Նկարագրություն") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                updateProductInExcel(
                    filePath = "/storage/emulated/0/Documents/Boyarskoe/Products.xlsx",
                    oldProductName = productName,
                    newProductName = newProductName,
                    newProductPrice = newProductPrice,
                    newPhotoName = newPhotoName,
                    newDescription = newDescription

                )
                onConfirm(newProductName, newProductPrice, newPhotoName,newDescription)
            }) {
                Text("Հաստատել")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Չեղարկել")
            }
        }
    )
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }
    return "unknown"
}

fun updateProductInExcel(filePath: String, oldProductName: String, newProductName: String, newProductPrice: String,  newPhotoName: String,  newDescription: String) {
    val file = File(filePath)
    var fis: FileInputStream? = null
    var fos: FileOutputStream? = null
    var workbook: Workbook? = null

    try {
        fis = FileInputStream(file)
        workbook = XSSFWorkbook(fis)
        val sheet: Sheet = workbook.getSheetAt(0)  // Assuming the first sheet contains the data

        var updated = false
        for (row in sheet) {
            if (row.getCell(0)?.stringCellValue == oldProductName) {  // Assuming productName is in the first column
                row.getCell(0)?.setCellValue(newProductName)  // Update product name
                row.getCell(1)?.setCellValue(newProductPrice)  // Update product price
                row.getCell(2)?.setCellValue(newPhotoName)  // Update description
                row.getCell(3)?.setCellValue(newDescription)  // Update photo name
                updated = true
                break
            }
        }

        if (!updated) {
//            println( $oldProductName not found.")
        } else {
            fos = FileOutputStream(file)
            workbook.write(fos)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            fis?.close()
            fos?.close()
            workbook?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}