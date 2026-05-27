package com.stock.analyzer.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface SinaApi {
    @GET("list={codes}")
    suspend fun getQuotes(
        @Path("codes", encoded = true) codes: String
    ): String
}

interface TencentApi {
    @GET("q={code}")
    suspend fun getKline(
        @Path("code", encoded = true) code: String
    ): String
}
