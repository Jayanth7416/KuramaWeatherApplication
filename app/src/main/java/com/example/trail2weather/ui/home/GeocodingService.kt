package com.example.trail2weather.ui.home


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("json")
    fun getLatLng(
        @Query("address") cityName: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>
}
