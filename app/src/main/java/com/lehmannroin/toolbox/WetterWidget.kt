package com.lehmannroin.toolbox

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import com.squareup.picasso.Picasso

/**
 * Implementation of App Widget functionality.
 */
class WetterWidget : AppWidgetProvider() {

    private lateinit var imageViewIcon: ImageView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewDegrees: TextView

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "UpdateWidgetData") {
            val temperature = intent.getStringExtra("Temperature")
            val cityName = intent.getStringExtra("Cityname")
            val iconUrl = intent.getStringExtra("IconUrl")

/*            Picasso.get().load(iconUrl).fit().centerCrop().into(imageViewIcon)
            textViewDegrees.text = "$temperature"
            textViewLocation.text = "$cityName"*/

            // Hier können Sie die empfangenen Daten verarbeiten
            Log.d("WetterWidget", "Temperature: $temperature, City: $cityName, Icon URL: $iconUrl")
        }

    }

    internal fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.wetter_widget)
        views.setTextViewText(R.id.textViewDegres, "test")
        views.setTextViewText(R.id.textViewLocation, "test2")

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}