package vn.dqt.lunarweather

import android.graphics.*
import java.util.Calendar
import java.util.Locale

object ClockFace {
    fun render(size:Int, roman:Boolean, color:Int):Bitmap {
        val bmp=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888)
        val c=Canvas(bmp); val cx=size/2f; val cy=size/2f; val r=size*0.46f
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader=LinearGradient(0f,0f,size.toFloat(),size.toFloat(),
            intArrayOf(Color.rgb(255,248,224),Color.rgb(244,226,185),Color.rgb(255,251,235)),
            null,Shader.TileMode.CLAMP)
        c.drawCircle(cx,cy,r,p)
        p.shader=null; p.style=Paint.Style.STROKE; p.strokeWidth=size*0.018f
        p.color=color; c.drawCircle(cx,cy,r,p)
        p.style=Paint.Style.FILL; p.color=Color.rgb(30,25,20)
        p.textAlign=Paint.Align.CENTER; p.typeface=Typeface.create("serif",Typeface.BOLD)
        p.textSize=size*0.105f
        val nums=if(roman) arrayOf("XII","I","II","III","IV","V","VI","VII","VIII","IX","X","XI")
                  else arrayOf("12","1","2","3","4","5","6","7","8","9","10","11")
        for(i in 0..59){
            val a=Math.PI*i/30.0-Math.PI/2
            val outer=r*0.91f; val inner=if(i%5==0) r*0.80f else r*0.86f
            p.color=if(i%5==0) Color.DKGRAY else Color.GRAY
            p.strokeWidth=if(i%5==0) size*0.009f else size*0.003f
            c.drawLine(cx+(inner*kotlin.math.cos(a)).toFloat(),cy+(inner*kotlin.math.sin(a)).toFloat(),
                cx+(outer*kotlin.math.cos(a)).toFloat(),cy+(outer*kotlin.math.sin(a)).toFloat(),p)
            if(i%5==0){
                p.color=Color.rgb(25,20,18); p.textSize=size*0.105f
                val n=i/5; val rr=r*0.68f
                c.drawText(nums[n],cx+(rr*kotlin.math.cos(a)).toFloat(),
                    cy+(rr*kotlin.math.sin(a)).toFloat()+p.textSize*0.34f,p)
            }
        }
        val now=Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val sec=now.get(Calendar.SECOND); val min=now.get(Calendar.MINUTE); val hour=now.get(Calendar.HOUR)
        fun hand(angle:Double,length:Float,width:Float,color:Int){
            p.color=color;p.strokeWidth=width;p.strokeCap=Paint.Cap.ROUND
            val a=angle-Math.PI/2
            c.drawLine(cx,cy,cx+(length*kotlin.math.cos(a)).toFloat(),cy+(length*kotlin.math.sin(a)).toFloat(),p)
        }
        hand((hour+min/60.0)*Math.PI/6,r*0.48f,size*0.025f,Color.BLACK)
        hand((min+sec/60.0)*Math.PI/30,r*0.70f,size*0.014f,Color.BLACK)
        hand(sec*Math.PI/30,r*0.78f,size*0.006f,Color.RED)
        p.color=color;c.drawCircle(cx,cy,size*0.035f,p)
        p.color=Color.DKGRAY;p.textSize=size*0.045f;p.textAlign=Paint.Align.CENTER
        c.drawText("DQT",cx,cy+r*0.22f,p)
        return bmp
    }
}
