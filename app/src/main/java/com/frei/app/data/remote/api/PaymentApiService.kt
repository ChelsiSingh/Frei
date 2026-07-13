package com.frei.app.data.remote.api

import com.frei.app.data.remote.dto.CreateOrderRequest
import com.frei.app.data.remote.dto.CreateOrderResponse
import com.frei.app.data.remote.dto.VerifyPaymentRequest
import com.frei.app.data.remote.dto.VerifyPaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApiService {
    @POST("payments/create-order")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse

    @POST("payments/verify")
    suspend fun verifyPayment(@Body request: VerifyPaymentRequest): VerifyPaymentResponse

}
