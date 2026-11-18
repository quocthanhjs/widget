package vn.dqt.lunarweather

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.ViewGroup
import android.widget.*
import android.content.Intent

class SettingsActivity : Activity() {
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var locationStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("dqt_settings", MODE_PRIVATE)
        if (intent.getBooleanExtra("open_weather", false)) {
            openWeather()
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 24)
            setBackgroundColor(Color.rgb(8, 22, 48))
        }
        fun label(s: String) = TextView(this).apply {
            text = s; textSize = 15f; setTextColor(Color.WHITE); setPadding(0, 12, 0, 5)
        }
        fun edit(hintText: String, value: String = "") = EditText(this).apply {
            hint = hintText; setText(value); setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY)
        }
        fun spinner(items: Array<String>, selected: Int) = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, items)
            setSelection(selected)
        }

        root.addView(label("DQT LUNAR WEATHER – CÀI ĐẶT WIDGET"))
        root.addView(label("Chọn giao diện hiển thị"))
        val design = spinner(
            arrayOf("Kiểu 1 – Đa năng (đồng hồ số)", "Kiểu 2 – Đồng hồ tròn truyền thống", "Kiểu 3 – Tối giản nghệ thuật"),
            prefs.getInt("design", 0)
        )
        root.addView(design)

        root.addView(label("Kiểu số trên đồng hồ tròn"))
        val roman = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        val normal = RadioButton(this).apply { text = "Số thường (1, 2, 3...)"; id = 1; setTextColor(Color.WHITE) }
        val romanBtn = RadioButton(this).apply { text = "Số La Mã (I, II, III...)"; id = 2; setTextColor(Color.WHITE) }
        roman.addView(normal); roman.addView(romanBtn)
        roman.check(if (prefs.getBoolean("roman", false)) 2 else 1)
        root.addView(roman)

        root.addView(label("Thời tiết và địa điểm theo điện thoại"))
        root.addView(TextView(this).apply {
            text = "Ứng dụng tự lấy vị trí GPS/mạng của điện thoại, sau đó lấy thời tiết tại vị trí đó. Không cần AccuWeather API Key."
            textSize = 13f; setTextColor(Color.LTGRAY)
        })
        locationStatus = label("Vị trí hiện tại: " + (prefs.getString("location_label", "Chưa cập nhật") ?: "Chưa cập nhật"))
        root.addView(locationStatus)
        root.addView(Button(this).apply {
            text = "CẬP NHẬT VỊ TRÍ TỪ ĐIỆN THOẠI"
            setOnClickListener {
                locationStatus.text = "Đang lấy vị trí điện thoại…"
                DeviceLocation.update(this@SettingsActivity) { result ->
                    locationStatus.text = "Vị trí hiện tại: $result"
                }
            }
        })

        root.addView(label("Chu kỳ cập nhật thời tiết"))
        val intervals = arrayOf("15 phút", "30 phút", "1 giờ", "3 giờ")
        val mins = intArrayOf(15, 30, 60, 180)
        val interval = spinner(intervals, mins.indexOf(prefs.getInt("refresh_minutes", 30)).coerceAtLeast(0))
        root.addView(interval)

        root.addView(label("Lời hay – Ý đẹp (giao diện đồng hồ tròn)"))
        val phrase = edit("Ví dụ: Vạn Sự Bình An", prefs.getString("phrase", "Vạn Sự Bình An") ?: "Vạn Sự Bình An")
        root.addView(phrase)

        root.addView(label("Màu nền"))
        val colors = arrayOf("Xanh neon", "Tím neon", "Đỏ rượu", "Đen kính", "Xanh ngọc")
        val colorValues = intArrayOf(0xFF142D67.toInt(), 0xFF39205F.toInt(), 0xFF641D31.toInt(), 0xFF101820.toInt(), 0xFF064D50.toInt())
        val colorSpinner = spinner(colors, colorValues.indexOf(prefs.getInt("background_color", colorValues[0])).coerceAtLeast(0))
        root.addView(colorSpinner)

        root.addView(label("Độ đậm nền / độ trong suốt: 0–100%"))
        val opacity = SeekBar(this).apply { max = 100; progress = prefs.getInt("background_alpha", 88) }
        root.addView(opacity)
        val opacityValue = TextView(this).apply { text = "${opacity.progress}%"; setTextColor(Color.WHITE) }
        opacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, value: Int, user: Boolean) { opacityValue.text = "$value%" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        root.addView(opacityValue)

        root.addView(Button(this).apply {
            text = "ÁP DỤNG CÀI ĐẶT"
            setOnClickListener {
                prefs.edit()
                    .putInt("design", design.selectedItemPosition)
                    .putBoolean("roman", roman.checkedRadioButtonId == 2)
                    .putInt("refresh_minutes", mins[interval.selectedItemPosition])
                    .putString("phrase", phrase.text.toString())
                    .putInt("background_color", colorValues[colorSpinner.selectedItemPosition])
                    .putInt("background_alpha", opacity.progress)
                    .apply()
                WidgetUpdater.refreshAll(this@SettingsActivity)
                RefreshScheduler.schedule(this@SettingsActivity)
                WeatherClient.refresh(this@SettingsActivity)
                Toast.makeText(this@SettingsActivity, "Đã áp dụng cài đặt.", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(Button(this).apply {
            text = "MỞ ỨNG DỤNG THỜI TIẾT CỦA MÁY"
            setOnClickListener { openWeather() }
        })
        root.addView(label("Lưu ý: Android không cho mọi ứng dụng đọc trực tiếp dữ liệu riêng của ứng dụng Thời tiết hãng máy. Widget dùng vị trí điện thoại và nguồn thời tiết trực tuyến để cập nhật nhiệt độ/dự báo."))
        root.addView(label("Cách thêm: nhấn giữ màn hình chính → Widgets → DQT Digital Widget, DQT Analog Widget hoặc DQT Minimal Widget."))

        setContentView(ScrollView(this).apply { addView(root) }, ViewGroup.LayoutParams(-1, -1))

        // Ask once for permission when the user opens settings for the first time.
        if (!DeviceLocation.hasPermission(this)) {
            DeviceLocation.requestPermission(this, 4102)
        } else {
            DeviceLocation.update(this) { result -> locationStatus.text = "Vị trí hiện tại: $result" }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 4102) {
            if (DeviceLocation.hasPermission(this)) {
                DeviceLocation.update(this) { result -> locationStatus.text = "Vị trí hiện tại: $result" }
            } else {
                locationStatus.text = "Chưa được cấp quyền vị trí. Có thể bật trong Cài đặt Android."
            }
        }
    }

    private fun openWeather() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_WEATHER")
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Máy không công khai ứng dụng Thời tiết qua Android. Hãy mở ứng dụng từ màn hình chính.", Toast.LENGTH_LONG).show()
        }
    }
}
