package com.frei.app.presentation.newtrip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun TripCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit = {},
    onClick: (() -> Unit)? = null
) {

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .then(
                        if (onClick != null)
                            Modifier.clickable {
                                onClick()
                            }
                        else Modifier
                    ),
                readOnly = readOnly,
                singleLine = true,

                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                },

                colors = OutlinedTextFieldDefaults.colors(),

                shape = MaterialTheme.shapes.medium
            )

        }

    }

}