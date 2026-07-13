package com.frei.app.data.remote.api

import com.frei.app.data.model.hotel.HotelSearchResponse
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.remote.FreiApiPaths
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HotelApiService {

    @GET(FreiApiPaths.CITY_HOTELS)
    suspend fun searchHotelsInCity(
        @Path("cityId") cityId: String,
        @Query("minStars") minStars: Double? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<HotelSearchResponse>

    @GET(FreiApiPaths.HOTEL_BY_ID)
    suspend fun getHotel(@Path("hotelId") hotelId: String): Response<Hotel>
}