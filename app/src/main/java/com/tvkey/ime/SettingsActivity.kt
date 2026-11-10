package com.tvkey.ime

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.*

class SettingsActivity : Activity() {
    private lateinit var imeStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = dp(28)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(0xFF111318.toInt())
        }
        fun text(value: String, size: Float, color: Int = 0xFFFFFFFF.toInt()) = TextView(this).apply {
            this.text = value; textSize = size; setTextColor(color); setPadding(0, dp(6), 0, dp(6))
        }
        fun button(value: String, click: () -> Unit) = Button(this).apply {
            text = value; isAllCaps = false; textSize = 18f; setOnClickListener { click() }
        }
        page.addView(text(getString(R.string.setup_title), 28f))
        page.addView(text(getString(R.string.instructions), 17f, 0xFFCAD2DF.toInt()))
        imeStatus = text("", 16f, 0xFFFFCA28.toInt())
        page.addView(imeStatus)
        page.addView(button(getString(R.string.enable)) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56)))
        page.addView(button(getString(R.string.select)) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56)))
        page.addView(EditText(this).apply {
            hint = getString(R.string.test_hint); textSize = 19f; setSingleLine(false); minLines = 2
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(0xFFBAC3D2.toInt())
        })
        page.addView(text(getString(R.string.help_title), 20f))
        page.addView(text(getString(R.string.help), 15f, 0xFFCAD2DF.toInt()))
        setContentView(ScrollView(this).apply { addView(page); foregroundGravity = Gravity.TOP })
    }

    override fun onResume() {
        super.onResume()
        if (!::imeStatus.isInitialized) return
        val component = ComponentName(this, TvKeyImeService::class.java)
        val inputMethods = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = inputMethods.enabledInputMethodList.any { it.packageName == packageName }
        val selected = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) in
            setOf(component.flattenToString(), component.flattenToShortString())
        imeStatus.text = "Trạng thái: ${if (enabled) "đã bật" else "chưa bật"} · ${if (selected) "đang chọn TVKey" else "chưa chọn TVKey"}"
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
