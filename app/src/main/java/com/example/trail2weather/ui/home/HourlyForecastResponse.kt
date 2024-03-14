package com.example.trail2weather.ui.home

import com.google.gson.annotations.SerializedName

data class HourlyForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly_units: HourlyUnits,
    val hourly: Hourly
)

data class HourlyUnits(
    val time: String,
    val temperature_2m: String
)

data class Hourly(
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature_2m: List<Double>  // Renamed to match API response
)
