package com.example.test.model.datasource

import com.example.test.api.DemoApi
import com.example.test.base.BaseApiSource
import com.example.test.base.Result
import com.example.test.model.response.HoldingsResponse
import javax.inject.Inject

interface HoldingsRemoteDataSource {
    suspend fun getUserHoldings(): Result<HoldingsResponse>
}

class HoldingsRemoteDataSourceImpl
@Inject constructor(
    private val demoApi: DemoApi
) : HoldingsRemoteDataSource, BaseApiSource() {

    override suspend fun getUserHoldings(): Result<HoldingsResponse> {
        return try {
            getResult {
                demoApi.getHoldings()
            }
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

}