package com.example.payment.API

import com.example.payment.Utils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object ApiUtilities {

    fun getApiInterface() : ApiInterface{
        check(Utils.PAYMENT_BACKEND_CONFIGURED) { "支付實驗未設定後端" }
        return Retrofit.Builder()
            .baseUrl("https://api.stripe.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiInterface::class.java)
    }
}