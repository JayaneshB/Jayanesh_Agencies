package com.chocowholesale.customer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chocowholesale.customer.ui.theme.ChocoTheme
import com.chocowholesale.customer.navigation.AppNavHost
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener {

    var onPaymentSuccess: ((String) -> Unit)? = null
    var onPaymentFailure: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChocoTheme {
                AppNavHost()
            }
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        paymentId?.let {
            onPaymentSuccess?.invoke(it)
        } ?: Toast.makeText(this, "Payment succeeded but no ID received", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentError(code: Int, message: String?) {
        onPaymentFailure?.invoke(message ?: "Payment failed (code: $code)")
    }
}
