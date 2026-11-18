package vn.dqt.lunarweather
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetManager
import android.content.Context
class DigitalWidgetProvider:AppWidgetProvider(){
    override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){
        WidgetUpdater.refreshAll(context);WeatherClient.refresh(context);RefreshScheduler.schedule(context)
    }
}
