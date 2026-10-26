package com.example.payment.API

import com.example.payment.Utils.SECRET_KEY
import com.example.payment.models.CustomersModel
import com.example.payment.models.PaymentIntentModel
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

// Historical REST experiment. Customer and PaymentIntent creation belong on a server.
interface ApiInterface {

    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/customers")
    suspend fun getCustomer() : Response<CustomersModel>

    @Headers(
        "Authorization: Bearer $SECRET_KEY",
        "Stripe-Version: 2023-10-16"
    )
    @POST("v1/ephemeral_keys")
    suspend fun getEphemeralKey(
        @Query("customer") customer: String
    ) : Response<CustomersModel>

    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/payment_intents")
    suspend fun getPaymentIntent(
        @Query("customer") customer: String,
        @Query("amount") amount: String = "1099",
        @Query("currency") currency: String = "usd",
        @Query("automatic_payment_methods[enabled]") automatePay: Boolean = true,
    ) : Response<PaymentIntentModel>
}