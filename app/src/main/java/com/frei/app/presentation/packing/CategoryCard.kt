package com.frei.app.presentation.packing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FreiInk = Color(0xFF1B1830)
val FreiPurple = Color(0xFF6C3FCF)
val FreiTeal = Color(0xFF14B8A6)

@Composable
fun CategoryCard(
    category: PackingCategory,
    onAddItemClick: () -> Unit,
    onItemToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category Header Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreiInk
                )
                IconButton(onClick = onAddItemClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add sub-item",
                        tint = FreiPurple
                    )
                }
            }

            if (category.items.isEmpty()) {
                Text(
                    text = "No items added yet.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))

                // PERFORMANCE OPTIMIZATION: Prevent lambda allocations on recomposition loops
                category.items.forEach { item ->
                    val itemId = item.id
                    val toggleClick = remember(itemId, onItemToggle) { { onItemToggle(itemId) } }

                    SubItemRow(
                        item = item,
                        onCheckedChange = toggleClick
                    )
                }
            }
        }
    }
}

@Composable
fun SubItemRow(
    item: PackingItem,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCheckedChange)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isPacked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = "Toggle Packed State",
            tint = if (item.isPacked) FreiTeal else Color.LightGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.name,
            fontSize = 14.sp,
            color = if (item.isPacked) Color.Gray else FreiInk,
            fontWeight = FontWeight.Medium
        )
    }
}