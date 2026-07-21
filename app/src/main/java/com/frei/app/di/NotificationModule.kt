package com.frei.app.di

import com.frei.app.data.repository.FirestoreNotificationRepository
import com.frei.app.data.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    abstract fun bindNotificationRepository(impl: FirestoreNotificationRepository): NotificationRepository
}