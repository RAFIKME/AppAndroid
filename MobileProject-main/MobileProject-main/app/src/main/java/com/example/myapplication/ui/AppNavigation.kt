package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.viewmodel.CityViewModel
import com.example.myapplication.viewmodel.ShopViewModel
import com.example.myapplication.viewmodel.ProductViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.CartViewModel
import androidx.navigation.navArgument
import com.example.myapplication.viewmodel.SharedViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val cityViewModel: CityViewModel = viewModel()
    val shopViewModel: ShopViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val sharedViewModel: SharedViewModel = viewModel() // Initialize SharedViewModel
    val cartViewModel: CartViewModel = viewModel { CartViewModel(sharedViewModel) } // Pass SharedViewModel to CartViewModel

    LaunchedEffect(Unit) {
        cityViewModel.loadCities() // Load cities when the app starts
    }

    NavHost(navController = navController, startDestination = "CityList") {
        composable("CityList") {
            CityList(navController, cityViewModel)
        }
        composable("ShopList/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toInt() ?: return@composable
            ShopList(cityId, navController, shopViewModel, cartViewModel)
        }
        composable(
            route = "ProductList/{shopId}/{shopName}",
            arguments = listOf(
                navArgument("shopId") { type = NavType.IntType },
                navArgument("shopName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getInt("shopId") ?: return@composable
            val shopName = backStackEntry.arguments?.getString("shopName") ?: return@composable
            LaunchedEffect(shopId) {
                productViewModel.loadProducts(context) // Pass the context if needed
            }
            ProductList(
                navController = navController,
                viewModel = productViewModel,
                cartViewModel = cartViewModel,
                shopName = shopName
            )
        }
        composable(
            route = "ProductImage/{productId}/{photoName}/{productName}/{description}/{shopName}",
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType },
                navArgument("photoName") { type = NavType.StringType },
                navArgument("productName") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("shopName") { type = NavType.StringType }  // Added shopName argument
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            val photoName = backStackEntry.arguments?.getString("photoName") ?: return@composable
            val productName = backStackEntry.arguments?.getString("productName") ?: return@composable
            val description = backStackEntry.arguments?.getString("description") ?: return@composable
            val shopName = backStackEntry.arguments?.getString("shopName") ?: return@composable

            ProductImage(
                productId = productId,
                photoName = photoName,
                productName = productName,
                description = description,
                shopName = shopName,  // Pass shopName to ProductImage
                navController = navController,
                cartViewModel = cartViewModel,
                productViewModel = productViewModel
            )
        }
        composable(
            route = "CartScreen/{headerName}",
            arguments = listOf(
                navArgument("headerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val headerName = backStackEntry.arguments?.getString("headerName") ?: "Default Header"
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                productViewModel = productViewModel,
                shopViewModel = shopViewModel,
                headerName = headerName // Pass headerName directly
            )
        }
        composable("MenuScreen") {
            MenuScreen(navController)
        }
        composable("AddRecords") {
            AddRecords(navController, shopViewModel)
        }
    }
}