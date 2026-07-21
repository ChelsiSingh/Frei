package com.frei.app.presentation.expenses

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.frei.app.data.model.CategoryTotal
import com.frei.app.data.model.Expense
import com.frei.app.data.model.ExpenseCategory
import com.frei.app.data.model.ExpenseSource
import com.frei.app.data.model.TripExpenseGroup
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

internal object FreiExpenseColors {
    val Ink = Color(0xFF1B1B23)
    val InkMuted = Color(0xFF6E6E7C)
    val InkFaint = Color(0xFFA6A6B3)
    val Purple = Color(0xFF6C3FCF)
    val PurpleSoft = Color(0xFFEFE8FC)
    val Teal = Color(0xFF14B8A6)
    val TealSoft = Color(0xFFE1F7F3)
    val Amber = Color(0xFFF59E0B)
    val AmberSoft = Color(0xFFFEF3E0)
    val Blue = Color(0xFF4A7FE8)
    val BlueSoft = Color(0xFFE9EFFD)
    val Surface = Color(0xFFFFFFFF)
    val Background = Color(0xFFE8E7EF)
    val DangerRed = Color(0xFFE23F3F)
}

internal fun ExpenseCategory.icon(): ImageVector = when (this) {
    ExpenseCategory.FLIGHT -> Icons.Filled.Flight
    ExpenseCategory.HOTEL -> Icons.Filled.Hotel
    ExpenseCategory.FOOD -> Icons.Filled.Restaurant
    ExpenseCategory.TRANSPORT -> Icons.Filled.DirectionsCar
    ExpenseCategory.SHOPPING -> Icons.Filled.ShoppingBag
    ExpenseCategory.OTHER -> Icons.Filled.Apps
}

internal fun ExpenseCategory.color(): Color = when (this) {
    ExpenseCategory.FLIGHT -> FreiExpenseColors.Teal
    ExpenseCategory.HOTEL -> FreiExpenseColors.Purple
    ExpenseCategory.FOOD -> FreiExpenseColors.Amber
    ExpenseCategory.TRANSPORT -> FreiExpenseColors.Blue
    ExpenseCategory.SHOPPING -> FreiExpenseColors.Amber
    ExpenseCategory.OTHER -> FreiExpenseColors.InkMuted
}

internal fun ExpenseCategory.colorSoft(): Color = when (this) {
    ExpenseCategory.FLIGHT -> FreiExpenseColors.TealSoft
    ExpenseCategory.HOTEL -> FreiExpenseColors.PurpleSoft
    ExpenseCategory.FOOD -> FreiExpenseColors.AmberSoft
    ExpenseCategory.TRANSPORT -> FreiExpenseColors.BlueSoft
    ExpenseCategory.SHOPPING -> FreiExpenseColors.AmberSoft
    ExpenseCategory.OTHER -> FreiExpenseColors.Background
}

