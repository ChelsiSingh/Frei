package com.frei.app.presentation.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.ExpenseCategory
import com.frei.app.data.model.TripOption


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheetContent(
    tripOptions: List<TripOption>,
    onSave: (title: String, category: ExpenseCategory, amount: Double, tripId: String?, tripName: String?) -> Unit,
    onCancel: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedTrip by remember { mutableStateOf<TripOption?>(null) }
    var tripMenuExpanded by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull()
    val isValid = title.isNotBlank() && selectedCategory != null && amount != null && amount > 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(
            "Add Expense",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FreiExpenseColors.Ink
        )
        Spacer(Modifier.height(20.dp))

        // Amount — the visual focal point, large and prefixed.
        OutlinedTextField(
            value = amountText,
            onValueChange = { input -> amountText = input.filter { it.isDigit() || it == '.' } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) },
            prefix = { Text("\u20B9", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = FreiExpenseColors.Ink) },
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = FreiExpenseColors.Ink),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FreiExpenseColors.Teal,
                unfocusedBorderColor = FreiExpenseColors.InkFaint
            )
        )
        Spacer(Modifier.height(14.dp))

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Cafe Rasoi, Udaipur") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FreiExpenseColors.Teal,
                unfocusedBorderColor = FreiExpenseColors.InkFaint
            )
        )
        Spacer(Modifier.height(16.dp))

        Text("Category", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = FreiExpenseColors.InkMuted)
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(ExpenseCategory.values().toList()) { category ->
                CategoryChip(
                    label = category.label,
                    icon = category.icon(),
                    isActive = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Trip", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = FreiExpenseColors.InkMuted)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = tripMenuExpanded,
            onExpandedChange = { tripMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedTrip?.name ?: "No trip",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FreiExpenseColors.Teal,
                    unfocusedBorderColor = FreiExpenseColors.InkFaint
                )
            )
            ExposedDropdownMenu(
                expanded = tripMenuExpanded,
                onDismissRequest = { tripMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("No trip") },
                    onClick = {
                        selectedTrip = null
                        tripMenuExpanded = false
                    }
                )
                tripOptions.forEach { trip ->
                    DropdownMenuItem(
                        text = { Text(trip.name) },
                        onClick = {
                            selectedTrip = trip
                            tripMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(title.trim(), selectedCategory ?: ExpenseCategory.OTHER, amount ?: 0.0, selectedTrip?.id, selectedTrip?.name)
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FreiExpenseColors.Teal,
                contentColor = Color.White,
                disabledContainerColor = FreiExpenseColors.Teal.copy(alpha = 0.4f)
            )
        ) {
            Text("Save", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}