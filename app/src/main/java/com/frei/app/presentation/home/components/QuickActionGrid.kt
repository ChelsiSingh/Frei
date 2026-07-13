package com.frei.app.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frei.app.presentation.home.model.QuickAction
import com.frei.app.ui.theme.FreiPurple
import com.frei.app.ui.theme.FreiTeal

@Composable
fun QuickActionGrid(
    onAddTripClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onPackingClick: () -> Unit,
    onExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val actionItems = listOf(
        QuickAction("New Trip", Icons.Default.AddCircle, FreiPurple, Color.White, Color.White) to onAddTripClick,
        QuickAction("Packing", Icons.Default.Luggage, Color.White, FreiTeal, Color.Black) to onPackingClick,
        QuickAction("Expenses", Icons.Default.AccountBalanceWallet, Color.White, FreiPurple, Color.Black) to onExpensesClick,
        QuickAction("Bookings", Icons.Default.Flight, FreiTeal, Color.White, Color.White) to onBookingsClick
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        actionItems.chunked(2).forEach { rowPairs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPairs.forEach { (action, onClickAction) ->
                    QuickActionCard(
                        action = action,
                        modifier = Modifier.weight(1f),
                        onClick = onClickAction
                    )
                }
            }
        }
    }
}