package vn.dqt.lunarweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import android.content.Intent
import android.app.PendingIntent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object WidgetUpdater {
    fun refreshAll(context:Context) {
        val prefs=context.getSharedPreferences("dqt_settings",Context.MODE_PRIVATE)
        val design=prefs.getInt("design",0)
        val layout=when(design) {
            1 -> R.layout.widget_analog
            2 -> R.layout.widget_minimal
            else -> R.layout.widget_digital
        }
        update(context,DigitalWidgetProvider::class.java,layout,design)
        update(context,AnalogWidgetProvider::class.java,layout,design)
        update(context,MinimalWidgetProvider::class.java,layout,design)
    }
    private fun update(context:Context,provider:Class<*>,layout:Int,design:Int) {
        val manager=AppWidgetManager.getInstance(context)
        val ids=manager.getAppWidgetIds(ComponentName(context,provider))
        if(ids.isEmpty()) return
        val views=buildViews(context,layout,design)
        ids.forEach{manager.updateAppWidget(it,views)}
    }
    private fun buildViews(context:Context,layout:Int,design:Int):RemoteViews {
        val v=RemoteViews(context.packageName,layout)
        val tz=TimeZone.getTimeZone("Asia/Ho_Chi_Minh"); val locale=Locale("vi","VN")
        val now=Date(); val cal=Calendar.getInstance(tz).apply{time=now}
        val prefs=context.getSharedPreferences("dqt_settings",Context.MODE_PRIVATE)
        val solar=SimpleDateFormat("EEEE, dd 'tháng' M, yyyy",locale).apply{timeZone=tz}.format(now)
        val lunar=LunarCalendar.fromSolar(cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1,cal.get(Calendar.YEAR))
        val lunarText="Ngày ${lunar.first} tháng ${lunar.second} năm ${lunar.third} (Âm lịch)"
        val phrase=prefs.getString("phrase","Vạn Sự Bình An").orEmpty().ifBlank{"Vạn Sự Bình An"}
        val location=prefs.getString("location_label","Đang lấy vị trí điện thoại…").orEmpty()
        val temp=prefs.getString("weather_temp","--°C").orEmpty()
        val desc=prefs.getString("weather_desc","Nhấn cập nhật vị trí điện thoại").orEmpty()
        val monthTitle=SimpleDateFormat("'Tháng' M, yyyy",locale).apply{timeZone=tz}.format(now)

        if(design==1){
            v.setImageViewBitmap(R.id.analog_face,ClockFace.render(420,prefs.getBoolean("roman",false),prefs.getInt("accent_color",0xFFFFC857.toInt())))
            v.setTextViewText(R.id.phrase,phrase)
            v.setTextViewText(R.id.month_solar,"${cal.get(Calendar.MONTH)+1} / ${cal.get(Calendar.YEAR)} (Dương lịch)")
            v.setTextViewText(R.id.month_lunar,"Tháng ${lunar.second} năm ${lunar.third} (Âm lịch)")
            v.setTextViewText(R.id.analog_weather,"${prefs.getString("weather_icon","🌤️")}  $temp  •  $desc")
            v.setTextViewText(R.id.analog_location,"⌖ $location")
        } else if(design==2) {
            v.setTextViewText(R.id.min_location,"⌖ $location")
            v.setTextViewText(R.id.min_temp,temp.replace("°C","°"))
            v.setTextViewText(R.id.min_icon,prefs.getString("weather_icon","🌤️"))
            v.setTextViewText(R.id.min_desc,desc)
            v.setTextViewText(R.id.min_month_title,"THÁNG ${cal.get(Calendar.MONTH)+1}")
            v.setTextViewText(R.id.min_big_day,String.format(locale,"%02d",cal.get(Calendar.DAY_OF_MONTH)))
            v.setTextViewText(R.id.min_lunar,"${lunar.first}/${lunar.second} • Âm lịch")
            v.setTextViewText(R.id.min_calendar_title,monthTitle.uppercase(locale))
            v.setTextViewText(R.id.min_calendar,monthGrid(cal,locale))
            v.setTextViewText(R.id.min_phrase,phrase)
        } else {
            v.setTextViewText(R.id.solar_date,solar.replaceFirstChar{it.uppercase(locale)})
            v.setTextViewText(R.id.lunar_date,lunarText)
            v.setTextViewText(R.id.month_title,monthTitle)
            v.setTextViewText(R.id.month_grid,monthGrid(cal,locale))
            v.setTextViewText(R.id.temp,temp)
            v.setTextViewText(R.id.weather_icon,prefs.getString("weather_icon","🌤️"))
            v.setTextViewText(R.id.weather_desc,desc)
            v.setTextViewText(R.id.location,"⌖ $location")
        }
        val alpha=prefs.getInt("background_alpha",88).coerceIn(0,100)
        val color=prefs.getInt("background_color",0xFF142D67.toInt())
        val argb=((alpha*255/100) shl 24) or (color and 0x00FFFFFF)
        runCatching{v.setInt(R.id.widget_root,"setBackgroundColor",argb)}
        val click=Intent(context,SettingsActivity::class.java).putExtra("open_weather",true)
        val pi=PendingIntent.getActivity(context,design+1,click,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val clickIds=when(design) {
            1 -> listOf(R.id.analog_weather,R.id.analog_location)
            2 -> listOf(R.id.min_icon,R.id.min_temp,R.id.min_desc,R.id.min_location)
            else -> listOf(R.id.weather_icon,R.id.temp,R.id.weather_desc,R.id.location)
        }
        clickIds.forEach{runCatching{v.setOnClickPendingIntent(it,pi)}}
        return v
    }
    private fun monthGrid(cal:Calendar,locale:Locale):String {
        val c=cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH,1)
        val first=(c.get(Calendar.DAY_OF_WEEK)+5)%7
        val max=c.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells=MutableList(42){"  ·  "}
        for(day in 1..max){
            val lunar=LunarCalendar.fromSolar(day,c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR))
            cells[first+day-1]=String.format(locale,"%2d/%02d",day,lunar.first)
        }
        val out=StringBuilder("T2   T3   T4   T5   T6   T7   CN\n")
        for(row in 0 until 6){
            for(col in 0 until 7) out.append(cells[row*7+col]).append(" ")
            if(row<5) out.append("\n")
        }
        return out.toString()
    }

}
