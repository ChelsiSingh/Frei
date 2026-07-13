package com.frei.app

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun FreiLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.frei_logo),
        contentDescription = "Frei Logo",
        modifier = modifier
    )
}