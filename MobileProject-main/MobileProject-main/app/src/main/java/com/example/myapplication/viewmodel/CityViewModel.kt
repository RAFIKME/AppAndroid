package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.City
import com.example.myapplication.ui.readCities
import com.example.myapplication.ui.readCitiesFromFile
import com.example.myapplication.ui.toIntOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

class CityViewModel(application: Application) : AndroidViewModel(application) {
    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> get() = _cities

    init {
        loadCities()  // Ensure cities are loaded when the ViewModel is initialized
    }

    fun loadCities() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filePath = "/storage/emulated/0/Documents/Boyarskoe/Cities.xlsx"
                val file = File(filePath)
                if (file.exists()) {
                    Log.d("FileOperations", "File exists at: $filePath")
                    val cities = readCitiesFromFile(filePath)
                    _cities.value = cities
                } else {
                    Log.e("FileOperations", "File does not exist at: $filePath")
                }
            } catch (e: Exception) {
                Log.e("FileOperations", "Error loading cities: ${e.message}")
            }
        }
    }

    fun getCityById(id: Int): City? {
        return _cities.value.find { it.id == id }
    }
}
