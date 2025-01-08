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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.viewmodel.ShopViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.example.myapplication.data.Shop
import com.example.myapplication.viewmodel.CartViewModel

@Composable
fun ShopList(
    cityId: Int,
    navController: NavController,
    shopViewModel: ShopViewModel,
    cartViewModel: CartViewModel
) {
    // Load shops
    LaunchedEffect(cityId) {
        shopViewModel.loadShops(cityId)
    }

    val shops by shopViewModel.shops.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedShopToDelete by remember { mutableStateOf<Shop?>(null) }
    val context = LocalContext.current // Get context within composable
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showAddShopDialog by remember { mutableStateOf(false) } // State for showing dialog

    // Handle showing the search box and requesting focus
    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus() // Hide keyboard
        }
    }

    // Handle back button press
    BackHandler {
        if (isSearchVisible) {
            isSearchVisible = false
            searchQuery = ""
            focusManager.clearFocus() // Hide keyboard
        } else {
            navController.navigateUp() // Navigate back
        }
    }

    // Determine if the cart is empty
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
                            label = { Text("Խանուտի անվանումը") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus() // Hide keyboard after search
                                    isSearchVisible = false // Hide search box after search
                                    searchQuery = ""
                                }
                            )
                        )
                    } else {
                        // Title
                        Text(
                            text = "Խանութներ",
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

        // Shop List
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 72.dp) // Add padding to avoid overlap with the icon
            ) {
                items(shops.filter { it.name.contains(searchQuery, ignoreCase = true) }) { shop ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .border(1.dp, Color.Gray)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.White, Color.LightGray)
                                )
                            )
                            .clickable {
                                // Navigate with both shopId and shopName
                                navController.navigate("ProductList/${shop.id}/${shop.name}")
                            }
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = shop.name,
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Start
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = {
                                selectedShopToDelete = shop
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
        }
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
                        showAddShopDialog = true // Show dialog on click
                    }
            )

            if (showAddShopDialog) {
                AddShopDialog(onDismiss = { showAddShopDialog = false }, context = context)
            }

            selectedShopToDelete?.let { shop ->
                DeleteShopDialog(
                    shop = shop,
                    fileName = "Shops.xlsx", // Example file name
                    cityId = cityId,
                    onDismiss = { selectedShopToDelete = null },
                    context = context,
                    onConfirmDelete = {
                        // Refresh shop list after deletion
                        shopViewModel.loadShops(cityId)
                    }
                )
            }
        }
    }
}
