package com.frei.app.di

import com.frei.app.BuildConfig
import com.frei.app.data.remote.FlightRemoteDataSource
import com.frei.app.data.remote.api.CityApiService
import com.frei.app.data.remote.api.FlightApiService
import com.frei.app.data.remote.api.HotelApiService
import com.frei.app.data.remote.api.PaymentApiService
import com.frei.app.data.remote.network.HotelRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

object FreiApiPaths {
    const val BASE_URL = "https://frei.onrender.com/"

    const val CITIES = "cities"
    const val CITY_BY_ID = "cities/{cityId}"
    const val CITY_HOTELS = "cities/{cityId}/hotels"
    const val CITY_ATTRACTIONS = "cities/{cityId}/attractions"

    const val AIRPORTS = "airports"
    const val AIRPORT_BY_CODE = "airports/{code}"

    const val FLIGHTS = "flights"
    const val FLIGHT_BY_ID = "flights/{flightId}"

    const val HOTEL_BY_ID = "hotels/{hotelId}"
    const val BOOKINGS = "bookings"
    const val BOOKING_BY_ID = "bookings/{bookingId}"
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FreiApiPaths.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFlightApiService(retrofit: Retrofit): FlightApiService =
        retrofit.create(FlightApiService::class.java)

    @Provides
    @Singleton
    fun provideCityApiService(retrofit: Retrofit): CityApiService =
        retrofit.create(CityApiService::class.java)

    @Provides
    @Singleton
    fun provideFlightRemoteDataSource(apiService: FlightApiService): FlightRemoteDataSource =
        FlightRemoteDataSource(apiService)

    @Provides
    @Singleton
    fun provideHotelApiService(retrofit: Retrofit): HotelApiService =
        retrofit.create(HotelApiService::class.java)

    @Provides
    @Singleton
    fun provideHotelRemoteDataSource(apiService: HotelApiService): HotelRemoteDataSource =
        HotelRemoteDataSource(apiService)

    @Provides
    @Singleton
    fun providePaymentApiService(freiRetrofit: Retrofit): PaymentApiService =
        freiRetrofit.create(PaymentApiService::class.java)
}