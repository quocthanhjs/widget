package vn.dqt.lunarweather

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import java.util.Locale

object DeviceLocation {
    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(activity: Activity, requestCode: Int) {
        activity.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            requestCode
        )
    }

    fun update(activity: Activity, onComplete: (String) -> Unit = {}) {
        if (!hasPermission(activity)) {
            requestPermission(activity, 4102)
            onComplete("Cần cấp quyền vị trí")
            return
        }
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        if (providers.isEmpty()) {
            onComplete("Vui lòng bật Vị trí/GPS trên điện thoại")
            return
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                saveLocation(activity, location, onComplete)
            }
            @Deprecated("Deprecated by Android")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var requested = false
                providers.forEach { provider ->
                    try {
                        manager.getCurrentLocation(provider, CancellationSignal(), activity.mainExecutor) { location ->
                            if (location != null) saveLocation(activity, location, onComplete)
                        }
                        requested = true
                    } catch (e: SecurityException) {
                        Log.w("DQTLocation", "Location permission not available", e)
                    }
                }
                if (!requested) onComplete("Không lấy được vị trí; kiểm tra quyền GPS")
            } else {
                val last = providers.mapNotNull { provider ->
                    runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
                }.maxByOrNull { it.time }
                if (last != null) {
                    saveLocation(activity, last, onComplete)
                } else {
                    manager.requestSingleUpdate(providers.first(), listener, activity.mainLooper)
                    onComplete("Đang lấy vị trí từ điện thoại…")
                }
            }
        } catch (e: SecurityException) {
            onComplete("Chưa được cấp quyền vị trí")
        } catch (e: Exception) {
            onComplete("Không lấy được vị trí thiết bị")
            Log.w("DQTLocation", "Location request failed", e)
        }
    }

    private fun saveLocation(context: Context, location: Location, onComplete: (String) -> Unit) {
        val prefs = context.getSharedPreferences("dqt_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("device_latitude", location.latitude.toString())
            .putString("device_longitude", location.longitude.toString())
            .apply()

        Thread {
            val label = try {
                val geocoder = Geocoder(context, Locale("vi", "VN"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val a = addresses?.firstOrNull()
                listOfNotNull(a?.locality, a?.subAdminArea, a?.adminArea, a?.countryName)
                    .distinct().take(2).joinToString(", ")
                    .ifBlank { "Vị trí điện thoại" }
            } catch (_: Exception) {
                "Vị trí điện thoại"
            }
            prefs.edit().putString("location_label", label).apply()
            WeatherClient.refresh(context)
            WidgetUpdater.refreshAll(context)
            (context as? Activity)?.runOnUiThread { onComplete(label) }
        }.start()
    }
}