internal fun formatInr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    return "\u20B9${formatter.format(amount)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tripOptions by viewModel.tripOptions.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        ExpensesScreenContent(
            uiState = uiState,
            onBackClick = onBackClick,
            onFilterClick = onFilterClick,
            onEditBudgetClick = { showBudgetDialog = true },
            onViewModeChange = viewModel::setViewMode,
            onCategorySelected = viewModel::setCategoryFilter,
            onDeleteExpense = { expense ->
                viewModel.deleteExpense(
                    expense = expense,
                    onResult = { result ->
                        result.onFailure { error ->
                            Toast.makeText(
                                context,
                                error.message ?: "Couldn't delete the expense. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        )
        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = FreiExpenseColors.Teal,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 22.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add expense")
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = FreiExpenseColors.Background
        ) {
            AddExpenseSheetContent(
                tripOptions = tripOptions,
                onSave = { title, category, amount, tripId, tripName ->
                    viewModel.addManualExpense(
                        title = title,
                        category = category,
                        amount = amount,
                        tripId = tripId,
                        tripName = tripName,
                        onResult = { result ->
                            result.onSuccess {
                                showAddSheet = false
                            }.onFailure { error ->
                                Toast.makeText(
                                    context,
                                    error.message ?: "Couldn't save the expense. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = uiState.monthlyBudget,
            onSave = { amount ->
                viewModel.setMonthlyBudget(
                    amount = amount,
                    onResult = { result ->
                        result.onSuccess {
                            showBudgetDialog = false
                        }.onFailure { error ->
                            Toast.makeText(
                                context,
                                error.message ?: "Couldn't save the budget. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            },
            onDismiss = { showBudgetDialog = false }
        )
    }
}

@Composable
private fun SetBudgetDialog(currentBudget: Double, onSave: (Double) -> Unit, onDismiss: () -> Unit) {
    var amountText by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "") }
    val amount = amountText.toDoubleOrNull()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FreiExpenseColors.Surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Monthly Budget", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiExpenseColors.Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Set how much you plan to spend this month",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreiExpenseColors.InkMuted
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() || it == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0") },
                    prefix = { Text("\u20B9") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreiExpenseColors.Teal,
                        unfocusedBorderColor = FreiExpenseColors.InkFaint
                    )
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FreiExpenseColors.Background,
                            contentColor = FreiExpenseColors.Ink
                        )
                    ) {
                        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { amount?.let(onSave) },
                        enabled = amount != null && amount > 0.0,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FreiExpenseColors.Teal,
                            contentColor = Color.White,
                            disabledContainerColor = FreiExpenseColors.Teal.copy(alpha = 0.4f)
                        )
                    ) {
                        Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpensesScreenContent(
    uiState: ExpensesUiState,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onEditBudgetClick: () -> Unit,
    onViewModeChange: (ExpenseViewMode) -> Unit,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiExpenseColors.Background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            TopBar(tripCount = uiState.tripCount, onBackClick = onBackClick, onFilterClick = onFilterClick)
            ViewModeToggle(selected = uiState.viewMode, onSelected = onViewModeChange)
            BudgetCard(
                spent = uiState.monthlySpent,
                budget = uiState.monthlyBudget,
                categoryTotals = uiState.categoryTotals,
                onEditBudgetClick = onEditBudgetClick
            )
            CategoryChipRow(selected = uiState.selectedCategory, onSelected = onCategorySelected)
        }

        if (uiState.viewMode == ExpenseViewMode.FLAT) {
            uiState.groupedByDate.forEach { (section, expenses) ->
                item { SectionLabel(section) }
                items(expenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        showTripTag = true,
                        onDeleteConfirmed = { onDeleteExpense(expense) }
                    )
                }
            }
        } else {
            items(uiState.groupedByTrip, key = { it.tripId }) { group ->
                TripGroupCard(group = group, onDeleteExpense = onDeleteExpense)
            }
        }
    }
}

@Composable
private fun TopBar(tripCount: Int, onBackClick: () -> Unit, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(38.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, FreiExpenseColors.InkFaint.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiExpenseColors.Ink)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text("Expenses", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = FreiExpenseColors.Ink)
            Text(
                "$tripCount trips this month",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FreiExpenseColors.InkMuted
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = FreiExpenseColors.Surface,
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = FreiExpenseColors.Ink)
            }
        }
    }
}

@Composable
private fun ViewModeToggle(selected: ExpenseViewMode, onSelected: (ExpenseViewMode) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FreiExpenseColors.Surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToggleButton(
            label = "All Expenses",
            isActive = selected == ExpenseViewMode.FLAT,
            modifier = Modifier.weight(1f)
        ) { onSelected(ExpenseViewMode.FLAT) }
        ToggleButton(
            label = "By Trip",
            isActive = selected == ExpenseViewMode.BY_TRIP,
            modifier = Modifier.weight(1f)
        ) { onSelected(ExpenseViewMode.BY_TRIP) }
    }
}

@Composable
private fun ToggleButton(label: String, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) FreiExpenseColors.Ink else Color.Transparent,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 9.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else FreiExpenseColors.InkMuted
        )
    }
}

@Composable
private fun BudgetCard(spent: Double, budget: Double, categoryTotals: List<CategoryTotal>, onEditBudgetClick: () -> Unit) {
    val pct = if (budget > 0) ((spent / budget) * 100).toInt().coerceIn(0, 999) else 0

    Surface(
        modifier = Modifier
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = FreiExpenseColors.Ink
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "SPENT THIS MONTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        IconButton(onClick = onEditBudgetClick, modifier = Modifier.size(18.dp)) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit monthly budget",
                                tint = Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            formatInr(spent),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            " / ${formatInr(budget)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$pct%", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(
                        "of budget",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            CompositionBar(categoryTotals)
            Spacer(Modifier.height(11.dp))
            CompositionLegend(categoryTotals)
        }
    }
}

@Composable
private fun CompositionBar(categoryTotals: List<CategoryTotal>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(6.dp)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        categoryTotals.forEach { total ->
            Box(
                modifier = Modifier
                    .weight(total.share.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(total.category.color())
            )
        }
    }
}

