package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.viewmodel.ShopViewModel
import kotlinx.coroutines.launch

@Composable
fun AddRecords(
    navController: NavController,
    shopViewModel: ShopViewModel // Pass the ShopViewModel
) {
    val context = LocalContext.current
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddShopDialog by remember { mutableStateOf(false) }

    // Observe and handle state changes
    val coroutineScope = rememberCoroutineScope()

    if (showAddProductDialog) {
        AddProductDialog(onDismiss = { showAddProductDialog = false }, context = context)
    }
    if (showAddShopDialog) {
        AddShopDialog(onDismiss = { showAddShopDialog = false }, context = context)
    }

    // Fetch and update shops when needed
    fun fetchAndUpdateShops(cityId: Int) {
        coroutineScope.launch {
            shopViewModel.loadShops(cityId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Updated background color
    ) {
        // Header
        Box(
            modifier = Modifier
                .background(Color.Blue)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Arrow Icon
                IconButton(onClick = {
                    navController.popBackStack() // Navigate back
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Centered Title
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = "Տվյալների կառավարում",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructional text and Button to open Cities.xlsx
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "1. Քաղաքների ցանկը տեսնելու համար ընտրել \"Քաղաքներ\" հրամանը։",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Button(
                    onClick = {
                        openFile(context, "Cities.xlsx")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Քաղաքներ", color = Color.White)
                }
            }

            // Instructional text and Button to open Shops.xlsx
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "2. Խանութների ցանկը տեսնելու համար ընտրել \"Խանութներ\" հրամանը։",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Button(
                    onClick = {
                        openFile(context, "Shops.xlsx")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Խանութներ", color = Color.White)
                }
            }

            // Instructional text and Button to open Products.xlsx
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "3. Ապրանքատեսակների ցանկը տեսնելու համար ընտրել \"Ապրանքներ\" հրամանը։",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Button(
                    onClick = {
                        openFile(context, "Products.xlsx")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Ապրանքներ", color = Color.White)
                }
            }

            // Instructional text and Button to add a new product
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "4. Նոր ապրանքտեսակ ավելացնելու համար ընտրել \"Ավելացնել Ապրանքտեսակ\" հրամանը։",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Button(
                    onClick = {
                        showAddProductDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Ավելացնել Ապրանքտեսակ", color = Color.White)
                }
            }

            // Instructional text and Button to add a new shop
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "5. Նոր խանութ ավելացնելու համար ընտրել \"Ավելացնել Խանութ\" հրամանը։",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Button(
                    onClick = {
                        // Replace with the actual city ID you want to use
                        val exampleCityId = 1
                        fetchAndUpdateShops(exampleCityId)

                        // Optionally show the dialog after updating shops
                        showAddShopDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Ավելացնել Խանութ", color = Color.White)
                }
            }
        }
    }
}
