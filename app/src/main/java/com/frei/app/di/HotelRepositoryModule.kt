package com.frei.app.di

import com.frei.app.data.repository.HotelRepository
import com.frei.app.data.repository.HotelRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HotelRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHotelRepository(impl: HotelRepositoryImpl): HotelRepository
}