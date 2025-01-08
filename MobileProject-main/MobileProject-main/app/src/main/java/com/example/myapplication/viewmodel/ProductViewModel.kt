package com.example.myapplication.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Product
import com.example.myapplication.ui.readProducts
import com.example.myapplication.ui.saveProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _product = MutableStateFlow<Product?>(null)

    fun loadProducts(context: Context) {
        Log.d(TAG, "loadProducts: Starting to load products")
        viewModelScope.launch {
            try {
                val products = readProducts(context, "Products.xlsx")
                _products.value = products
                Log.d(TAG, "loadProducts: Successfully loaded ${products.size} products")
            } catch (e: IOException) {
                Log.e(TAG, "loadProducts: Error loading products", e)
            }
        }
    }

    fun deleteProduct(context: Context, productToDelete: Product) {
        Log.d(TAG, "deleteProduct: Deleting product $productToDelete")
        viewModelScope.launch {
            try {
                val updatedProducts = _products.value.filter { it.productId != productToDelete.productId }
                saveProducts(context, "Products.xlsx", updatedProducts) // Save updated list
                _products.value = updatedProducts // Update state with new list
                Log.d(TAG, "deleteProduct: Product deleted, new list size ${updatedProducts.size}")
            } catch (e: IOException) {
                Log.e(TAG, "deleteProduct: Error deleting product", e)
            }
        }
    }

    fun getProduct(productId: Int) {
        Log.d(TAG, "getProduct: Getting product with ID $productId")
        viewModelScope.launch {
            val product = _products.value.find { it.productId == productId }
            _product.value = product
            if (product != null) {
                Log.d(TAG, "getProduct: Found product $product")
            } else {
                Log.d(TAG, "getProduct: Product with ID $productId not found")
            }
        }
    }

    fun getProductPrice(productId: Int): Double? {
        val price = _products.value.find { it.productId == productId }?.productPrice
        Log.d(TAG, "getProductPrice: Product ID $productId, Price $price")
        return price
    }

    fun getProductName(productId: Int): String? {
        val name = _products.value.find { it.productId == productId }?.productName
        Log.d(TAG, "getProductName: Product ID $productId, Name $name")
        return name
    }

    fun getProductDescription(productId: Int): String? {
        val description = _products.value.find { it.productId == productId }?.description
        Log.d(TAG, "getProductDescription: Product ID $productId, Description $description")
        return description
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }
    fun addProduct(newProduct: Product) {
        _products.value = _products.value + newProduct
        // Optionally, persist the addition to a database or remote server
    }

    fun deleteProduct(product: Product) {
        _products.value = _products.value.filter { it.productId != product.productId }
        // Optionally, persist the deletion to a database or remote server
    }
}
