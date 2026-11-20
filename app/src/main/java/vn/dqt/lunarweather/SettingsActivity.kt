package vn.dqt.lunarweather

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.TextWatcher
import android.text.Editable

class SettingsActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("dqt_settings", MODE_PRIVATE) }
    private val palette = intArrayOf(
        0xFF142D67.toInt(), 0xFF39205F.toInt(), 0xFF641D31.toInt(),
        0xFF101820.toInt(), 0xFF064D50.toInt()
    )
    private val paletteNames = arrayOf("Xanh đêm", "Tím cực quang", "Đỏ rượu", "Đen kính", "Xanh ngọc")
    private var selectedDesign = 0
    private var selectedColor = 0
    private var opacityValue = 88
    private var romanNumbers = false
    private var refreshMinutes = 30
    private lateinit var previewCard: LinearLayout
    private lateinit var previewHeading: TextView
    private lateinit var opacityLabel: TextView
    private lateinit var locationLabel: TextView
    private lateinit var phraseInput: EditText
    private lateinit var romanGroup: RadioGroup
    private lateinit var intervalSpinner: Spinner
    private val styleButtons = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("open_weather", false)) {
            openWeather()
            finish()
            return
        }
        selectedDesign = prefs.getInt("design", 0).coerceIn(0, 2)
        selectedColor = palette.indexOf(prefs.getInt("background_color", palette[0])).let { if (it < 0) 0 else it }
        opacityValue = prefs.getInt("background_alpha", 88).coerceIn(0, 100)
        romanNumbers = prefs.getBoolean("roman", false)
        refreshMinutes = prefs.getInt("refresh_minutes", 30)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(4, 11, 25))
            isFillViewport = true
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(30))
        }
        scroll.addView(page)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = column()
        brand.addView(label("DQT LUNAR WEATHER", 21f, 0xFFFFD77A.toInt(), true))
        brand.addView(label("WIDGET STUDIO  ·  LIQUID GLASS", 10f, 0xFF9DB6D2.toInt(), true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(pill("● ĐANG TÙY CHỈNH", 0xFF173E54.toInt(), 0xFF7CE7F2.toInt()))
        page.addView(header)
        page.addView(space(14))

        val previewPanel = card()
        val previewTop = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        previewHeading = label("XEM TRƯỚC WIDGET", 13f, 0xFFFFD77A.toInt(), true)
        previewTop.addView(previewHeading, LinearLayout.LayoutParams(0, -2, 1f))
        previewTop.addView(label("CẬP NHẬT TRỰC TIẾP", 9f, 0xFF8DE7E0.toInt(), true))
        previewPanel.addView(previewTop)
        previewCard = column().apply {
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        previewPanel.addView(previewCard, LinearLayout.LayoutParams(-1, dp(238)).apply {
            topMargin = dp(12)
        })
        previewPanel.addView(label("Bản xem trước thay đổi ngay khi bạn chọn kiểu, màu nền hoặc độ trong suốt.", 11f, 0xFFB5C7DA.toInt()))
        page.addView(previewPanel)

        val stylePanel = card()
        stylePanel.addView(sectionTitle("01  ·  CHỌN GIAO DIỆN"))
        stylePanel.addView(label("Chọn một trong ba mẫu widget", 12f, 0xFFB7C8DA.toInt()))
        val styleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val names = arrayOf("ĐA NĂNG", "SỐ TỐI GIẢN", "ĐỒNG HỒ TRÒN")
        val descriptions = arrayOf("Lịch tháng + thời tiết", "Giờ lớn + ngày âm", "Mặt đồng hồ kim")
        for (i in 0..2) {
            val tile = column().apply {
                gravity = Gravity.CENTER
                setPadding(dp(5), dp(10), dp(5), dp(10))
                background = rounded(0xFF14243A.toInt(), 18f, 0x5579B9F4)
                setOnClickListener {
                    selectedDesign = i
                    refreshStyleButtons()
                    updatePreview()
                }
            }
            val mini = label(arrayOf("14:49\n▦", "14:49", "◷")[i], 22f, 0xFFFFD77A.toInt(), true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, dp(8))
            }
            tile.addView(mini)
            tile.addView(label(names[i], 10f, Color.WHITE, true).apply { gravity = Gravity.CENTER })
            tile.addView(label(descriptions[i], 9f, 0xFFADC0D5.toInt()).apply { gravity = Gravity.CENTER })
            styleButtons.add(tile)
            styleRow.addView(tile, LinearLayout.LayoutParams(0, dp(108), 1f).apply {
                if (i > 0) marginStart = dp(7)
            })
        }
        stylePanel.addView(styleRow)
        page.addView(stylePanel)

        val clockPanel = card()
        clockPanel.addView(sectionTitle("02  ·  TÙY CHỈNH ĐỒNG HỒ"))
        clockPanel.addView(label("Kiểu số trên mặt đồng hồ tròn", 12f, 0xFFB7C8DA.toInt()))
        romanGroup = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val regular = RadioButton(this).apply {
            id = 101
            text = "Số thường"
            setTextColor(Color.WHITE)
            textSize = 12f
            buttonTintList = android.content.res.ColorStateList.valueOf(0xFF65DDF2.toInt())
        }
        val roman = RadioButton(this).apply {
            id = 102
            text = "Số La Mã"
            setTextColor(Color.WHITE)
            textSize = 12f
            buttonTintList = android.content.res.ColorStateList.valueOf(0xFF65DDF2.toInt())
        }
        romanGroup.addView(regular)
        romanGroup.addView(roman)
        romanGroup.check(if (romanNumbers) 102 else 101)
        clockPanel.addView(romanGroup)
        clockPanel.addView(space(5))
        clockPanel.addView(label("Lời hay – ý đẹp (dùng cho giao diện tối giản và đồng hồ tròn)", 11f, 0xFFB7C8DA.toInt()))
        phraseInput = EditText(this).apply {
            setText(prefs.getString("phrase", "Vạn Sự Bình An"))
            hint = "Nhập câu bạn yêu thích"
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF8298B3.toInt())
            textSize = 14f
            setSingleLine(true)
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = rounded(0xFF0B1728.toInt(), 12f, 0x5579B9F4)
        }
        clockPanel.addView(phraseInput, LinearLayout.LayoutParams(-1, dp(48)))
        page.addView(clockPanel)

        val weatherPanel = card()
        weatherPanel.addView(sectionTitle("03  ·  THỜI TIẾT HIỆN TẠI"))
        weatherPanel.addView(label("Chỉ hiển thị nhiệt độ, biểu tượng, tình trạng thời tiết và vị trí. Chạm vào thời tiết trên widget để mở ứng dụng thời tiết trên điện thoại.", 12f, 0xFFB7C8DA.toInt()))
        locationLabel = label("⌖  " + (prefs.getString("location_label", "Chưa cập nhật vị trí") ?: "Chưa cập nhật vị trí"), 12f, Color.WHITE, true)
        weatherPanel.addView(locationLabel)
        val locationButton = actionButton("LẤY VỊ TRÍ ĐIỆN THOẠI") {
            locationLabel.text = "⌖  Đang lấy vị trí..."
            DeviceLocation.update(this) { locationLabel.text = "⌖  $it" }
        }
        weatherPanel.addView(locationButton, LinearLayout.LayoutParams(-1, dp(42)))
        weatherPanel.addView(label("Nguồn dữ liệu: Open-Meteo · Tự động theo vị trí GPS/mạng. Không đọc dữ liệu riêng của ứng dụng thời tiết hãng máy.", 10f, 0xFF8FA8C2.toInt()))
        weatherPanel.addView(space(5))
        weatherPanel.addView(label("Chu kỳ cập nhật", 12f, 0xFFB7C8DA.toInt()))
        intervalSpinner = spinner(arrayOf("15 phút", "30 phút", "1 giờ", "3 giờ"), intArrayOf(15, 30, 60, 180).indexOf(refreshMinutes).let { if (it < 0) 1 else it })
        weatherPanel.addView(intervalSpinner)
        page.addView(weatherPanel)

        val appearancePanel = card()
        appearancePanel.addView(sectionTitle("04  ·  MÀU NỀN & ĐỘ TRONG SUỐT"))
        appearancePanel.addView(label("Chọn tông màu Liquid Glass", 12f, 0xFFB7C8DA.toInt()))
        val colorRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (i in palette.indices) {
            val swatch = View(this).apply {
                background = rounded(palette[i], 40f, 0x99FFFFFF.toInt())
                setOnClickListener {
                    selectedColor = i
                    refreshColorSwatches(colorRow)
                    updatePreview()
                }
            }
            colorRow.addView(swatch, LinearLayout.LayoutParams(0, dp(34), 1f).apply {
                if (i > 0) marginStart = dp(8)
            })
        }
        appearancePanel.addView(colorRow)
        appearancePanel.addView(label("Độ đậm nền", 12f, 0xFFB7C8DA.toInt()))
        val opacityRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val seek = SeekBar(this).apply {
            max = 100
            progress = opacityValue
            progressTintList = android.content.res.ColorStateList.valueOf(0xFF5DE1F2.toInt())
            thumbTintList = android.content.res.ColorStateList.valueOf(0xFFFFD77A.toInt())
        }
        opacityLabel = label("$opacityValue%", 12f, Color.WHITE, true).apply { gravity = Gravity.END }
        opacityRow.addView(seek, LinearLayout.LayoutParams(0, dp(42), 1f))
        opacityRow.addView(opacityLabel, LinearLayout.LayoutParams(dp(46), -2))
        appearancePanel.addView(opacityRow)
        page.addView(appearancePanel)

        val apply = actionButton("✓   LƯU & ÁP DỤNG WIDGET") {
            prefs.edit()
                .putInt("design", selectedDesign)
                .putBoolean("roman", romanGroup.checkedRadioButtonId == 102)
                .putString("phrase", phraseInput.text.toString())
                .putInt("refresh_minutes", intArrayOf(15, 30, 60, 180)[intervalSpinner.selectedItemPosition])
                .putInt("background_color", palette[selectedColor])
                .putInt("background_alpha", opacityValue)
                .putInt("accent_color", 0xFFFFD36A.toInt())
                .apply()
            WidgetUpdater.refreshAll(this)
            RefreshScheduler.schedule(this)
            WeatherClient.refresh(this)
            Toast.makeText(this, "Đã lưu và áp dụng giao diện", Toast.LENGTH_SHORT).show()
        }
        apply.background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xFF00BFEA.toInt(), 0xFF6557F5.toInt(), 0xFFD64BEA.toInt())).apply {
            cornerRadius = dp(28).toFloat()
            setStroke(dp(1), 0x99FFFFFF.toInt())
        }
        page.addView(apply, LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(4)
            bottomMargin = dp(10)
        })
        page.addView(actionButton("MỞ ỨNG DỤNG THỜI TIẾT") { openWeather() })
        page.addView(label("Thêm widget: nhấn giữ màn hình chính → Widgets → chọn DQT Lunar Weather. Sau đó chọn giao diện tại đây.", 10f, 0xFF8FA8C2.toInt()))

        romanGroup.setOnCheckedChangeListener { _, id ->
            romanNumbers = id == 102
            updatePreview()
        }
        phraseInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updatePreview() }
            override fun afterTextChanged(s: Editable?) {}
        })
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                opacityValue = progress
                opacityLabel.text = "$progress%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        intervalSpinner.setSelection(intArrayOf(15, 30, 60, 180).indexOf(refreshMinutes).let { if (it < 0) 1 else it })
        refreshStyleButtons()
        refreshColorSwatches(colorRow)
        updatePreview()
        setContentView(scroll, ViewGroup.LayoutParams(-1, -1))
        if (!DeviceLocation.hasPermission(this)) DeviceLocation.requestPermission(this, 4102)
        else DeviceLocation.update(this) { locationLabel.text = "⌖  $it" }
    }

    private fun updatePreview() {
        if (!::previewCard.isInitialized) return
        val base = palette[selectedColor]
        val alpha = (opacityValue * 255 / 100 shl 24) or (base and 0x00FFFFFF)
        previewCard.background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
            intArrayOf(alpha, 0xFF07111F.toInt())).apply {
            cornerRadius = dp(20).toFloat()
            setStroke(dp(1), 0x997BCBFF.toInt())
        }
        previewCard.removeAllViews()
        val white = Color.WHITE
        val gold = 0xFFFFD66B.toInt()
        val phrase = phraseInput.text.toString().ifBlank { "Vạn Sự Bình An" }
        previewHeading.text = arrayOf("XEM TRƯỚC · ĐA NĂNG", "XEM TRƯỚC · SỐ TỐI GIẢN", "XEM TRƯỚC · ĐỒNG HỒ TRÒN")[selectedDesign]
        when (selectedDesign) {
            0 -> {
                previewCard.addView(label("14:49", 43f, white, true).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("Thứ Sáu · 20 tháng 11, 2026", 12f, 0xFFDCE8F5.toInt()).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("Ngày 12 tháng 10 âm lịch", 13f, gold, true).apply {
                    gravity = Gravity.CENTER
                    background = rounded(0x443D5CA8, 18f, 0x886B82FF.toInt())
                    setPadding(dp(14), dp(5), dp(14), dp(5))
                })
                previewCard.addView(label("☀   28°C   Trời quang   ·   Bạc Liêu", 13f, white, true).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("THÁNG 11, 2026     T2   T3   T4   T5   T6   T7   CN\n                         16   17   18   19   20   21   22", 9f, 0xFFDCEBFA.toInt()).apply {
                    gravity = Gravity.CENTER
                    typeface = Typeface.MONOSPACE
                })
            }
            1 -> {
                previewCard.addView(label("14:49", 62f, white, true).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("THỨ SÁU  ·  20 THÁNG 11, 2026", 11f, 0xFFE4EDF8.toInt(), true).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("Ngày 12 tháng 10 âm lịch", 13f, gold, true).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("☀  28°C  ·  Trời quang", 14f, white).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("⌖ Bạc Liêu", 12f, 0xFFCAD8E8.toInt()).apply { gravity = Gravity.CENTER })
            }
            else -> {
                val clock = label(if (romanNumbers) "XII\n◷\nVI" else "12\n◷\n6", 35f, gold, true).apply {
                    gravity = Gravity.CENTER
                    typeface = Typeface.SERIF
                }
                previewCard.addView(clock)
                previewCard.addView(label(phrase, 20f, gold, true).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("Tháng 11 năm 2026 · Dương lịch", 12f, white).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("Tháng 10 năm Bính Ngọ · Âm lịch", 12f, 0xFFFFE9A6.toInt()).apply { gravity = Gravity.CENTER })
                previewCard.addView(label("☀  28°C  ·  Trời quang  ·  Bạc Liêu", 12f, white).apply { gravity = Gravity.CENTER })
            }
        }
    }

    private fun refreshStyleButtons() {
        styleButtons.forEachIndexed { index, view ->
            view.background = rounded(
                if (index == selectedDesign) 0xFF263E67.toInt() else 0xFF14243A.toInt(),
                18f,
                if (index == selectedDesign) 0xFFB96BFF.toInt() else 0x5579B9F4
            )
        }
    }

    private fun refreshColorSwatches(row: LinearLayout) {
        for (i in 0 until row.childCount) {
            row.getChildAt(i).background = rounded(
                palette[i], 40f,
                if (i == selectedColor) Color.WHITE else 0x668AA6C8
            )
        }
    }

    private fun spinner(items: Array<String>, selected: Int): Spinner {
        val sp = Spinner(this)
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return (super.getView(position, convertView, parent) as TextView).apply {
                    setTextColor(Color.WHITE); textSize = 13f; setPadding(dp(12), dp(8), dp(12), dp(8))
                }
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                    setTextColor(Color.WHITE); setBackgroundColor(0xFF14243A.toInt()); setPadding(dp(14), dp(12), dp(14), dp(12))
                }
            }
        }
        sp.adapter = adapter
        sp.setSelection(selected)
        sp.background = rounded(0xFF0B1728.toInt(), 12f, 0x5579B9F4)
        return sp
    }

    private fun actionButton(title: String, action: () -> Unit): Button =
        Button(this).apply {
            text = title
            textSize = 12f
            setTextColor(Color.WHITE)
            isAllCaps = false
            setOnClickListener { action() }
            background = rounded(0xFF1B3D65.toInt(), 24f, 0x887BCBFF.toInt())
        }

    private fun sectionTitle(value: String) = label(value, 12f, 0xFFFFD77A.toInt(), true).apply {
        setPadding(0, 0, 0, dp(9))
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(5))
        }

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun space(height: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(height)) }
    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(15), dp(14), dp(15), dp(14))
        background = rounded(0xFF111F33.toInt(), 22f, 0x5579B9F4)
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(13) }
    }
    private fun pill(text: String, bg: Int, fg: Int) = TextView(this).apply {
        this.text = text
        textSize = 9f
        setTextColor(fg)
        setPadding(dp(10), dp(7), dp(10), dp(7))
        background = rounded(bg, 18f, 0x6679B9F4)
    }
    private fun rounded(color: Int, radius: Float, stroke: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
            setStroke(dp(1), stroke)
        }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 4102) {
            if (DeviceLocation.hasPermission(this)) DeviceLocation.update(this) {
                if (::locationLabel.isInitialized) locationLabel.text = "⌖  $it"
            } else if (::locationLabel.isInitialized) {
                locationLabel.text = "Chưa cấp quyền vị trí. Có thể bật trong Cài đặt Android."
            }
        }
    }

    private fun openWeather() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_WEATHER")
        try { startActivity(intent) }
        catch (_: Exception) {
            Toast.makeText(this, "Máy không công khai ứng dụng Thời tiết qua Android.", Toast.LENGTH_LONG).show()
        }
    }
}
