package vn.dqt.lunarweather

import java.util.Calendar
import kotlin.math.floor

/** Vietnamese lunar calendar calculation, UTC+7. Validate against an official Vietnamese calendar before release. */
object LunarCalendar {
    private const val PI = Math.PI
    private fun jd(d:Int,m:Int,y:Int):Int {
        val a=(14-m)/12; val yy=y+4800-a; val mm=m+12*a-3
        var x=d+(153*mm+2)/5+365*yy+yy/4-yy/100+yy/400-32045
        if(x<2299161) x=d+(153*mm+2)/5+365*yy+yy/4-32083
        return x
    }
    private fun newMoon(k:Int):Double {
        val t=k/1236.85; val t2=t*t; val t3=t2*t; val dr=PI/180
        var x=2415020.75933+29.53058868*k+0.0001178*t2-0.000000155*t3
        x+=0.00033*kotlin.math.sin((166.56+132.87*t-0.009173*t2)*dr)
        val m=359.2242+29.10535608*k-0.0000333*t2-0.00000347*t3
        val mp=306.0253+385.81691806*k+0.0107306*t2+0.00001236*t3
        val f=21.2964+390.67050646*k-0.0016528*t2-0.00000239*t3
        var c=(0.1734-0.000393*t)*kotlin.math.sin(m*dr)+0.0021*kotlin.math.sin(2*m*dr)
        c-=0.4068*kotlin.math.sin(mp*dr)+0.0161*kotlin.math.sin(2*mp*dr)+0.0004*kotlin.math.sin(3*mp*dr)
        c+=0.0104*kotlin.math.sin(2*f*dr)-0.0051*kotlin.math.sin((m+mp)*dr)-0.0074*kotlin.math.sin((m-mp)*dr)
        c+=0.0004*kotlin.math.sin((2*f+m)*dr)-0.0004*kotlin.math.sin((2*f-m)*dr)-0.0006*kotlin.math.sin((2*f+mp)*dr)
        c+=0.0010*kotlin.math.sin((2*f-mp)*dr)+0.0005*kotlin.math.sin((2*mp+m)*dr)
        val delta=if(t < -11) 0.001+0.000839*t+0.0002261*t2-0.00000845*t3-0.000000081*t*t3 else -0.000278+0.000265*t+0.000262*t2
        return x+c-delta
    }
    private fun newMoonDay(k:Int)=floor(newMoon(k)+0.5+7.0/24.0).toInt()
    private fun sunLongitude(j:Double):Int {
        val t=(j-2451545.0)/36525; val t2=t*t; val dr=PI/180
        val m=357.52910+35999.05030*t-0.0001559*t2-0.00000048*t*t2
        val l0=280.46645+36000.76983*t+0.0003032*t2
        var dl=(1.914600-0.004817*t-0.000014*t2)*kotlin.math.sin(dr*m)
        dl+=(0.019993-0.000101*t)*kotlin.math.sin(2*dr*m)+0.000290*kotlin.math.sin(3*dr*m)
        var l=(l0+dl)*dr; l-=PI*2*floor(l/(PI*2))
        return floor(l/PI*6).toInt()
    }
    private fun sunLongDay(day:Int)=sunLongitude(day-0.5-7.0/24.0)
    private fun month11(y:Int):Int {
        val off=jd(31,12,y)-2415021; val k=floor(off/29.530588853).toInt()
        var nm=newMoonDay(k); if(sunLongDay(nm)>=9) nm=newMoonDay(k-1); return nm
    }
    private fun leapOffset(a11:Int):Int {
        val k=floor(0.5+(a11-2415021.076998695)/29.530588853).toInt()
        var last=0; var i=1; var arc=sunLongDay(newMoonDay(k+i))
        do { last=arc; i++; arc=sunLongDay(newMoonDay(k+i)) } while(arc!=last && i<14)
        return i-1
    }
    fun fromSolar(day:Int,month:Int,year:Int):Triple<Int,Int,Int> {
        val dayNumber=jd(day,month,year)
        val k=floor((dayNumber-2415021.076998695)/29.530588853).toInt()
        var monthStart=newMoonDay(k+1); if(monthStart>dayNumber) monthStart=newMoonDay(k)
        var a11=month11(year); var b11=a11; val lunarYear:Int
        if(a11>=monthStart){lunarYear=year;a11=month11(year-1)} else {lunarYear=year+1;b11=month11(year+1)}
        val lunarDay=dayNumber-monthStart+1; val diff=floor((monthStart-a11)/29.0).toInt()
        var lunarMonth=diff+11
        if(b11-a11>365){val leap=leapOffset(a11); if(diff>=leap) lunarMonth=diff+10}
        if(lunarMonth>12) lunarMonth-=12
        var ly=lunarYear; if(lunarMonth>=11 && diff<4) ly--
        return Triple(lunarDay,lunarMonth,ly)
    }
}
