package com.example.myapplication.data

data class ProductItem(
    val item: CartItem,
    var quantity: Int,
    var isEditing: Boolean = false
)
