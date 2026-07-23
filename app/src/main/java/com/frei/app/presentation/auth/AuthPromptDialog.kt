package com.frei.app.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private object FreiPromptColors {
    val Ink = Color(0xFF1B1B23)
    val InkMuted = Color(0xFF6E6E7C)
    val Purple = Color(0xFF6C3FCF)
    val PurpleSoft = Color(0xFFEFE8FC)
    val Surface = Color(0xFFFFFFFF)
}

@Composable
fun AuthPromptDialog(
    onLoginClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = FreiPromptColors.Surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = FreiPromptColors.PurpleSoft,
                    modifier = Modifier.size(52.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = FreiPromptColors.Purple
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Login to continue",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiPromptColors.Ink,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Sign in or create a free account to continue.",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreiPromptColors.InkMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FreiPromptColors.Purple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(4.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        "Not now",
                        color = FreiPromptColors.InkMuted,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}