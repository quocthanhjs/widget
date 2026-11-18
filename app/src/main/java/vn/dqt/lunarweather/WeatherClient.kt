package vn.dqt.lunarweather

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Weather is queried from Open-Meteo using the phone's saved GPS/network coordinates.
 * No AccuWeather key or Location Key is required.
 */
object WeatherClient {
    fun refresh(context: Context) {
        val prefs = context.getSharedPreferences("dqt_settings", Context.MODE_PRIVATE)
        val lat = prefs.getString("device_latitude", null)?.toDoubleOrNull()
        val lon = prefs.getString("device_longitude", null)?.toDoubleOrNull()

        if (lat == null || lon == null) {
            prefs.edit()
                .putString("weather_temp", "--°C")
                .putString("weather_desc", "Nhấn cập nhật vị trí điện thoại")
                .putString("weather_forecast", "Chưa có vị trí thiết bị")
                .apply()
            WidgetUpdater.refreshAll(context)
            return
        }

        Thread {
            try {
                val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${String.format(Locale.US, "%.5f", lat)}" +
                    "&longitude=${String.format(Locale.US, "%.5f", lon)}" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                    "&timezone=Asia%2FHo_Chi_Minh&forecast_days=7"
                val data = JSONObject(get(url))
                val current = data.getJSONObject("current")
                val temp = current.getDouble("temperature_2m").toInt()
                val code = current.getInt("weather_code")
                val desc = description(code)
                val icon = icon(code, current.optInt("is_day", 1) == 1)
                val daily = data.getJSONObject("daily")
                val dates = daily.getJSONArray("time")
                val mins = daily.getJSONArray("temperature_2m_min")
                val maxs = daily.getJSONArray("temperature_2m_max")
                val codes = daily.getJSONArray("weather_code")
                val rows = mutableListOf<String>()
                for (i in 0 until minOf(dates.length(), 7)) {
                    val date = dates.getString(i).takeLast(5)
                    rows.add("$date ${mins.getDouble(i).toInt()}°/${maxs.getDouble(i).toInt()}° ${icon(codes.getInt(i), true)}")
                }
                prefs.edit()
                    .putString("weather_temp", "$temp°C")
                    .putString("weather_desc", desc)
                    .putString("weather_icon", icon)
                    .putString("weather_forecast", rows.joinToString("   •   "))
                    .putLong("weather_updated", System.currentTimeMillis())
                    .apply()
                WidgetUpdater.refreshAll(context)
            } catch (e: Exception) {
                Log.w("DQTWeather", "Open-Meteo refresh failed", e)
                prefs.edit().putString("weather_desc", "Không cập nhật được thời tiết").apply()
                WidgetUpdater.refreshAll(context)
            }
        }.start()
    }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        try {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun description(code: Int): String = when (code) {
        0 -> "Trời quang"
        1 -> "Ít mây"
        2 -> "Có mây"
        3 -> "Nhiều mây"
        45, 48 -> "Sương mù"
        51, 53, 55 -> "Mưa phùn"
        56, 57 -> "Mưa phùn đóng băng"
        61, 63, 65 -> "Mưa"
        66, 67 -> "Mưa đóng băng"
        71, 73, 75, 77 -> "Tuyết"
        80, 81, 82 -> "Mưa rào"
        85, 86 -> "Mưa tuyết"
        95, 96, 99 -> "Dông"
        else -> "Thời tiết thay đổi"
    }

    private fun icon(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1, 2 -> if (isDay) "🌤️" else "☁️"
        3, 45, 48 -> "☁️"
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
        71, 73, 75, 77, 85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}
