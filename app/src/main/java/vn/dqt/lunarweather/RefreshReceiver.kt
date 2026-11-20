package vn.dqt.lunarweather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RefreshReceiver:BroadcastReceiver(){
    override fun onReceive(context:Context,intent:Intent?){
        WidgetUpdater.refreshAll(context)
        if (intent?.action == "vn.dqt.lunarweather.SECOND_TICK") {
            RefreshScheduler.scheduleSecondTick(context)
        } else {
            WeatherClient.refresh(context)
        }
    }
}