@Composable
private fun CompositionLegend(categoryTotals: List<CategoryTotal>) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        categoryTotals.forEach { total ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(total.category.color())
                )
                Text(
                    "${total.category.label} ${formatInr(total.amount)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun CategoryChipRow(selected: ExpenseCategory?, onSelected: (ExpenseCategory?) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            CategoryChip(label = "All", icon = Icons.Filled.Apps, isActive = selected == null) {
                onSelected(null)
            }
        }
        items(ExpenseCategory.values().toList()) { category ->
            CategoryChip(label = category.label, icon = category.icon(), isActive = selected == category) {
                onSelected(category)
            }
        }
    }
}

@Composable
internal fun CategoryChip(label: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) FreiExpenseColors.Teal else FreiExpenseColors.Surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isActive) Color.White else FreiExpenseColors.InkMuted
            )
            Text(
                label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else FreiExpenseColors.InkMuted
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = FreiExpenseColors.InkFaint
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseRow(
    expense: Expense,
    showTripTag: Boolean,
    flatBackground: Color = FreiExpenseColors.Surface,
    onDeleteConfirmed: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            if (targetValue == SwipeToDismissBoxValue.EndToStart) {
                // Don't let the swipe auto-commit the delete. Reveal the confirm
                // dialog instead, and only remove the row once the user confirms.
                showConfirmDialog = true
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { ExpenseRowDeleteBackground(dismissState) }
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 5.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = flatBackground
        ) {
            Row(
                modifier = Modifier.padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(expense.category.colorSoft()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(expense.category.icon(), contentDescription = null, tint = expense.category.color())
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        expense.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreiExpenseColors.Ink,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        if (showTripTag && expense.tripName != null) {
                            Tag(text = expense.tripName, background = FreiExpenseColors.PurpleSoft, color = FreiExpenseColors.Purple)
                        }
                        if (expense.source == ExpenseSource.AUTO) {
                            Tag(text = "Auto", background = FreiExpenseColors.TealSoft, color = FreiExpenseColors.Teal)
                        } else {
                            Text("Manual", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = FreiExpenseColors.InkMuted)
                        }
                    }
                }

                Text(
                    formatInr(expense.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiExpenseColors.Ink
                )
            }
        }
    }

    if (showConfirmDialog) {
        ExpenseDeleteConfirmDialog(
            title = "Delete expense?",
            message = "\"${expense.title}\" (${formatInr(expense.amount)}) will be removed. This can't be undone.",
            onConfirm = {
                showConfirmDialog = false
                onDeleteConfirmed()
            },
            onDismiss = {
                showConfirmDialog = false
                // Snap the row back closed since the user backed out of the delete.
                scope.launch { dismissState.reset() }
            }
        )
    }
}

@Composable
private fun ExpenseDeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.ExtraBold, color = FreiExpenseColors.Ink) },
        text = { Text(message, color = FreiExpenseColors.InkMuted, fontSize = 13.5.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FreiExpenseColors.DangerRed,
                    contentColor = Color.White
                )
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FreiExpenseColors.Background,
                    contentColor = FreiExpenseColors.Ink
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = FreiExpenseColors.Surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseRowDeleteBackground(dismissState: SwipeToDismissBoxState) {
    val isArmed = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
    val backgroundColor by animateColorAsState(
        targetValue = if (isArmed) FreiExpenseColors.DangerRed else FreiExpenseColors.DangerRed.copy(alpha = 0.35f),
        label = "expenseDeleteBackground"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 22.dp, vertical = 5.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            Icons.Filled.Delete,
            contentDescription = "Delete expense",
            tint = Color.White,
            modifier = Modifier.padding(end = 22.dp)
        )
    }
}

@Composable
private fun Tag(text: String, background: Color, color: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = background) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TripGroupCard(group: TripExpenseGroup, onDeleteExpense: (Expense) -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 22.dp, vertical = 7.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = FreiExpenseColors.Surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(FreiExpenseColors.Purple, Color(0xFF8A5FE0)))
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(group.tripName, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(
                        group.dateRange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Text(formatInr(group.total), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                group.expenses.forEach { expense ->
                    ExpenseRow(
                        expense = expense,
                        showTripTag = false,
                        flatBackground = FreiExpenseColors.Background,
                        onDeleteConfirmed = { onDeleteExpense(expense) }
                    )
                }
            }
        }
    }
}