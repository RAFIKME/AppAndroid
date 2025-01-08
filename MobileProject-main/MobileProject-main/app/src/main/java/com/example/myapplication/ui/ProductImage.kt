package com.example.myapplication.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.CartItem
import com.example.myapplication.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.ProductViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Product
import com.example.myapplication.viewmodel.SharedViewModel
import java.io.File

@Composable
fun ProductImage(
    productId: Int,
    productName: String,
    photoName: String,
    description: String,
    shopName: String,
    navController: NavController,
    cartViewModel: CartViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    sharedViewModel: SharedViewModel = viewModel()
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf(loadImageFromFile(context, photoName)) }
    var count by remember { mutableStateOf(0) }
    var percentage by remember { mutableIntStateOf(0) }
    var productPrice by remember { mutableDoubleStateOf(0.0) }
    var showEditDialog by remember { mutableStateOf(false) }
    var newProductName by remember { mutableStateOf(productName) }
    var newProductPrice by remember { mutableStateOf(productPrice.toString()) }
    var newPhotoName by remember { mutableStateOf(photoName) }
    var newDescription by remember { mutableStateOf(description) }
    var isImageEnlarged by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(shopName) {
        sharedViewModel.shopName.value = shopName
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            newPhotoName = fileName // Update photo name

            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageBitmap = bitmap?.asImageBitmap()
        }
    }

    LaunchedEffect(productId) {
        productViewModel.getProduct(productId)
        productPrice = productViewModel.getProductPrice(productId) ?: 0.0
        newProductPrice = productPrice.toString()
        newDescription = productViewModel.getProductDescription(productId) ?: ""
    }

    val isButtonEnabled = count >= 1

    fun getTruncatedDescription(desc: String, isExpanded: Boolean): String {
        return if (isExpanded) {
            desc
        } else {
            val maxLines = 3
            val lines = desc.split("\n")
            if (lines.size > maxLines) {
                lines.take(maxLines).joinToString("\n") + "...\nՏեսնել ավելին"
            } else {
                desc
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Blue, Color.Cyan)
                    )
                )
                .border(1.dp, Color.Gray)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    val icon = painterResource(id = R.drawable.ic_arrow_back)
                    Image(
                        painter = icon,
                        contentDescription = "Հետ",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = newProductName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )

                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Մշակել",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the image
        imageBitmap?.let {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = it,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isImageEnlarged = !isImageEnlarged }
                        .let { modifier ->
                            if (isImageEnlarged) {
                                modifier.height(600.dp)
                            } else {
                                modifier.height(300.dp)
                            }
                        }
                )

                if (newPhotoName.isNotEmpty()) {
                    Text(
                        text = "",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } ?: run {
            Text(
                text = "Նկարը չի գտնվել",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = getTruncatedDescription(newDescription, isDescriptionExpanded),
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Start
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    isDescriptionExpanded = !isDescriptionExpanded
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Row with quantity adjustment buttons
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Quantity preset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuantityButton(quantity = 5, currentCount = count) { newCount ->
                    count = newCount
                }
                Spacer(modifier = Modifier.width(8.dp))
                QuantityButton(quantity = 10, currentCount = count) { newCount ->
                    count = newCount
                }
                Spacer(modifier = Modifier.width(8.dp))
                QuantityButton(quantity = 20, currentCount = count) { newCount ->
                    count = newCount
                }
                Spacer(modifier = Modifier.width(8.dp))
                QuantityButton(quantity = 40, currentCount = count) { newCount ->
                    count = newCount
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row with increment and decrement buttons and TextField
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (count > 0) count-- },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier.size(60.dp)
                ) {
                    Text(text = "-", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = count.toString(),
                    onValueChange = { value ->
                        val newValue = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                        count = newValue
                    },
                    label = { Text("Քանակ") },
                    modifier = Modifier
                        .width(400.dp) // Fixed width for TextField
                        .border(1.dp, Color.Gray),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { count++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier.size(60.dp)
                ) {
                    Text(text = "+", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Զեղչ",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Gray)
                    .background(Color.LightGray)
                    .clickable { if (percentage > 0) percentage -= 1 }
                    .padding(8.dp)
            ) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Text(
                text = "${percentage}%",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Box(
                modifier = Modifier
                    .border(1.dp, Color.Gray)
                    .background(Color.LightGray)
                    .clickable { percentage += 1 }
                    .padding(8.dp)
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isButtonEnabled) {
                    val item = CartItem(
                        shopId = 1,
                        name = "Product $productId",
                        productId = productId,
                        count = count,
                        price = productPrice,
                        discountPercent = percentage.toDouble()
                    )
                    cartViewModel.addItem(item)
                    navController.navigateUp()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isButtonEnabled
        ) {
            Text(text = "Ավելացնել պատվերը")
        }

        if (showError) {
            Text(
                text = "Քանակը պետք է լինի առնվազն 1",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
            )
        }
    }

    if (showEditDialog) {
        EditProductDialog(
            productName = newProductName,
            productPrice = newProductPrice,
            photoName = newPhotoName,
            description = newDescription,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedProductName, updatedProductPrice, updatedDescription, updatedPhotoName ->
                val updatedProduct = Product(
                    productId = productId,
                    productName = updatedProductName,
                    productPrice = updatedProductPrice.toDoubleOrNull() ?: 0.0,
                    photoName = updatedPhotoName,
                    description = updatedDescription
                )

                // Update the product list and local state
                val products = listOf(updatedProduct)
                updateProducts(context, "Products.xlsx", products)

                // Update local state with the new values
                newProductName = updatedProductName
                newProductPrice = updatedProductPrice
                newPhotoName = updatedPhotoName
                newDescription = updatedDescription
                imageBitmap = loadImageFromFile(context, updatedPhotoName) // Load the new image

                showEditDialog = false
            }
        )
    }
}

@Composable
fun QuantityButton(quantity: Int, currentCount: Int, onClick: (Int) -> Unit) {
    Button(
        onClick = {
            onClick(currentCount + quantity)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Text(
            text = quantity.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}


fun getAppDirectory(context: Context): File {
    val directory = File(context.filesDir, "my_files")
    if (!directory.exists()) {
        directory.mkdirs() // Create the directory if it does not exist
    }
    return directory
}


fun loadImageFromFile(context: Context, imageName: String): ImageBitmap? {
    val filePath = "/storage/emulated/0/Documents/Boyarskoe/$imageName.png"
    val file = File(filePath)
    return if (file.exists()) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        bitmap?.asImageBitmap()
    } else {
        null
    }
}

