package com.example.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.payment.API.ApiUtilities
import com.example.payment.Utils.PUBLISHABLE_KEY
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.model.PaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var paymentSheet : PaymentSheet
    private lateinit var customerId: String
    private lateinit var ephemeralKey: String
    private lateinit var clientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val  button = findViewById<Button>(R.id.button)
        button.setOnClickListener{
            paymentFlow()
        }

        if (!paymentBackendConfigured()) {
            showPaymentUnavailable()
            return
        }
        PaymentConfiguration.init(this, PUBLISHABLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        getCustomerId()
    }

    private val apiInterface by lazy { ApiUtilities.getApiInterface() }

    private fun getCustomerId() {
        if (!paymentBackendConfigured()) return
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getCustomer()

            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    customerId = res.body()!!.id
                    getEphemeralKey(customerId)
                }
            }
        }
    }

    private fun getEphemeralKey(customerId: String) {
        if (!paymentBackendConfigured()) return
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getEphemeralKey(customerId)

            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    ephemeralKey = res.body()!!.id
                    getPaymentIntent(customerId, ephemeralKey)
                }
            }
        }
    }

    private fun getPaymentIntent(customerId: String, ephemeralKey: String) {
        if (!paymentBackendConfigured()) return

        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getPaymentIntent(customerId)

            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    clientSecret = res.body()!!.id
                    Toast.makeText(this@MainActivity, "Proceed for payment", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun paymentFlow() {
        if (!paymentBackendConfigured() || !::paymentSheet.isInitialized ||
            !::customerId.isInitialized || !::ephemeralKey.isInitialized ||
            !::clientSecret.isInitialized) {
            showPaymentUnavailable()
            return
        }
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                "SmallShop",
                PaymentSheet.CustomerConfiguration(
                    customerId, ephemeralKey
                ),
                PaymentSheet.GooglePayConfiguration(
                    PaymentSheet.GooglePayConfiguration.Environment.Test,
                    "US",
                    "USD",
                    null,
                    null,
                ),
                primaryButtonColor = null,
                defaultBillingDetails = null,
                shippingDetails = null,
                allowsDelayedPaymentMethods = false,
                allowsPaymentMethodsRequiringShippingAddress = false,
                appearance = PaymentSheet.Appearance(),
                primaryButtonLabel = null,
                billingDetailsCollectionConfiguration = PaymentSheet.BillingDetailsCollectionConfiguration()
            )
        )
    }

    private fun paymentBackendConfigured(): Boolean {
        return Utils.PAYMENT_BACKEND_CONFIGURED && PUBLISHABLE_KEY.isNotBlank()
    }

    private fun showPaymentUnavailable() {
        Toast.makeText(this, "支付實驗未設定後端", Toast.LENGTH_SHORT).show()
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        if (paymentSheetResult is PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Done", Toast.LENGTH_SHORT).show()
        }
    }
}