package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel(
    private val sharedViewModel: SharedViewModel // Inject SharedViewModel
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> get() = _cartItems // Expose cartItems as StateFlow

    fun addItem(cartItem: CartItem) {
        _cartItems.value += cartItem
    }

    fun updateItemQuantity(cartItem: CartItem, newQuantity: Int) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.productId == cartItem.productId && item.shopId == cartItem.shopId) {
                item.copy(count = newQuantity)
            } else {
                item
            }
        }
    }

    fun getItemsByShop(): Map<Int, List<CartItem>> {
        return _cartItems.value.groupBy { it.shopId }
    }

    fun getShopTotal(shopId: Int): Double {
        return _cartItems.value.filter { it.shopId == shopId }
            .sumOf { it.price * it.count }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun removeItem(cartItem: CartItem) {
        _cartItems.value -= cartItem
    }

    fun checkIfCartIsEmpty(): Boolean {
        return _cartItems.value.isEmpty()
    }

    fun getHeaderName(): String {
        return sharedViewModel.headerName
    }

    companion object {
        private const val TAG = "CartViewModel"
    }
}
