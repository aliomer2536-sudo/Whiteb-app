package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.FormulaLibraryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.WhiteboardScreen
import com.example.ui.theme.SmartWhiteboardTheme
import com.example.ui.viewmodel.WhiteboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartWhiteboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val application = context.applicationContext as android.app.Application
                    val whiteboardViewModel: WhiteboardViewModel = viewModel(
                        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                    )
                    val navController = rememberNavController()
                    val notebooks by whiteboardViewModel.notebooks.collectAsState()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                notebooks = notebooks,
                                onSelectNotebook = { notebookId ->
                                    whiteboardViewModel.selectNotebook(notebookId)
                                    navController.navigate("whiteboard")
                                },
                                onCreateNotebook = { title, category, colorHex ->
                                    whiteboardViewModel.createNotebook(title, category, colorHex)
                                    navController.navigate("whiteboard")
                                },
                                onOpenFormulaLibrary = {
                                    navController.navigate("formula_library")
                                }
                            )
                        }

                        composable("whiteboard") {
                            WhiteboardScreen(
                                viewModel = whiteboardViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("formula_library") {
                            FormulaLibraryScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onInsertFormula = { formula ->
                                    whiteboardViewModel.insertFormula(formula)
                                    navController.navigate("whiteboard") {
                                        popUpTo("whiteboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
