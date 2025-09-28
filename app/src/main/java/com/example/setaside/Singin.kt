package com.example.setaside

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignInScreen(onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        // ---------- UPPER FRAME ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(370.dp)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.veg_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )


            // Gradient overlay
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

            // Logo
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
                    modifier = Modifier.size(1000.dp) // scale logo
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tagline
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 25.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "We set them aside for you!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    lineHeight = 35.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Shopping made easy with SetAside",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 15.sp
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

            // Header
            Text(
                text = "Sign In to your account",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 20.sp,
                modifier = Modifier.align(Alignment.Start),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Let’s get you ready!",
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 15.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 15.dp),
                color = Color(0xFF808080)
            )

            // ---------- EMAIL ----------
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
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- PASSWORD ----------
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
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- SIGN IN BUTTON ----------
            Button(
                onClick = { /* Handle sign in */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF618264)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- ACCOUNT CREATION + FORGOT PASSWORD ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don’t have an account? ",
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        color = Color.Black
                    )
                    TextButton(onClick = onNavigateToSignUp) {
                        Text(
                            text = "Sign Up",
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 15.sp,
                            color = Color.Black
                        )
                    }
                }

                TextButton(onClick = { /* Forgot Password */ }) {
                    Text(
                        text = "Forgot Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 15.dp),
                        color = Color(0xFF618264)
                    )
                }
            }
        }
    }
}

// ---------- REUSABLE INPUT FIELD ----------
@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: @Composable () -> Unit,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 15.sp,
                color = Color.Black
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(
                    width = 0.5.dp,
                    color = Color(0xFF808080),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon()
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
