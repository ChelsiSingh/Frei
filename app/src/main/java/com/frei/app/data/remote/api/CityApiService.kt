package com.frei.app.data.remote.api


import com.frei.app.data.model.hotel.City
import com.frei.app.data.model.hotel.CitySearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.frei.app.data.remote.FreiApiPaths

interface CityApiService {

    @GET(FreiApiPaths.CITIES)
    suspend fun searchCities(
        @Query("q") query: String? = null,
        @Query("countryCode") countryCode: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<CitySearchResponse>

    @GET(FreiApiPaths.CITY_BY_ID)
    suspend fun getCity(@Path("cityId") cityId: String): Response<City>
}