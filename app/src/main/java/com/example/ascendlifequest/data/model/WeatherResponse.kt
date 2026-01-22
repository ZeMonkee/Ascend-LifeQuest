package com.example.ascendlifequest.data.model

/**
 * API response model for weather data from Open-Meteo.
 *
 * @property current_weather Current weather conditions
 */
data class WeatherResponse(val current_weather: CurrentWeather)

/**
 * Current weather conditions.
 *
 * @property temperature Current temperature in Celsius
 * @property weathercode WMO weather interpretation code
 */
data class CurrentWeather(val temperature: Double, val weathercode: Int)
