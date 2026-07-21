package com.frei.app.presentation.mytrips

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Shared confirmation dialog used before any destructive delete
 * (trip, packing category, expense). Keeps the "are you sure" copy
 * and button styling consistent across screens.
 */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        text = { Text(text = message, color = Color(0xFF6F6C79)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", color = Color(0xFFE0453C), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}