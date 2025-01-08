package com.example.myapplication.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import java.text.NumberFormat
import java.util.Locale


// Utility to safely get a cell's string value
fun getCellValueAsString(row: Row, columnIndex: Int): String {
    val cell: Cell = row.getCell(columnIndex)
    return cell.stringCellValue ?: ""
}

// Utility to safely get a cell's numeric value as Double
fun getCellValueAsDouble(row: Row, columnIndex: Int): Double {
    val cell: Cell = row.getCell(columnIndex)
    return cell.numericCellValue
}

// Utility to safely get a cell's numeric value as Int
fun getCellValueAsInt(row: Row, columnIndex: Int): Int {
    val cell: Cell = row.getCell(columnIndex)
    return cell.numericCellValue.toInt()
}
// Utility to format a price as a currency string
fun formatPrice(price: Double): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return numberFormat.format(price)
}
// Utility to parse a string to a Double
fun parseDouble(value: String): Double {
    return value.toDoubleOrNull() ?: 0.0
}

// Utility to parse a string to an Int
fun parseInt(value: String): Int {
    return value.toIntOrNull() ?: 0
}
