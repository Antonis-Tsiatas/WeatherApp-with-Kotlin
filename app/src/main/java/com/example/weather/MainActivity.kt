package com.example.weather


import WeatherResponse
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class MainActivity : AppCompatActivity() {

    private lateinit var btnGetWeather: Button
    private lateinit var textCity: TextView
    private lateinit var textTemperature: TextView
    private lateinit var textDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGetWeather = findViewById(R.id.btnGetWeather)
        textCity = findViewById(R.id.textCity)
        textTemperature = findViewById(R.id.textTemperature)
        textDescription = findViewById(R.id.textDescription)

        btnGetWeather.setOnClickListener {
            val city = "Athens"
            if (city.isNotEmpty()) {
                val weatherApiClient = WeatherApiClient()
                weatherApiClient.getWeather(city, object : Callback<WeatherResponse> {
                    override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                        if (response.isSuccessful) {
                            val weatherResponse = response.body()
                            if (weatherResponse != null) {
                                updateUI(weatherResponse)
                            }
                        }
                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {

                    }
                })
            }
        }
    }

    private fun updateUI(weatherResponse: WeatherResponse) {
        textCity.text = "City: Athens"
        val temperatureCelsius = weatherResponse.main.temp - 273.15
        textTemperature.text = "Temperature: ${String.format("%.2f", temperatureCelsius)}°C"
        textDescription.text = "Description: ${weatherResponse.weather[0].description}"

        textTemperature.visibility = TextView.VISIBLE
        textDescription.visibility = TextView.VISIBLE
    }

}
