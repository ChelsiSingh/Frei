package com.frei.app.data.repository

import com.frei.app.data.model.hotel.City
import com.frei.app.data.model.hotel.CitySearchResponse

interface CityRepository {
    suspend fun searchCities(query: String): Result<CitySearchResponse>
    suspend fun getCity(cityId: String): Result<City>
}