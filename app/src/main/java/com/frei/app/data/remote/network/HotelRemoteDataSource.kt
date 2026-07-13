package com.frei.app.data.remote.network

import com.frei.app.data.remote.api.HotelApiService
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.model.hotel.HotelSearchResponse
import retrofit2.Response

class HotelRemoteDataSource(
    private val apiService: HotelApiService
) {
    suspend fun searchHotelsInCity(cityId: String, minStars: Double?): Response<HotelSearchResponse> =
        apiService.searchHotelsInCity(cityId = cityId, minStars = minStars)

    suspend fun getHotel(hotelId: String): Response<Hotel> =
        apiService.getHotel(hotelId)
}