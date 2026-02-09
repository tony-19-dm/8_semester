package com.example.astronomy

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "news",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable("news") { NewsScreen() }
            composable("opengl") { OpenGLScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = android.R.drawable.ic_menu_report_image),
                    contentDescription = "Новости"
                )
            },
            label = { Text("Новости") },
            selected = navController.currentDestination?.route == "news",
            onClick = { navController.navigate("news") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = "3D Сцена"
                )
            },
            label = { Text("3D Сцена") },
            selected = navController.currentDestination?.route == "opengl",
            onClick = { navController.navigate("opengl") }
        )
    }
}