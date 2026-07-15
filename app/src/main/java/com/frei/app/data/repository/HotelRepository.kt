package com.frei.app.data.repository

import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.model.hotel.HotelSearchResponse

interface HotelRepository {
    suspend fun getHotel(hotelId: String): Result<Hotel>
    suspend fun searchHotelsInCity(cityId: String, minStars: Double? = null): Result<HotelSearchResponse>

    suspend fun getRecommendedHotels(cityId: String? = null): Result<HotelSearchResponse>
}
