package com.frei.app.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthGateBottomSheet(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRegister by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {

        Box(modifier = Modifier.fillMaxHeight(0.92f)) {
            if (showRegister) {
                RegisterScreen(
                    onNavigateToLogin = { showRegister = false },
                    onRegisterSuccess = onAuthSuccess
                )
            } else {
                LoginScreen(
                    onNavigateToRegister = { showRegister = true },
                    onLoginSuccess = onAuthSuccess
                )
            }
        }
    }
}