package com.chocowholesale.customer.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.chocowholesale.customer.ui.cart.CartScreen
import com.chocowholesale.customer.ui.home.HomeScreen
import com.chocowholesale.customer.ui.home.CategoryProductsScreen
import com.chocowholesale.customer.ui.home.ProductDetailScreen
import com.chocowholesale.customer.ui.home.SearchScreen
import com.chocowholesale.customer.ui.orders.OrdersScreen
import com.chocowholesale.customer.ui.orders.OrderDetailScreen
import com.chocowholesale.customer.ui.profile.ProfileScreen

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    SEARCH("search", "Search", Icons.Default.Search),
    CART("cart", "Cart", Icons.Default.ShoppingCart),
    ORDERS("orders", "Orders", Icons.Default.Receipt),
    PROFILE("profile", "Profile", Icons.Default.Person)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomTabs = BottomTab.entries
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(BottomTab.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.HOME.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomTab.HOME.route) {
                HomeScreen(
                    onCategoryClick = { id, name ->
                        navController.navigate("category/$id/$name")
                    },
                    onProductClick = { id ->
                        navController.navigate("product/$id")
                    },
                    onGoToCart = {
                        navController.navigate(BottomTab.CART.route) {
                            popUpTo(BottomTab.HOME.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.SEARCH.route) {
                SearchScreen(onProductClick = { id -> navController.navigate("product/$id") })
            }
            composable(BottomTab.CART.route) {
                CartScreen(onOrderPlaced = {
                    navController.navigate(BottomTab.ORDERS.route) {
                        popUpTo(BottomTab.HOME.route) { saveState = true }
                    }
                })
            }
            composable(BottomTab.ORDERS.route) {
                OrdersScreen(onOrderClick = { id -> navController.navigate("order/$id") })
            }
            composable(BottomTab.PROFILE.route) { ProfileScreen() }

            composable(
                "category/{categoryId}/{categoryName}",
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { entry ->
                CategoryProductsScreen(
                    categoryId = entry.arguments!!.getString("categoryId")!!,
                    categoryName = entry.arguments!!.getString("categoryName")!!,
                    onProductClick = { id -> navController.navigate("product/$id") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { entry ->
                ProductDetailScreen(
                    productId = entry.arguments!!.getString("productId")!!,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "order/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { entry ->
                OrderDetailScreen(
                    orderId = entry.arguments!!.getString("orderId")!!,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
