package com.example.trail2weather.ui.home

data class WeatherResponse(
    val name: String,
    val coord: Coord?,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val timezone: Int,
    val sys: Sys
)

data class Coord(
    val lon: Double,
    val lat: Double
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Sys(
    val sunrise: Long,
    val sunset: Long
)
