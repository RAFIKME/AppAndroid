package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.CityViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border

@Composable
fun CityList(navController: NavController, viewModel: CityViewModel) {
    val citiesState = viewModel.cities.collectAsState()
    val cities = citiesState.value

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Blue, Color.Cyan)
                    )
                )
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Add Button (Replaces Search Icon)
                IconButton(onClick = {
                    navController.navigate("AddRecords")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "Add",
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
                        text = "Քաղաքներ",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                // Menu Button
                IconButton(onClick = {
                    navController.navigate("MenuScreen")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_grd),
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
        }

        // City List
        LazyColumn {
            items(cities) { city ->
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
                            navController.navigate("ShopList/${city.id}")
                        }
                        .padding(8.dp) // Padding inside the border
                ) {
                    Text(
                        text = city.name,
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCityList() {
    CityList(navController = rememberNavController(), viewModel = viewModel())
}
