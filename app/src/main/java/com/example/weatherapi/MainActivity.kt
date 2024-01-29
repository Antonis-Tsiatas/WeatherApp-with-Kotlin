package com.example.weatherapi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val apiKey = "4ad96273d6564aababd2a351a30fd3c2"
    private lateinit var weatherService: WeatherService
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            requestLocation()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            showError("Location permission not granted")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        }
    }

    private fun requestLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        Log.d("WeatherApp", "User location - Latitude: $latitude, Longitude: $longitude")

                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val weatherData =
                                    weatherService.getWeatherByCoordinates(
                                        latitude.toString(),
                                        longitude.toString(),
                                        apiKey
                                    )
                                withContext(Dispatchers.Main) {
                                    updateUI(weatherData)
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    showError("Error loading weather data")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        findViewById<TextView>(R.id.textViewCity).text = weatherData.name
        findViewById<TextView>(R.id.textViewTemperature).text =
            "${(weatherData.main.temp - 273.15).toInt()}°C"
        val iconUrl = "https://openweathermap.org/img/w/${weatherData.weather[0].icon}.png"
        Glide.with(this)
            .load(iconUrl)
            .into(findViewById(R.id.imageViewWeatherIcon))

        findViewById<TextView>(R.id.textViewHumidityWind).text =
            "Humidity: ${weatherData.main?.humidity ?: "Unknown"}% | Wind Speed: ${weatherData.wind?.speed ?: "Unknown"} m/s"
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    data class WeatherData(
        val name: String,
        val main: Main,
        val weather: List<Weather>,
        val wind: Wind
    )

    data class Main(
        val temp: Double,
        val humidity: Int // Προσθέστε το πεδίο υγρασίας εδώ
    )

    data class Weather(
        val icon: String
    )

    data class Wind(
        val speed: Double
    )
}
