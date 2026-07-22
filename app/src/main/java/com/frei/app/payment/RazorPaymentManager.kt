package com.frei.app.payment

import android.app.Activity
import com.razorpay.Checkout
import com.razorpay.PaymentData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RazorpayResult {
    data class Success(val paymentId: String, val orderId: String, val signature: String) : RazorpayResult
    data class Failure(val code: Int, val description: String) : RazorpayResult
}

@Singleton
class RazorpayPaymentManager @Inject constructor() {

    private val _results = MutableSharedFlow<RazorpayResult>(replay = 0, extraBufferCapacity = 1)
    val results: SharedFlow<RazorpayResult> = _results

    fun startCheckout(
        activity: Activity,
        keyId: String,
        orderId: String,
        amountPaise: Int,
        name: String,
        description: String,
        prefillEmail: String,
        prefillContact: String,
        preferredMethod: String? = null
    ) {
        val checkout = Checkout()
        checkout.setKeyID(keyId)

        val options = JSONObject().apply {
            put("name", name)
            put("description", description)
            put("order_id", orderId)
            put("currency", "INR")
            put("amount", amountPaise)
            put("prefill", JSONObject().apply {
                put("email", prefillEmail)
                put("contact", prefillContact)
                if (!preferredMethod.isNullOrBlank()) put("method", preferredMethod)
            })
        }

        try {
            checkout.open(activity, options)
        } catch (e: Exception) {
            _results.tryEmit(RazorpayResult.Failure(-1, e.message ?: "Could not open checkout."))
        }
    }


    fun onPaymentSuccess(paymentId: String, paymentData: PaymentData?) {
        android.util.Log.d(
            "RazorpayDebug",
            "paymentId=$paymentId orderId=${paymentData?.orderId} signature=${paymentData?.signature}"
        )
        _results.tryEmit(
            RazorpayResult.Success(
                paymentId = paymentId,
                orderId = paymentData?.orderId.orEmpty(),
                signature = paymentData?.signature.orEmpty()
            )
        )
    }

    fun onPaymentError(code: Int, description: String) {
        val userMessage = parseRazorpayError(description)
        _results.tryEmit(RazorpayResult.Failure(code, userMessage))
    }

    private fun parseRazorpayError(rawResponse: String): String {
        return try {
            val json = JSONObject(rawResponse)
            val error = json.optJSONObject("error")
            val reason = error?.optString("reason").orEmpty()
            val description = error?.optString("description").orEmpty()

            when {
                reason == "payment_error" -> "Payment could not be completed. Please check your payment details and try again."
                reason.isNotBlank() -> reason.replace("_", " ").replaceFirstChar { it.uppercase() }
                description.isNotBlank() && description != "undefined" -> description
                else -> "Payment failed. Please try again."
            }
        } catch (e: Exception) {
            rawResponse.ifBlank { "Payment failed. Please try again." }
        }
    }
}