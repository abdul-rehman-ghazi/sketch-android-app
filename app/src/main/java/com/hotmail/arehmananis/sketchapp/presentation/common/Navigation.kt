package com.hotmail.arehmananis.sketchapp.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import com.hotmail.arehmananis.sketchapp.presentation.feature.auth.LoginScreen
import com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.DrawingScreen
import com.hotmail.arehmananis.sketchapp.presentation.feature.gallery.GalleryScreen
import com.hotmail.arehmananis.sketchapp.presentation.feature.profile.ProfileScreen
import com.hotmail.arehmananis.sketchapp.presentation.feature.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Gallery : Screen("gallery", "Gallery", Icons.Default.Home)
    object Drawing : Screen("drawing/{sketchId}", "Drawing") {
        fun createRoute(sketchId: String?) = if (sketchId != null) {
            "drawing/$sketchId"
        } else {
            "drawing/new"
        }
    }
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Gallery,
    Screen.Profile,
    Screen.Settings
)

@Composable
fun AppNavigation(
    authUser: AuthUser?,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (authUser == null) Screen.Login.route else Screen.Gallery.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login screen (no bottom bar)
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Gallery.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Gallery screen (with bottom bar)
        composable(Screen.Gallery.route) {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) { padding ->
                GalleryScreen(
                    onCreateNewSketch = {
                        navController.navigate(Screen.Drawing.createRoute(null))
                    },
                    onSketchClick = { sketchId ->
                        navController.navigate(Screen.Drawing.createRoute(sketchId))
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        // Drawing screen (no bottom bar)
        composable(
            route = Screen.Drawing.route,
            arguments = listOf(
                navArgument("sketchId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val sketchId = backStackEntry.arguments?.getString("sketchId")
            DrawingScreen(
                sketchId = if (sketchId == "new") null else sketchId,
                onBack = { navController.popBackStack() }
            )
        }

        // Profile screen (with bottom bar)
        composable(Screen.Profile.route) {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) { padding ->
                ProfileScreen()
            }
        }

        // Settings screen (with bottom bar)
        composable(Screen.Settings.route) {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) { padding ->
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    screen.icon?.let { iconVector ->
                        Icon(
                            imageVector = iconVector,
                            contentDescription = screen.title
                        )
                    }
                },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
