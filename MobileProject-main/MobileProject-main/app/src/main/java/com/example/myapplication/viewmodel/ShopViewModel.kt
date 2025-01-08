package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Shop
import com.example.myapplication.ui.readShops
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> get() = _shops

    fun loadShops(cityId: Int) {
        Log.d(TAG, "loadShops: Loading shops for cityId $cityId")
        viewModelScope.launch {
            try {
                val allShops = readShops(getApplication(), "Shops.xlsx", cityId)
                _shops.value = allShops
                Log.d(TAG, "loadShops: Successfully loaded ${allShops.size} shops for cityId $cityId")
            } catch (e: Exception) {
                Log.e(TAG, "loadShops: Error loading shops", e)
            }
        }
    }

    fun getShopName(shopId: Int): String {
        Log.d(TAG, "getShopName: Retrieving shop name for shopId $shopId")
        val shop = _shops.value.find { it.id == shopId }
        val shopName = shop?.name ?: "Unknown Shop"
        Log.d(TAG, "getShopName: Shop name for shopId $shopId is $shopName")
        return shopName
    }

    // New function to log the shop name based on the clicked shopId
    fun logClickedShopName(shopId: Int) {
        Log.d(TAG, "logClickedShopName: Shop ID $shopId clicked")
        val shopName = getShopName(shopId)
        Log.d(TAG, "logClickedShopName: Shop name for ID $shopId is $shopName")
    }

    companion object {
        private const val TAG = "ShopViewModel"
    }
}
