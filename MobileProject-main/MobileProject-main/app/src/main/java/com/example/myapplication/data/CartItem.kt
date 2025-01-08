package com.example.myapplication.data

data class CartItem(
    val shopId: Int,
    val productId: Int, // Change from shopId to productId
    val name: String,
    val count: Int,
    val price: Double,
    val discountPercent: Double
)

