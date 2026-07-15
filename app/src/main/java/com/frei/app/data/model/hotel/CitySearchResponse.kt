package com.frei.app.data.model.hotel

import com.google.gson.annotations.SerializedName

data class CitySearchResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("items") val items: List<City> = emptyList()
)

data class City(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("countryCode") val countryCode: String = "",
    @SerializedName("timezone") val timezone: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("image") val image: String? = null
)

data class HotelSearchResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("items") val items: List<Hotel> = emptyList()
)

data class Hotel(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("cityId") val cityId: String = "",
    @SerializedName("starRating") val starRating: Int = 0,
    @SerializedName("userRating") val userRating: Double = 0.0,
    @SerializedName("pricePerNight") val pricePerNight: Int = 0,
    @SerializedName("currency") val currency: String = "",
    @SerializedName("amenities") val amenities: List<String> = emptyList(),
    @SerializedName("image") val image: String? = null,
    @SerializedName("address") val address: String = ""
)