package com.lehmannroin.toolbox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val icon: String,
    val cityName: String
)

class WeatherApiClient(private val apiKey: String) {

    fun getWeatherData(lat: Double, lon: Double, lang: String = "de"): WeatherData? {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=$lang&type=current"

        var weatherData: WeatherData? = null

        val thread = Thread(Runnable {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()

                while (true) {
                    val line = reader.readLine() ?: break
                    response.append(line)
                }

                reader.close()
                connection.disconnect()

                val json = JSONObject(response.toString())

                val temperature = json.getJSONObject("main").getDouble("temp")
                val feelsLike = json.getJSONObject("main").getDouble("feels_like")
                val icon = json.getJSONArray("weather").getJSONObject(0).getString("icon")
                val cityName = json.getString("name")

                weatherData = WeatherData(
                    temperature,
                    feelsLike,
                    icon,
                    cityName
                )
            } else {
                connection.disconnect()
            }
        })

        thread.start()
        thread.join()

        return weatherData
    }
}
