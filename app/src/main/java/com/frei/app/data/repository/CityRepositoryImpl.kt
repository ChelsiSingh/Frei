package com.frei.app.data.repository

import com.frei.app.data.model.hotel.City
import com.frei.app.data.model.hotel.CitySearchResponse
import com.frei.app.data.remote.api.CityApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepositoryImpl @Inject constructor(
    private val api: CityApiService
) : CityRepository {

    private fun <T> Response<T>.getOrThrow(): T {
        if (isSuccessful) return body() ?: throw Exception("Response body was empty")
        throw Exception(errorBody()?.string() ?: "Network call failed with code ${code()}")
    }

    override suspend fun searchCities(query: String): Result<CitySearchResponse> = runCatching {
        api.searchCities(query = query).getOrThrow()
    }

    override suspend fun getCity(cityId: String): Result<City> = runCatching {
        api.getCity(cityId).getOrThrow()
    }
}