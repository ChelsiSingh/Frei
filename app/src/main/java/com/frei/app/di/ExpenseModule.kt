package com.frei.app.di

import com.frei.app.data.repository.ExpenseRepository
import com.frei.app.data.repository.FirestoreExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExpenseModule {

    @Binds
    abstract fun bindExpenseRepository(
        impl: FirestoreExpenseRepository
    ): ExpenseRepository
}