package com.frei.app.data.repository

import com.frei.app.data.remote.api.PaymentApiService
import com.frei.app.data.remote.dto.CreateOrderRequest
import com.frei.app.data.remote.dto.CreateOrderResponse
import com.frei.app.data.remote.dto.VerifyPaymentRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface PaymentRepository {
    suspend fun createOrder(amountRupees: Double, receipt: String): Result<CreateOrderResponse>
    suspend fun verifyPayment(orderId: String, paymentId: String, signature: String): Result<Boolean>
}

class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApiService
) : PaymentRepository {

    override suspend fun createOrder(amountRupees: Double, receipt: String): Result<CreateOrderResponse> = runCatching {
        api.createOrder(CreateOrderRequest(amount = (amountRupees * 100).toInt(), receipt = receipt))
    }

    override suspend fun verifyPayment(orderId: String, paymentId: String, signature: String): Result<Boolean> = runCatching {
        api.verifyPayment(
            VerifyPaymentRequest(orderId, paymentId, signature)
        ).verified
    }
}