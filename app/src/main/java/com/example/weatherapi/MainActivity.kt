package com.example.weatherapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val apiKey = "4ad96273d6564aababd2a351a30fd3c2"
    private lateinit var weatherService: WeatherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)


        GlobalScope.launch(Dispatchers.IO) {
            val weatherData = weatherService.getWeather("Athens", apiKey)
            withContext(Dispatchers.Main) {
                updateUI(weatherData)
            }
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        runOnUiThread {
            findViewById<TextView>(R.id.textViewCity).text = weatherData.name
            findViewById<TextView>(R.id.textViewTemperature).text =
                "${weatherData.main.temp.toInt()}°C"
            val iconUrl = "https://openweathermap.org/img/w/${weatherData.weather[0].icon}.png"
            Glide.with(this)
                .load(iconUrl)
                .into(findViewById(R.id.imageViewWeatherIcon))
        }
    }

    data class WeatherData(
        val name: String,
        val main: Main,
        val weather: List<Weather>
    )

    data class Main(
        val temp: Double
    )

    data class Weather(
        val icon: String
    )

}



