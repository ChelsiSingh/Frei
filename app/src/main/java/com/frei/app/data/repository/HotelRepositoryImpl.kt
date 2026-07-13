package com.frei.app.data.repository

import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.model.hotel.HotelSearchResponse
import com.frei.app.data.remote.network.HotelRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotelRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val api: HotelRemoteDataSource
) : HotelRepository {

    private fun <T> Response<T>.getOrThrow(): T {
        if (isSuccessful) return body() ?: throw Exception("Response body was empty")
        throw Exception(errorBody()?.string() ?: "Network call failed with code ${code()}")
    }

    override suspend fun searchHotelsInCity(cityId: String, minStars: Double?): Result<HotelSearchResponse> = runCatching {
        api.searchHotelsInCity(cityId, minStars).getOrThrow()
    }

    override suspend fun getHotel(hotelId: String): Result<Hotel> = runCatching {
        api.getHotel(hotelId).getOrThrow()
    }
}