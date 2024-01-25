package com.example.weather

import WeatherApiService
import WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherApiClient {
    private val apiService: WeatherApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(WeatherApiService::class.java)
    }

    fun getWeather(city: String, callback: retrofit2.Callback<WeatherResponse>) {
        apiService.getWeather(city, API_KEY).enqueue(callback)
    }

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        private const val API_KEY = "4ad96273d6564aababd2a351a30fd3c2" // Αντικαταστήστε με το δικό σας κλειδί API
    }
}
