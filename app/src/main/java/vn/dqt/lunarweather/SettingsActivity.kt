package vn.dqt.lunarweather

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : Activity() {
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var preview: LinearLayout
    private lateinit var previewTitle: TextView
    private lateinit var locationStatus: TextView
    private lateinit var design: Spinner
    private lateinit var colorSpinner: Spinner
    private lateinit var interval: Spinner
    private lateinit var opacity: SeekBar
    private lateinit var opacityValue: TextView
    private lateinit var phrase: EditText
    private lateinit var roman: RadioGroup
    private val colors = intArrayOf(0xFF142D67.toInt(),0xFF39205F.toInt(),0xFF641D31.toInt(),0xFF101820.toInt(),0xFF064D50.toInt())
    private val colorNames = arrayOf("Xanh đêm", "Tím thủy tinh", "Đỏ rượu", "Đen kính", "Xanh ngọc")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("dqt_settings", MODE_PRIVATE)
        if (intent.getBooleanExtra("open_weather", false)) { openWeather(); finish(); return }
        val scroll = ScrollView(this).apply { setBackgroundColor(Color.rgb(5,12,27)) }
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,22,18,28) }
        scroll.addView(root)
        fun text(s:String,size:Float=14f,color:Int=Color.WHITE,bold:Boolean=false)=TextView(this).apply { text=s;textSize=size;setTextColor(color);if(bold)setTypeface(null,Typeface.BOLD);setPadding(0,8,0,6) }
        fun card():LinearLayout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(15,13,15,13);background=GradientDrawable().apply{cornerRadius=22f;setColor(0xFF142238.toInt());setStroke(1,0x446EA8D8)};layoutParams=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=13} }
        fun styledSpinner(items:Array<String>, selected:Int):Spinner {
            val sp=Spinner(this)
            val adapter=object: ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items){ override fun getView(position:Int,convertView:View?,parent:ViewGroup):View { return (super.getView(position,convertView,parent) as TextView).apply{setTextColor(Color.WHITE);textSize=14f;setPadding(12,10,12,10)} }; override fun getDropDownView(position:Int,convertView:View?,parent:ViewGroup):View{return (super.getDropDownView(position,convertView,parent) as TextView).apply{setTextColor(Color.WHITE);setBackgroundColor(0xFF142238.toInt());setPadding(14,13,14,13)} } }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);sp.adapter=adapter;sp.setSelection(selected.coerceIn(items.indices));return sp
        }
        root.addView(text("DQT LUNAR WEATHER",23f,0xFFFFD77A.toInt(),true))
        root.addView(text("CÀI ĐẶT WIDGET  ·  LIQUID GLASS",11f,0xFF9FB6D2.toInt(),true))

        val pcard=card();pcard.addView(text("XEM TRƯỚC",12f,0xFFFFD77A.toInt(),true));previewTitle=text("Giao diện 1 · Đa năng",16f,Color.WHITE,true);pcard.addView(previewTitle)
        preview=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(14,16,14,16)}
        pcard.addView(preview,LinearLayout.LayoutParams(-1,190));pcard.addView(text("Bản xem trước thay đổi theo giao diện, màu nền và độ trong suốt bạn chọn.",11f,0xFFB7C8DA.toInt()));root.addView(pcard)

        val designCard=card();designCard.addView(text("1. CHỌN GIAO DIỆN",12f,0xFFFFD77A.toInt(),true))
        design=styledSpinner(arrayOf("01 · Đa năng – đồng hồ số + lịch tháng","02 · Đồng hồ tròn – phong cách cổ điển","03 · Tối giản – nghệ thuật"),prefs.getInt("design",0));designCard.addView(design);root.addView(designCard)

        val styleCard=card();styleCard.addView(text("2. TÙY CHỈNH ĐỒNG HỒ",12f,0xFFFFD77A.toInt(),true))
        roman=RadioGroup(this).apply{orientation=RadioGroup.VERTICAL}
        val r1=RadioButton(this).apply{text="Số thường (1, 2, 3...)";id=1;setTextColor(Color.WHITE)}
        val r2=RadioButton(this).apply{text="Số La Mã (I, II, III...)";id=2;setTextColor(Color.WHITE)}
        roman.addView(r1);roman.addView(r2);roman.check(if(prefs.getBoolean("roman",false))2 else 1);styleCard.addView(roman)
        styleCard.addView(text("Lời hay – ý đẹp (giao diện 2 và 3)"));phrase=EditText(this).apply{setText(prefs.getString("phrase","Vạn Sự Bình An"));setTextColor(Color.WHITE);setHintTextColor(0xFF8FA4BC.toInt());hint="Nhập câu bạn yêu thích";setSingleLine(true)};styleCard.addView(phrase);root.addView(styleCard)

        val weatherCard=card();weatherCard.addView(text("3. THỜI TIẾT HIỆN TẠI",12f,0xFFFFD77A.toInt(),true));weatherCard.addView(text("Chỉ hiển thị nhiệt độ, tình trạng thời tiết và vị trí điện thoại. Chạm vào thời tiết trên widget để mở ứng dụng Thời tiết của máy.",12f,0xFFB7C8DA.toInt()))
        locationStatus=text("⌖ Vị trí: "+(prefs.getString("location_label","Chưa cập nhật")?:"Chưa cập nhật"),13f);weatherCard.addView(locationStatus)
        weatherCard.addView(Button(this).apply{text="CẬP NHẬT VỊ TRÍ ĐIỆN THOẠI";setOnClickListener{locationStatus.text="Đang lấy vị trí…";DeviceLocation.update(this@SettingsActivity){locationStatus.text="⌖ Vị trí: $it"}}})
        weatherCard.addView(text("Nguồn thời tiết: Open-Meteo · Tự động theo GPS/mạng · Không đọc dữ liệu riêng của ứng dụng thời tiết hãng máy.",11f,0xFF9FB6D2.toInt()))
        weatherCard.addView(text("Chu kỳ cập nhật"));interval=styledSpinner(arrayOf("15 phút","30 phút","1 giờ","3 giờ"),intArrayOf(15,30,60,180).indexOf(prefs.getInt("refresh_minutes",30)).coerceAtLeast(0));weatherCard.addView(interval);root.addView(weatherCard)

        val displayCard=card();displayCard.addView(text("4. MÀU SẮC & ĐỘ TRONG SUỐT",12f,0xFFFFD77A.toInt(),true));colorSpinner=styledSpinner(colorNames,colors.indexOf(prefs.getInt("background_color",colors[0])).coerceAtLeast(0));displayCard.addView(text("Màu nền"));displayCard.addView(colorSpinner)
        displayCard.addView(text("Độ đậm nền: 0–100%"));opacity=SeekBar(this).apply{max=100;progress=prefs.getInt("background_alpha",88)};displayCard.addView(opacity);opacityValue=text("${opacity.progress}%",12f,Color.WHITE);displayCard.addView(opacityValue);root.addView(displayCard)

        val updatePreview={
            val idx=design.selectedItemPosition;val color=colors[colorSpinner.selectedItemPosition];val a=(opacity.progress*255/100 shl 24) or (color and 0x00FFFFFF)
            preview.background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(a,0xFF07111F.toInt())).apply{cornerRadius=20f;setStroke(1,0x667FC8FF)}
            preview.removeAllViews();previewTitle.text=arrayOf("Giao diện 1 · Đa năng","Giao diện 2 · Đồng hồ tròn","Giao diện 3 · Tối giản nghệ thuật")[idx]
            when(idx){
                0->{preview.addView(text("09:41",42f,Color.WHITE,true));preview.addView(text("Thứ Tư · 19 tháng 11, 2026",12f,Color.WHITE));preview.addView(text("Ngày 10 tháng 10 âm lịch",12f,0xFFFFD66B.toInt(),true));preview.addView(text("☀  28°C   Trời quang   ·   Bạc Liêu",13f,Color.WHITE));preview.addView(text("T2   T3   T4   T5   T6   T7   CN\n17   18   19   20   21   22   23",11f,0xFFDCEBFA.toInt()))}
                1->{preview.addView(text("◷",54f,0xFFFFD66B.toInt()));preview.addView(text(phrase.text.toString().ifBlank{"Vạn Sự Bình An"},20f,0xFFFFD66B.toInt(),true));preview.addView(text("Tháng 11 · Âm lịch Việt Nam",12f,Color.WHITE));preview.addView(text("☀  28°C · Trời quang",14f,Color.WHITE));preview.addView(text("⌖ Bạc Liêu",12f,Color.WHITE))}
                else->{preview.addView(text("19",62f,Color.WHITE,true));preview.addView(text("THÁNG MƯỜI MỘT  ·  2026",11f,0xFFFFD66B.toInt(),true));preview.addView(text("10/10 ÂM LỊCH",12f,Color.WHITE));preview.addView(text("28°  ☀  Trời quang · Bạc Liêu",13f,Color.WHITE));preview.addView(text(phrase.text.toString().ifBlank{"Vạn Sự Bình An"},15f,0xFFFFD66B.toInt()))}
            }
        }
        design.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,position:Int,id:Long)=updatePreview()}
        colorSpinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,position:Int,id:Long)=updatePreview()}
        opacity.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,v:Int,u:Boolean){opacityValue.text="$v%";updatePreview()};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?) {}})
        phrase.addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){updatePreview()};override fun afterTextChanged(s:android.text.Editable?) {}})
        updatePreview()

        val apply=Button(this).apply{text="LƯU & ÁP DỤNG WIDGET";setOnClickListener{prefs.edit().putInt("design",design.selectedItemPosition).putBoolean("roman",roman.checkedRadioButtonId==2).putString("phrase",phrase.text.toString()).putInt("refresh_minutes",intArrayOf(15,30,60,180)[interval.selectedItemPosition]).putInt("background_color",colors[colorSpinner.selectedItemPosition]).putInt("background_alpha",opacity.progress).apply();WidgetUpdater.refreshAll(this@SettingsActivity);RefreshScheduler.schedule(this@SettingsActivity);WeatherClient.refresh(this@SettingsActivity);Toast.makeText(this@SettingsActivity,"Đã áp dụng cài đặt widget",Toast.LENGTH_SHORT).show()}}
        root.addView(apply,LinearLayout.LayoutParams(-1,-2).apply{topMargin=5;bottomMargin=12})
        root.addView(Button(this).apply{text="MỞ ỨNG DỤNG THỜI TIẾT";setOnClickListener{openWeather()}})
        root.addView(text("Thêm widget: nhấn giữ màn hình chính → Widgets → chọn DQT Lunar Weather. Có thể đặt nhiều widget trên màn hình.",11f,0xFF9FB6D2.toInt()))
        setContentView(scroll,ViewGroup.LayoutParams(-1,-1))
        if(!DeviceLocation.hasPermission(this))DeviceLocation.requestPermission(this,4102) else DeviceLocation.update(this){locationStatus.text="⌖ Vị trí: $it"}
    }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==4102){if(DeviceLocation.hasPermission(this))DeviceLocation.update(this){locationStatus.text="⌖ Vị trí: $it"}else locationStatus.text="Chưa cấp quyền vị trí. Có thể bật trong Cài đặt Android."}}
    private fun openWeather(){val i=Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_WEATHER");try{startActivity(i)}catch(_:Exception){Toast.makeText(this,"Máy không công khai ứng dụng Thời tiết qua Android.",Toast.LENGTH_LONG).show()}}
}
