package com.frei.app.presentation.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.frei.app.ui.theme.FreiPurple

data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun FreiBottomBar(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Booking", Icons.Outlined.Flight),
        BottomNavItem("Trips", Icons.Outlined.Luggage),
        BottomNavItem("Profile", Icons.Outlined.Person)
    )

    NavigationBar(
        containerColor = Color.White
    ) {

        items.forEachIndexed { index, item ->

            NavigationBarItem(
                selected = selectedIndex == index,

                onClick = {
                    onItemSelected(index)
                },

                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },

                label = {
                    Text(item.title)
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FreiPurple,
                    selectedTextColor = FreiPurple,
                    indicatorColor = Color(0xFFEDE7FF),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
