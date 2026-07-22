package com.frei.app.presentation.auth

import android.app.Activity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frei.app.R

private val HeaderHeight = 280.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as Activity

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Gender") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6C3FCF))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.frei_full_logo_transparent_cropped),
                contentDescription = "Frei",
                modifier = Modifier.fillMaxWidth(0.90f),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(8.dp))

        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = HeaderHeight),
            shape = RoundedCornerShape(
                topStart = 36.dp,
                topEnd = 36.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F8F8)
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp)
            ) {

                Text(
                    text = "Create account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF151B3D)
                )

                uiState.error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    RoundedField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "FIRST NAME",
                        modifier = Modifier.weight(1f)
                    )

                    RoundedField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "LAST NAME",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        OutlinedTextField(
                            value = selectedGender,
                            onValueChange = {},
                            readOnly = false,
                            label = {
                                Text(
                                    "GENDER",
                                    fontSize = 11.sp
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF6C3FCF)
                                )
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF2EFF8),
                                focusedContainerColor = Color(0xFFF2EFF8),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {

                            genderOptions.forEach { gender ->

                                DropdownMenuItem(
                                    text = {
                                        Text(gender)
                                    },
                                    onClick = {
                                        selectedGender = gender
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    RoundedField(
                        value = contact,
                        onValueChange = {

                            if (it.all(Char::isDigit) && it.length <= 10) {
                                contact = it
                            }

                        },
                        label = "CONTACT",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {

                        if (contact.length == 10) {
                            viewModel.sendOtp(
                                activity = activity,
                                phoneNumber = contact
                            )
                        } else {
                            viewModel.setError("Enter a valid 10-digit phone number")
                        }

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Phone Number")
                }
                if (uiState.isPhoneVerified) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "✓ Phone number verified",
                        color = Color(0xFF2E7D32)
                    )
                }

                if (uiState.otpSent) {

                    AlertDialog(

                        onDismissRequest = { },

                        title = {
                            Text("Verify OTP")
                        },

                        text = {

                            Column {

                                OutlinedTextField(
                                    value = otp,
                                    onValueChange = {

                                        if (it.length <= 6)
                                            otp = it

                                    },
                                    label = {
                                        Text("Enter OTP")
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                            }

                        },

                        confirmButton = {

                            TextButton(
                                onClick = {
                                    viewModel.verifyOtp(otp)
                                }
                            ) {

                                Text("Verify")

                            }

                        }

                    )

                }

                Spacer(modifier = Modifier.height(12.dp))

                RoundedField(
                    value = email,
                    onValueChange = { email = it },
                    label = "EMAIL"
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("PASSWORD") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    visualTransformation =
                        if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),

                    trailingIcon = {

                        IconButton(
                            onClick = {
                                passwordVisible = !passwordVisible
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF2EFF8),
                        focusedContainerColor = Color(0xFFF2EFF8),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("CONFIRM PASSWORD") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    visualTransformation =
                        if (confirmPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),

                    trailingIcon = {

                        IconButton(
                            onClick = {
                                confirmPasswordVisible =
                                    !confirmPasswordVisible
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (confirmPasswordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF2EFF8),
                        focusedContainerColor = Color(0xFFF2EFF8),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                RoundedField(
                    value = address,
                    onValueChange = { address = it },
                    label = "ADDRESS"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {

                        if (password != confirmPassword) {
                            viewModel.setError("Passwords do not match")
                            return@Button
                        }

                        viewModel.register(
                            email = email,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            contact = contact,
                            gender = selectedGender,
                            address = address
                        ) {
                            onRegisterSuccess()
                        }
                    },
                    enabled = uiState.isPhoneVerified,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C3FCF)
                    )
                ) {

                    Text(
                        text = "Get Started",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "Already have an account? ",
                        color = Color.Gray
                    )

                    Text(
                        text = "Login",
                        color = Color(0xFF6C3FCF),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onNavigateToLogin()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RoundedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        label = {
            Text(
                text = label,
                fontSize = 14.sp
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF2EFF8),
            focusedContainerColor = Color(0xFFF2EFF8),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        )
    )
}