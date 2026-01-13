package com.example.ascendlifequest.ui.components

import com.example.ascendlifequest.data.remote.WeatherApi
import com.example.ascendlifequest.data.model.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://api.open-meteo.com/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api: WeatherApi = retrofit.create(WeatherApi::class.java)

// Modified to accept a callback that will receive the temperature and description (or nulls on error)
fun fetchWeather(lat: Double, lon: Double, onResult: (temp: Double?, description: String?) -> Unit) {
    api.getWeather(lat, lon).enqueue(object : Callback<WeatherResponse> {
        override fun onResponse(
            call: Call<WeatherResponse>,
            response: Response<WeatherResponse>
        ) {
            val data = response.body()?.current_weather
            if (data != null) {
                val temp = data.temperature
                val code = data.weathercode

                val description = weatherCodeToText(code)

                // Retourne les données via le callback
                onResult(temp, description)

                println("Température: $temp°C")
                println("Météo: $description")
            }
            else {
                println("Aucune donnée météo disponible")
                onResult(null, null)
            }
        }

        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
            println("Erreur : ${t.message}")
            onResult(null, null)
        }
    })
}

fun weatherCodeToText(code: Int): String {
    return when (code) {
        0 -> "Soleil"
        in 1..3 -> "Nuageux"
        in 45..48 -> "Brouillard"
        in 51..57 -> "Bruine"
        in 61..67 -> "Pluie"
        in 71..77 -> "Neige"
        in 80..82 -> "Averses"
        in 95..99 -> "Orages"
        else -> "Inconnu"
    }
}
