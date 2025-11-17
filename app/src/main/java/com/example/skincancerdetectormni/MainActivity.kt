package com.example.skincancerdetectormni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skincancerdetectormni.home.mainapp.MainApp
import com.example.skincancerdetectormni.home.mainapp.Route
import com.example.skincancerdetectormni.utils.DrawerContent
import com.example.skincancerdetectormni.utils.JayLabsTopAppBar
import com.example.skincancerdetectormni.ui.theme.SkinCancerDetectorMNITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val topBarHiddenRoutes = listOf(
                Route.AuthScreen.route, Route.LogIn.route, Route.SignUp.route,
                Route.ForgotPassword.route
            )

            val drawerState =  rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            SkinCancerDetectorMNITheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            navController=navController,
                            scope = scope,
                            drawerState = drawerState
                        ) // Drawer content goes here
                    },
                    gesturesEnabled = currentRoute !in topBarHiddenRoutes
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (currentRoute !in topBarHiddenRoutes) {
                                JayLabsTopAppBar(
                                    navController,
                                    onPastReportsClick = {
                                        navController.navigate(Route.PastReports.route)
                                    },
                                    onMenuClick = {
                                        scope.launch { drawerState.open()

                                        } // Open drawer on menu click
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        MainApp(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SkinCancerDetectorMNITheme {
        Greeting("Android")
    }
}