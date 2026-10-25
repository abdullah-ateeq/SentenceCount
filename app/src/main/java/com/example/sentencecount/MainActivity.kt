package com.example.sentencecount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sentencecount.ui.SentenceCountViewModel
import com.example.sentencecount.ui.screens.AboutScreen
import com.example.sentencecount.ui.screens.HistoryScreen
import com.example.sentencecount.ui.screens.HomeScreen
import com.example.sentencecount.ui.screens.SettingsScreen
import com.example.sentencecount.ui.screens.SplashScreen
import com.example.sentencecount.ui.theme.SentenceCountTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: SentenceCountViewModel = viewModel()
            val themeSettings by viewModel.themeSettings.collectAsState()

            SentenceCountTheme(
                appTheme = themeSettings.theme,
                dynamicColor = themeSettings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToAbout = { navController.navigate("about") }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("about") {
                            AboutScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
