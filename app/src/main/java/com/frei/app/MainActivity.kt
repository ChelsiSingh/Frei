package com.frei.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.frei.app.navigation.FreiNavGraph
import com.frei.app.payment.RazorpayPaymentManager
import com.frei.app.ui.theme.FreiTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    @Inject lateinit var paymentManager: RazorpayPaymentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {

            FreiTheme {

                FreiNavGraph()

            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, data: PaymentData?) {
        paymentManager.onPaymentSuccess(razorpayPaymentId.orEmpty(), data)
    }

    override fun onPaymentError(code: Int, response: String?, data: PaymentData?) {
        paymentManager.onPaymentError(code, response.orEmpty())
    }
}
