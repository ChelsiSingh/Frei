package com.frei.app.presentation.home.components

data class RecommendedHotel(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val rating: String,
    val price: String,
    val imageUrl: String? = null,
    val host: String,
    val description: String,
    val amenities: List<String> = emptyList()
)