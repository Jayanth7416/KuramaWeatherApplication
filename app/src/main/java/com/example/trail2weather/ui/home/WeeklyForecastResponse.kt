package com.example.trail2weather.ui.home

data class WeeklyForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val daily_units: DailyUnits,
    val daily: DailyForecast
)

data class DailyUnits(
    val time: String,
    val temperature_2m_max: String,
    val temperature_2m_min: String
)

data class DailyForecast(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)
