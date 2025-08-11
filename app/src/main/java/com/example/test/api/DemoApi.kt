package com.example.test.api

import com.example.test.model.response.HoldingsResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Retrofit API interface for Demo trading platform.
 * Defines network endpoints for interacting with the Demo API.
 */
interface DemoApi {

    /**
     * Retrieves user's holdings from the Demo API.
     * 
     * @return [Response] containing [HoldingsResponse] with user's holdings data
     */
    @GET("/")
    suspend fun getHoldings(): Response<HoldingsResponse>
}