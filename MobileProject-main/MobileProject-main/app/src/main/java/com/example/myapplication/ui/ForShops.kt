package com.example.myapplication.ui

import android.content.Context
import java.io.File
import com.example.myapplication.data.City
import com.example.myapplication.data.Shop
import java.io.FileOutputStream
import android.widget.Toast
import java.io.FileInputStream
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.myapplication.FilePathProvider
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

fun readShops(context: Context, fileName: String, cityId: Int): List<Shop> {
    val shops = mutableListOf<Shop>()
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    if (!file.exists()) {
        println("File not found: $filePath")
        return shops
    }

    try {
        FileInputStream(file).use { fis ->
            val workbook: Workbook = WorkbookFactory.create(fis)
            val sheet: Sheet = workbook.getSheetAt(0) // Assuming the data is in the first sheet

            for (row in sheet) {
                if (row.rowNum == 0) {
                    // Skip the header row
                    continue
                }

                val fileCityId = row.getCell(0)?.numericCellValue?.toInt() ?: continue
                if (fileCityId != cityId) {
                    continue
                }

                val shopName = row.getCell(1)?.stringCellValue?.trim()
                if (shopName.isNullOrBlank()) {
                    continue
                }

                // Generate new Shop ID starting from 1
                val shopId = shops.size + 1

                shops.add(Shop(shopId, shopName))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return shops
}

fun updateShops(context: Context, fileName: String, cityId: Int, newShops: List<Shop>) {
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)
    val shops = readShops(context, fileName, cityId).toMutableList()

    // Update existing shops or add new ones
    newShops.forEach { newShop ->
        val existingShop = shops.find { it.name == newShop.name }
        if (existingShop != null) {
            // Update existing shop (if needed)
            val index = shops.indexOf(existingShop)
            shops[index] = newShop // Replace with updated shop details
        } else {
            // Add new shop
            shops.add(newShop)
        }
    }

    // Save updated list back to the file
    saveShops(context, fileName, cityId, shops)
}

fun saveShops(context: Context, fileName: String, cityId: Int, shops: List<Shop>) {
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    try {
        FileOutputStream(file).use { fos ->
            val workbook: Workbook = XSSFWorkbook() // Create a new workbook
            val sheet: Sheet = workbook.createSheet("Shops")

            // Create header row
            val headerRow: Row = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("City ID")
            headerRow.createCell(1).setCellValue("Shop Name")

            // Write shops to sheet
            shops.forEachIndexed { index, shop ->
                val row: Row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(cityId.toDouble()) // Write city ID
                row.createCell(1).setCellValue(shop.name)
            }

            workbook.write(fos)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    println("Shops updated and saved successfully.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopDialog(onDismiss: () -> Unit, context: Context) {
    var shopName by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Load cities from the file
    val cities by remember { mutableStateOf(readCities(context, "Cities.xlsx")) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    val cityId = selectedCity?.id
                    if (shopName.isNotBlank() && cityId != null) {
                        addShop(context,cityId,shopName)
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
            Text("Ավելացնել Խանութ")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shop Name Field
                TextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Խանութի անուն") }
                )

                // City Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isExpanded -> isDropdownExpanded = isExpanded }
                ) {
                    TextField(
                        value = selectedCity?.name ?: "",
                        onValueChange = { /* No-op */ },
                        label = { Text("Քաղաքի անուն") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isDropdownExpanded
                            )
                        },
                        readOnly = true,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.name) },
                                onClick = {
                                    selectedCity = city
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Display selected city
                if (selectedCity != null) {
                    Text(text = "Ընտրված Քաղաք: ${selectedCity?.name}")
                }
            }
        }
    )
}

fun addShop(context: Context, cityId: Int, shopName: String) {
    val filePath = FilePathProvider.getFilePath(context, "Shops.xlsx")
    val xlsxFile = File(filePath)
    val workbook: Workbook = if (xlsxFile.exists()) {
        FileInputStream(xlsxFile).use { fis ->
            WorkbookFactory.create(fis)
        }
    } else {
        XSSFWorkbook()
    }

    val sheet: Sheet = workbook.getSheetAt(0) ?: workbook.createSheet("Shops")

    // Create header row if the file is new
    if (sheet.firstRowNum == 0 && sheet.getRow(0) == null) {
        val headerRow: Row = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("City ID")
        headerRow.createCell(1).setCellValue("Shop Name")
    }

    // Find the last row index
    val rowIndex = sheet.lastRowNum + 1

    // Add new shop entry
    val row: Row = sheet.createRow(rowIndex)
    row.createCell(0).setCellValue(cityId.toDouble())
    row.createCell(1).setCellValue(shopName)

    // Write to the file
    FileOutputStream(xlsxFile).use { fos ->
        workbook.write(fos)
    }

    workbook.close()

    Toast.makeText(context, "Նոր խանութը ավելացված է", Toast.LENGTH_SHORT).show()

    // Optionally, update the UI or local cache
    // val updatedShops = readShops(context, "Shops.xlsx", cityId)
    // updateShops(context, "Shops.xlsx", cityId, updatedShops)
}

@Composable
fun DeleteShopDialog(
    shop: Shop,
    fileName: String,
    cityId: Int,
    onDismiss: () -> Unit,
    context: Context,
    onConfirmDelete: () -> Unit // No need to pass shop name, handle confirmation logic here
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Ցանկանում եք ջնջել խանութը ցանկից") },
        text = {
            Text(shop.name)
        },
        confirmButton = {
            Button(
                onClick = {
                    deleteShop(context, fileName, cityId, shop) // Provide all required parameters
                    onConfirmDelete() // Call the confirmation callback
                    onDismiss() // Close the dialog
                }
            ) {
                Text("Հաստատել")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {  // Close the dialog
                Text("Չեղարկել")
            }
        }
    )
}


fun deleteShop(context: Context, fileName: String, cityId: Int, shopToDelete: Shop) {
    // Get the file path using FilePathProvider
    val filePath = FilePathProvider.getFilePath(context, fileName)
    val file = File(filePath)

    if (!file.exists()) {
        Toast.makeText(context, "Ֆայլը չի գտնվել: $fileName", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val workbook: Workbook = FileInputStream(file).use { fis ->
            XSSFWorkbook(fis)
        }
        val sheet: Sheet = workbook.getSheetAt(0)
        val rowsToKeep = mutableListOf<Row>()
        var shopFound = false

        // Iterate over rows and identify rows to keep
        for (rowIndex in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            val shopNameCell = row.getCell(1) // Assuming shop name is in the second cell
            if (shopNameCell != null &&
                shopNameCell.stringCellValue.trim().equals(shopToDelete.name.trim(), ignoreCase = true) &&
                row.getCell(0)?.numericCellValue?.toInt() == cityId) {
                // Skip the row that matches the shop to delete
                shopFound = true
            } else {
                // Keep rows that do not match
                rowsToKeep.add(row)
            }
        }

        if (shopFound) {
            // Create a new workbook and sheet to write the updated rows
            val newWorkbook = XSSFWorkbook()
            val newSheet = newWorkbook.createSheet("Shops")

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

            Toast.makeText(context, "Խանութը հաջողությամբ ջնջված է", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context, "Խանութը չի գտնվել", Toast.LENGTH_SHORT).show()
        }

        workbook.close()

    } catch (e: Exception) {
        Toast.makeText(context, "Չհաջողվեց ջնջել խանութը: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}