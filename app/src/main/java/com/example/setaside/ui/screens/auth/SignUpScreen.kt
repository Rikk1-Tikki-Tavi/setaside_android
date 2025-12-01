package com.example.setaside.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.setaside.R
import com.example.setaside.ui.components.ErrorDialog
import com.example.setaside.ui.components.InputField
import com.example.setaside.ui.components.LoadingButton
import com.example.setaside.ui.viewmodel.AuthUiState

@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String?) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onClearError: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    if (uiState.error != null) {
        ErrorDialog(message = uiState.error, onDismiss = onClearError)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- UPPER FRAME ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.veg_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF618264).copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(150.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 25.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "We set them aside for you!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shopping made easy with SetAside",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }

        // ---------- LOWER FRAME ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create an account",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.Start),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome to SetAside",
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 15.dp),
                color = Color(0xFF808080)
            )

            InputField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Name Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                },
                isPassword = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputField(
                label = "Phone Number (Optional)",
                value = phone,
                onValueChange = { phone = it },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoadingButton(
                text = "Sign Up",
                onClick = { 
                    onRegister(email, password, fullName, phone.takeIf { it.isNotBlank() })
                },
                isLoading = uiState.isLoading,
                enabled = email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 15.sp,
                    color = Color.Black
                )
                TextButton(onClick = onNavigateToSignIn) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF618264)
                    )
                }
            }
        }
    }
}
