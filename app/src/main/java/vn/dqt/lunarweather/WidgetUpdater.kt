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
            1 -> R.layout.widget_minimal
            2 -> R.layout.widget_analog
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
        val lunarText="Ngày ${lunar.first} tháng ${lunar.second} năm ${lunar.third}"
        val phrase=prefs.getString("phrase","Vạn Sự Bình An").orEmpty().ifBlank{"Vạn Sự Bình An"}
        val location=prefs.getString("location_label","Đang lấy vị trí điện thoại…").orEmpty()
        val temp=prefs.getString("weather_temp","--°C").orEmpty()
        val desc=prefs.getString("weather_desc","Nhấn cập nhật vị trí điện thoại").orEmpty()
        val monthTitle=SimpleDateFormat("'Tháng' M, yyyy",locale).apply{timeZone=tz}.format(now)

        if(design==2){
            v.setImageViewBitmap(R.id.analog_face,ClockFace.render(420,prefs.getBoolean("roman",false),prefs.getInt("accent_color",0xFFFFC857.toInt()),showSeconds=prefs.getBoolean("show_seconds",false)))
            v.setTextViewText(R.id.phrase,phrase)
            v.setTextViewText(R.id.month_solar,"${cal.get(Calendar.DAY_OF_MONTH)} / ${cal.get(Calendar.MONTH)+1} / ${cal.get(Calendar.YEAR)}")
            v.setTextViewText(R.id.month_lunar,"${lunar.first} / ${lunar.second} / ${lunar.third}")
            v.setTextViewText(R.id.analog_weather,"${prefs.getString("weather_icon","🌤️")}  $temp  •  $desc")
            v.setTextViewText(R.id.analog_location,"⌖ $location")
        } else if(design==1) {
            v.setTextViewText(R.id.min_solar_date,solar.replaceFirstChar{it.uppercase(locale)})
            v.setTextViewText(R.id.min_lunar,lunarText)
            v.setTextViewText(R.id.min_weather_icon,prefs.getString("weather_icon","🌤️"))
            v.setTextViewText(R.id.min_temp,temp)
            v.setTextViewText(R.id.min_desc,desc)
            v.setTextViewText(R.id.min_location,"⌖ $location")
            v.setTextViewText(R.id.min_phrase,phrase)
        } else {
            v.setTextViewText(R.id.solar_date,solar.replaceFirstChar{it.uppercase(locale)})
            v.setTextViewText(R.id.lunar_date,lunarText)
            val weekday=SimpleDateFormat("EEEE",locale).apply{timeZone=tz}.format(now).uppercase(locale)
            v.setTextViewText(R.id.calendar_weekday,weekday)
            v.setTextViewText(R.id.calendar_day,cal.get(Calendar.DAY_OF_MONTH).toString())
            v.setTextViewText(R.id.calendar_month,monthTitle.uppercase(locale))
            v.setTextViewText(R.id.calendar_lunar,"${lunar.first} tháng ${lunar.second}, ${lunar.third}")
            v.setTextViewText(R.id.temp,temp)
            v.setTextViewText(R.id.weather_icon,prefs.getString("weather_icon","🌤️"))
            v.setTextViewText(R.id.weather_desc,desc)
            v.setTextViewText(R.id.location,"⌖ $location")
        }
        val alpha=prefs.getInt("background_alpha",88).coerceIn(0,100)
        val color=prefs.getInt("background_color",0xFF142D67.toInt())
        val argb=((alpha*255/100) shl 24) or (color and 0x00FFFFFF)
        runCatching{v.setInt(R.id.widget_root,"setBackgroundColor",argb)}
        val click=Intent(context,SettingsActivity::class.java)
        val pi=PendingIntent.getActivity(context,design+1,click,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val clickIds=when(design) {
            2 -> listOf(R.id.analog_weather,R.id.analog_location)
            1 -> listOf(R.id.min_weather_icon,R.id.min_temp,R.id.min_desc,R.id.min_location)
            else -> listOf(R.id.weather_icon,R.id.temp,R.id.weather_desc,R.id.location)
        }
        // Chạm bất kỳ vị trí nào trên widget đều mở thẳng màn hình cài đặt.
        v.setOnClickPendingIntent(R.id.widget_root, pi)
        clickIds.forEach{runCatching{v.setOnClickPendingIntent(it,pi)}}
        val clockFormat = if (prefs.getBoolean("show_seconds", false)) "HH:mm:ss" else "HH:mm"
        val clockFormat12 = if (prefs.getBoolean("show_seconds", false)) "hh:mm:ss a" else "hh:mm a"
        val clockSize = prefs.getFloat("digital_clock_size", 58f).coerceIn(38f, 82f)
        runCatching { v.setCharSequence(R.id.digital_clock, "setFormat24Hour", clockFormat) }
        runCatching { v.setCharSequence(R.id.digital_clock, "setFormat12Hour", clockFormat12) }
        runCatching { v.setTextViewTextSize(R.id.digital_clock, android.util.TypedValue.COMPLEX_UNIT_SP, (clockSize * 0.72f).coerceIn(28f, 58f)) }
        runCatching { v.setCharSequence(R.id.min_clock, "setFormat24Hour", clockFormat) }
        runCatching { v.setCharSequence(R.id.min_clock, "setFormat12Hour", clockFormat12) }
        runCatching { v.setTextViewTextSize(R.id.min_clock, android.util.TypedValue.COMPLEX_UNIT_SP, clockSize) }
        return v
    }
    private fun monthGrid(cal:Calendar,locale:Locale):String {
        val c=cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH,1)
        val first=(c.get(Calendar.DAY_OF_WEEK)+5)%7
        val max=c.getActualMaximum(Calendar.DAY_OF_MONTH)
        val solar=MutableList(42){"  "}
        val lunar=MutableList(42){"  "}
        for(day in 1..max){
            val index=first+day-1
            val lunarDate=LunarCalendar.fromSolar(day,c.get(Calendar.MONTH)+1,c.get(Calendar.YEAR))
            solar[index]=String.format(locale,"%2d",day)
            lunar[index]=String.format(locale,"%2d",lunarDate.first)
        }
        val out=StringBuilder("T2 T3 T4 T5 T6 T7 CN\n")
        for(row in 0 until 6){
            for(col in 0 until 7) out.append(solar[row*7+col]).append(" ")
            out.append("\n")
            for(col in 0 until 7) out.append(lunar[row*7+col]).append(" ")
            if(row<5) out.append("\n")
        }
        return out.toString()
    }
}
