package vn.dqt.lunarweather

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class RefreshScheduler{
    companion object{
        fun schedule(context:Context){
            val p=context.getSharedPreferences("dqt_settings",Context.MODE_PRIVATE)
            val minutes=p.getInt("refresh_minutes",30).coerceAtLeast(15)
            val am=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i=Intent(context,RefreshReceiver::class.java).setAction("vn.dqt.lunarweather.REFRESH")
            val pi=PendingIntent.getBroadcast(context,99,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+60_000,
                minutes*60_000L,pi)
            scheduleSecondTick(context)
        }
        fun scheduleSecondTick(context:Context) {
            val prefs=context.getSharedPreferences("dqt_settings",Context.MODE_PRIVATE)
            val am=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent=Intent(context,RefreshReceiver::class.java).setAction("vn.dqt.lunarweather.SECOND_TICK")
            val pi=PendingIntent.getBroadcast(context,100,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            if (!prefs.getBoolean("show_seconds",false) || prefs.getInt("design",0)!=2) {
                am.cancel(pi)
                return
            }
            val trigger=SystemClock.elapsedRealtime()+1000L
            runCatching { am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME,trigger,pi) }
                .onFailure { am.set(AlarmManager.ELAPSED_REALTIME,trigger,pi) }
        }
    }
}
