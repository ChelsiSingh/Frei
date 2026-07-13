package com.frei.app.data.remote.dto

data class CreateOrderRequest(
    val amount: Int, // paise
    val currency: String = "INR",
    val receipt: String
)

data class CreateOrderResponse(
    val orderId: String,
    val amount: Int,
    val currency: String,
    val keyId: String
)

data class VerifyPaymentRequest(
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String
)

data class VerifyPaymentResponse(val verified: Boolean)