package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.viewmodel.ProductViewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.data.Product
import com.example.myapplication.viewmodel.CartViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ProductList(
    navController: NavController,
    viewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    shopName: String // Added shopName parameter
) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedProductToDelete by remember { mutableStateOf<Product?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }


    // Get Context in Composable scope
    val context = LocalContext.current

    // Create a FocusRequester
    val focusRequester = remember { FocusRequester() }

    // Get FocusManager to clear focus
    val focusManager = LocalFocusManager.current

    // Handle showing the search box and requesting focus
    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            // Request focus for the search TextField when it becomes visible
            focusRequester.requestFocus()
        }
    }

    val filteredProducts = products.filter {
        it.productName.contains(searchQuery, ignoreCase = true)
    }

    // LazyListState to control scrolling
    val listState = rememberLazyListState()

    // Scroll to the top whenever the search query changes
    LaunchedEffect(searchQuery) {
        listState.scrollToItem(0)
    }

    // Handle back button press
    BackHandler {
        if (isSearchVisible) {
            // Close the search box and reset the state
            isSearchVisible = false
            searchQuery = ""
            focusManager.clearFocus() // Hide keyboard
        } else {
            navController.navigateUp() // Navigate back
        }
    }
    val isCartEmpty by remember { derivedStateOf { cartViewModel.checkIfCartIsEmpty() } }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Header Content
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Blue, Color.Cyan)
                        )
                    )
                    .border(1.dp, Color.Gray)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Conditionally show the Back Button
                    if (!isSearchVisible) {
                        IconButton(onClick = {
                            navController.navigateUp() // Navigate back
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    // Title or Search Box
                    if (isSearchVisible) {
                        // Search Box
                        TextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester), // Attach FocusRequester
                            singleLine = true,
                            label = { Text("Ապրանքի անվանումը") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus() // Hide keyboard after search
                                }
                            )
                        )
                    } else {
                        // Title
                        Text(
                            text = "Ապրանքատեսակներ",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Search Button
                    IconButton(onClick = {
                        isSearchVisible = !isSearchVisible
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Product List
        LazyColumn(
            state = listState, // Attach LazyListState
            modifier = Modifier
                .weight(1f) // Take up available space but leave room for the button
        ) {
            items(filteredProducts) { product ->
                val formattedPrice = String.format("%d", product.productPrice.toInt())

                // URL encode the description
                val encodedDescription = URLEncoder.encode(product.description, StandardCharsets.UTF_8.toString())

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .border(1.dp, Color.Gray)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.White, Color.LightGray)
                            )
                        )
                        .clickable {
                            // Navigate to the product image screen with the selected product details
                            navController.navigate(
                                "ProductImage/${product.productId}/${product.photoName}/${product.productName}/${encodedDescription}/${shopName}"
                            )
                        }
                        .padding(8.dp) // Padding inside the border
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Name
                        Text(
                            text = product.productName,
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.weight(1f)
                        )

                        // Product Price
                        Text(
                            text = "${formattedPrice} AMD",
                            color = Color.Blue,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterVertically)
                        )

                        // Remove Product Icon
                        IconButton(onClick = {
                            selectedProductToDelete = product
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_del),
                                contentDescription = "Remove Product",
                                tint = Color.Red // Set the tint color to red
                            )
                        }
                    }
                }
            }
        }

        // Confirm Orders Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Add Icon
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp) // Size of the icon
                    .background(Color(0xFF6200EE), CircleShape) // Purple background
                    .padding(13.dp) // Space around the icon
                    .clickable {
                        showAddProductDialog = true // Show dialog on click
                    }
            )

            if (showAddProductDialog) {
                AddProductDialog(onDismiss = { showAddProductDialog = false }, context = context)
            }
            // Conditionally show Shop Icon Button with updated appearance
            if (!isCartEmpty) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shop),
                    contentDescription = "Cart",
                    tint = Color.White,
                    modifier = Modifier
                        .size(60.dp) // Size of the icon
                        .background(Color(0xFF6200EE), CircleShape) // Purple background
                        .padding(13.dp) // Space around the icon
                        .clickable {
                            // Navigate to CartScreen with shopName included
                            navController.navigate("CartScreen/$shopName")
                        }
                )
            }
        }

        // Show DeleteProductDialog if needed
        selectedProductToDelete?.let { product ->
            DeleteProductDialog(
                product = product,
                fileName = "Products.xlsx",
                onDismiss = { selectedProductToDelete = null },
                context = context, // Pass context here
                onConfirmDelete = {
                    viewModel.deleteProduct(context, product) // Call delete method in ViewModel
                }
            )
        }
    }
}
