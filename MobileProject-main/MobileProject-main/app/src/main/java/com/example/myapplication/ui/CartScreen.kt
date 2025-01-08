package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.ProductViewModel
import com.example.myapplication.viewmodel.ShopViewModel
import com.example.myapplication.R
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.ProductItem

@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel,
    shopViewModel: ShopViewModel,
    headerName: String
) {
    val context = LocalContext.current
    val cartItems by cartViewModel.cartItems.collectAsState() // Observe cart items
    val productItems = remember { mutableStateListOf<ProductItem>() }

    // Update ProductItems from cartItems
    LaunchedEffect(cartItems) {
        productItems.clear()
        productItems.addAll(
            cartItems.map { cartItem ->
                ProductItem(cartItem, cartItem.count)
            }
        )
    }


    // State to manage shop totals
    val shopTotals = remember { mutableStateMapOf<Int, Double>() }

    // Function to update total prices
    fun updateTotalPrices() {
        shopTotals.clear()
        productItems.groupBy { it.item.shopId }.forEach { (shopId, items) ->
            val total = items.sumOf { productItem ->
                val productPrice = productViewModel.getProductPrice(productItem.item.productId) ?: 0.0
                val discountPercent = productItem.item.discountPercent
                val discountedPrice = productPrice * (1 - discountPercent / 100)
                discountedPrice * productItem.quantity
            }
            shopTotals[shopId] = total
        }
    }

    // Update totals initially and when productItems changes
    LaunchedEffect(productItems) {
        updateTotalPrices()
    }

    // Provide cart empty status
    val isCartEmpty by remember { derivedStateOf { cartItems.isEmpty() } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar Header
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(colors = listOf(Color.Blue, Color.Cyan)))
                .border(1.dp, Color.Gray)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Title
                Text(
                    text = headerName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Check if cart is empty
        if (isCartEmpty) {
            Text(
                text = "Զաբյուղը դատարկ է",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // Cart Items
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(productItems) { productItem ->
                    val item = productItem.item
                    val productName = productViewModel.getProductName(item.productId) ?: "Unknown Product"
                    val productPrice = productViewModel.getProductPrice(item.productId) ?: 0.0
                    val discountPercent = item.discountPercent

                    // Calculate the price after applying discount
                    val discountedPrice = productPrice * (1 - discountPercent / 100)
                    val totalPrice = discountedPrice * productItem.quantity

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.LightGray.copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        // Product Name
                        Text(
                            text = productName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        // Quantity and Discounted Price with Total Price Right-Aligned
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quantity Adjustment
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (productItem.quantity > 1) {
                                            cartViewModel.updateItemQuantity(
                                                item.copy(count = productItem.quantity - 1),
                                                productItem.quantity - 1
                                            )
                                            updateTotalPrices()
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_minus),
                                        contentDescription = "Decrease Quantity",
                                        tint = Color.Black
                                    )
                                }
                                // Quantity Display
                                Text(
                                    text = "${productItem.quantity} հատ",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        cartViewModel.updateItemQuantity(
                                            item.copy(count = productItem.quantity + 1),
                                            productItem.quantity + 1
                                        )
                                        updateTotalPrices()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_plus),
                                        contentDescription = "Increase Quantity",
                                        tint = Color.Black
                                    )
                                }
                            }

                            // Total Price
                            Text(
                                text = formatPrice(totalPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Blue,
                                textAlign = TextAlign.End
                            )
                        }

                        // Remove Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    cartViewModel.removeItem(item)
                                    productItems.remove(productItem)
                                    updateTotalPrices()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "Ջնջել",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Display a total for all items
                val totalForAllItems = productItems.sumOf { productItem ->
                    val productPrice = productViewModel.getProductPrice(productItem.item.productId) ?: 0.0
                    val discountPercent = productItem.item.discountPercent
                    val discountedPrice = productPrice * (1 - discountPercent / 100)
                    discountedPrice * productItem.quantity
                }

                item {
                    Text(
                        text = "Ընդհանուր: ${formatPrice(totalForAllItems)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Button
        Button(
            onClick = {
                // Save the cart to CSV file
                saveCart(context, headerName, cartViewModel, productViewModel, shopViewModel)

                // Clear the cart items
                cartViewModel.clearCart()

                // Navigate back to Shop List and pop up all previous screens
                navController.navigate("CityList") {
                    popUpTo("CityList") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp)
                .background(Color.White, shape = MaterialTheme.shapes.small)
        ) {
            Text(
                text = "Հաստատել",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Remove Button
        Button(
            onClick = {
                cartViewModel.clearCart()
                navController.navigate("CityList") {
                    popUpTo("CityList") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp)
                .background(Color.White, shape = MaterialTheme.shapes.small)
        ) {
            Text(
                text = "Հեռացնել",
                color = Color.White
            )
        }
    }
}
