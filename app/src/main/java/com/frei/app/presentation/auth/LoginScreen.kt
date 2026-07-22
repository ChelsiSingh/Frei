package com.frei.app.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.R
import com.google.firebase.auth.FirebaseAuth

private object FreiAuthColors {
    val Ink = Color(0xFF1B1B23)
    val InkMuted = Color(0xFF6E6E7C)
    val InkFaint = Color(0xFFA6A6B3)
    val Purple = Color(0xFF6C3FCF)
    val Background = Color(0xFFF7F3FC)
    val Surface = Color(0xFFFFFFFF)
}

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiAuthColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.frei_wordmark),
                    contentDescription = "Frei",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(58.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null, tint = FreiAuthColors.InkFaint)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAuthenticating,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FreiAuthColors.Surface,
                    unfocusedContainerColor = FreiAuthColors.Surface,
                    focusedBorderColor = FreiAuthColors.Purple,
                    unfocusedBorderColor = FreiAuthColors.InkFaint.copy(alpha = 0.4f),
                    focusedLabelColor = FreiAuthColors.Purple,
                    cursorColor = FreiAuthColors.Purple
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAuthenticating,
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = FreiAuthColors.InkFaint)
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = FreiAuthColors.InkFaint)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FreiAuthColors.Surface,
                    unfocusedContainerColor = FreiAuthColors.Surface,
                    focusedBorderColor = FreiAuthColors.Purple,
                    unfocusedBorderColor = FreiAuthColors.InkFaint.copy(alpha = 0.4f),
                    focusedLabelColor = FreiAuthColors.Purple,
                    cursorColor = FreiAuthColors.Purple
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isAuthenticating = true

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener {
                            isAuthenticating = false
                            onLoginSuccess()
                        }
                        .addOnFailureListener { exception ->
                            isAuthenticating = false
                            Toast.makeText(
                                context,
                                "Login Failed: ${exception.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FreiAuthColors.Purple,
                    contentColor = Color.White,
                    disabledContainerColor = FreiAuthColors.Purple.copy(alpha = 0.5f)
                ),
                enabled = !isAuthenticating
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreiAuthColors.InkMuted
                )
                Text(
                    text = "Register",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreiAuthColors.Purple,
                    modifier = Modifier.clickable(enabled = !isAuthenticating) { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}