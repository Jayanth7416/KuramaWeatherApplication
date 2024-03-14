package com.example.trail2weather.ui.home

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>

    @GET("json")
    fun getLatLng(
        @Query("address") cityName: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>

    @GET("weather")
    fun getWeatherByCityName(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>

    // New method for hourly forecast using Open Meteo API
    @GET("forecast")
    fun getOpenMeteoHourlyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String,
        @Query("forecast_days") forecastDays: Int
    ): Call<HourlyForecastResponse>

    @GET("forecast")
    fun getOpenMeteoWeeklyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min"
    ): Call<WeeklyForecastResponse>
}
