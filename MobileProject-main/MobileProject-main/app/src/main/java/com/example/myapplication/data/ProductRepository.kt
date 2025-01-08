package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.data.Product
import java.io.IOException
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ProductRepository(private val context: Context) {

    fun readProducts(fileName: String): List<Product> {
        val products = mutableListOf<Product>()
        try {
            context.assets.open(fileName).use { excelFile ->
                val workbook = XSSFWorkbook(excelFile)
                val sheet = workbook.getSheetAt(0)

                // Read each row in the sheet
                for (i in 1..sheet.lastRowNum) { // Assuming the first row is header
                    val row = sheet.getRow(i) ?: continue

                    // Read Product ID
                    val productId = row.getCell(0)?.numericCellValue?.toInt() ?: continue

                    // Read Product Name
                    val productName = row.getCell(1)?.stringCellValue ?: continue

                    // Read Product Price
                    val productPrice = row.getCell(2)?.numericCellValue ?: continue

                    // Read Photo Name
                    val photoName = row.getCell(3)?.stringCellValue ?: continue

                    // Read Description
                    val description = row.getCell(4)?.stringCellValue ?: ""

                    // Add Product to the list
                    products.add(Product(productId, productName, productPrice, photoName, description))
                }

                workbook.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return products
    }
}
