package com.example.setaside

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.setaside.ui.theme.SetAsideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetAsideTheme {
                val navController = rememberNavController()

                // ---------- NAVIGATION ----------
                NavHost(
                    navController = navController,
                    startDestination = "signin" // default start page
                ) {
                    // Sign In Screen
                    composable("signin") {
                        SignInScreen(
                            onNavigateToSignUp = { navController.navigate("signup") }
                        )
                    }

                    // Sign Up Screen
                    composable("signup") {
                        SignUpScreen(
                            onNavigateToSignIn = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
