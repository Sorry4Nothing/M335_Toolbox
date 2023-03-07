package com.lehmannroin.toolbox

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.INTERNET
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.Exception
import java.net.URL

class Wetter : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val MY_PERMISSIONS_REQUEST_INTERNET = 100
    private lateinit var imageView: ImageView
    private lateinit var textViewDegres: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewTempDetails: TextView
    private lateinit var textViewWeatherDetails: TextView

    lateinit var appWidgetManager: AppWidgetManager
    lateinit var remoteViews: RemoteViews
    lateinit var widget: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wetter)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        imageView = findViewById(R.id.imageViewWetter)
        textViewDegres = findViewById(R.id.textViewDegres)
        textViewLocation = findViewById(R.id.textViewLocatoin)
        textViewTempDetails = findViewById(R.id.textViewTempDetails)
        textViewWeatherDetails = findViewById(R.id.textViewWeatherDetails)

        appWidgetManager = AppWidgetManager.getInstance(this)
        remoteViews = RemoteViews(this.packageName, R.layout.wetter_widget)
        widget = ComponentName(this, WetterWidget::class.java)

        checkPermission()
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }

        if (ContextCompat.checkSelfPermission(this, INTERNET)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(INTERNET),
                MY_PERMISSIONS_REQUEST_INTERNET)
        }


    }

    fun getLocation() {
        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                getWeather(latitude, longitude)
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    900000,
                    100f,
                    locationListener
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            getWeather(latitude, longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }
    }

    fun sendWidgetData(context: Context, temperature: String, cityName: String, iconUrl: String) {
        val intent =  Intent(context, WetterWidget::class.java)
        intent.action = "UpdateWidgetData"
        intent.putExtra("Temperature", temperature)
        intent.putExtra("Cityname", cityName)
        intent.putExtra("IconUrl", iconUrl)
        context.sendBroadcast(intent)
    }

    fun updateWidgetText(temperature: String, cityName: String) {
        remoteViews.setTextViewText(R.id.textViewDegrees, temperature)
        remoteViews.setTextViewText(R.id.textViewLocation, cityName)
        appWidgetManager.updateAppWidget(widget, remoteViews)
    }

    fun updateWidgetImage(bitmap: Bitmap) {
        remoteViews.setImageViewBitmap(R.id.imageViewIcon, bitmap)
        appWidgetManager.updateAppWidget(widget, remoteViews)
    }


    fun getWeather(latInt: Double, longInt: Double) {
        val lat = latInt
        val lon = longInt
        val apiKey = "3d04a8070adc6b4bc67d6759db1e523b"
        val lang = "de"

        val client = WeatherApiClient(apiKey)

        val weatherData = client.getWeatherData(lat, lon, lang)

        if (weatherData != null) {
            val iconURL = "http://openweathermap.org/img/wn/${weatherData.icon}.png"
            Picasso.get().load(iconURL).fit().centerCrop().into(imageView, object : Callback {
                override fun onSuccess() {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = try {
                            BitmapFactory.decodeStream(URL(iconURL).openConnection().getInputStream())
                        } catch (e: IOException) {
                            null
                        }
                        withContext(Dispatchers.Main) {
                            if (bitmap != null) {
                                updateWidgetImage(bitmap)
                            } else {
                                Log.d(iconURL, "Failed to Load Image")
                            }
                        }
                    }
                }

                override fun onError(e: Exception?) {
                    Log.d(iconURL, "Failed to Load Image")
                }
            })
            textViewDegres.text = "${weatherData.temperature}°"
            val temperature = "${weatherData.temperature}°"
            textViewLocation.text = "${weatherData.cityName}"
            val cityname = "${weatherData.cityName}"
            textViewTempDetails.text = "${weatherData.temperature}° Gefühlt wie ${weatherData.feelsLike}°"
            textViewWeatherDetails.text = "${weatherData.description}"
            updateWidgetText(temperature, cityname)
        }
    }

}