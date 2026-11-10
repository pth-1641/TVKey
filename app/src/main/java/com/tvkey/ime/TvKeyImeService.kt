package com.tvkey.ime

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

/** A real Android IME; it writes only through the current InputConnection. */
class TvKeyImeService : InputMethodService() {
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private val keyViews = mutableListOf<Button>()
    private var focusIndex = 0
    private var mode = KeyboardMode.LETTERS
    private var shift = false
    private var capsLock = false
    private var actionLabel = "↵"
    private var webContext = false
    private val handler = Handler(Looper.getMainLooper())
    private var deleteRepeater: Runnable? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        // Depending on the ROM, hardware keys reach either this service (window not focusable)
        // or the keyboard window's own view tree. Intercept at the root so both paths share one
        // handler and the framework's default spatial focus search never moves focus on its own.
        root = object : LinearLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                val handled = when (event.action) {
                    KeyEvent.ACTION_DOWN -> onKeyDown(event.keyCode, event)
                    KeyEvent.ACTION_UP -> onKeyUp(event.keyCode, event)
                    else -> false
                }
                return handled || super.dispatchKeyEvent(event)
            }
        }.apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.keyboard_background))
            setPadding(dp(14), dp(10), dp(14), dp(12))
            isFocusable = true
            isFocusableInTouchMode = true
            clipChildren = false
            clipToPadding = false
        }
        val screenHeight = resources.displayMetrics.heightPixels
        val desiredHeight = (screenHeight * 0.58f).roundToInt()
        val maxHeight = (screenHeight - dp(72)).coerceAtLeast(dp(240))
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            desiredHeight.coerceIn(dp(300), minOf(dp(460), maxHeight)),
        )
        rebuild(resetFocus = true)
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val inputType = info?.inputType ?: InputType.TYPE_CLASS_TEXT
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        webContext = variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == InputType.TYPE_TEXT_VARIATION_URI
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        mode = if (inputClass == InputType.TYPE_CLASS_NUMBER || inputClass == InputType.TYPE_CLASS_PHONE) {
            KeyboardMode.NUMBERS
        } else {
            KeyboardMode.LETTERS
        }
        val capFlags = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
            InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        shift = inputClass == InputType.TYPE_CLASS_TEXT && (inputType and capFlags) != 0
        capsLock = false
        actionLabel = actionText(info)
        if (::root.isInitialized) rebuild(resetFocus = true)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        stopDeleteRepeat()
        super.onFinishInputView(finishingInput)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> { moveFocus(MoveDirection.LEFT); true }
        KeyEvent.KEYCODE_DPAD_RIGHT -> { moveFocus(MoveDirection.RIGHT); true }
        KeyEvent.KEYCODE_DPAD_UP -> { moveFocus(MoveDirection.UP); true }
        KeyEvent.KEYCODE_DPAD_DOWN -> { moveFocus(MoveDirection.DOWN); true }
        KeyEvent.KEYCODE_DPAD_CENTER -> {
            if (event.repeatCount == 0) activateFocusedKey()
            true
        }
        KeyEvent.KEYCODE_ENTER -> {
            if (event.repeatCount == 0) {
                if (event.device?.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC) performEditorAction()
                else activateFocusedKey()
            }
            true
        }
        KeyEvent.KEYCODE_DEL -> {
            if (event.repeatCount == 0) { deleteOne(); startDeleteRepeat() }
            true
        }
        KeyEvent.KEYCODE_BACK -> { requestHideSelf(0); true }
        else -> {
            val codePoint = event.getUnicodeChar(event.metaState)
            if (codePoint != 0 && !event.isCtrlPressed && !event.isMetaPressed) {
                commit(String(Character.toChars(codePoint)))
                true
            } else super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            stopDeleteRepeat()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun rebuild(resetFocus: Boolean) {
        if (!::root.isInitialized) return
        val oldSpec = keyViews.getOrNull(focusIndex)?.tag as? KeySpec
        root.removeAllViews()
        keyViews.clear()
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(4), 0, dp(4), dp(6))
            clipChildren = false
        }
        status = TextView(this).apply {
            text = when (mode) {
                else -> if (capsLock) "⇪ CAPS" else if (shift) "⇧ HOA" else ""
            }
            setTextColor(Color.WHITE); textSize = dimensionSp(R.dimen.ime_status_text_size)
            layoutParams = LinearLayout.LayoutParams(0, dp(34), 1f)
        }
        header.addView(status)
        if (mode == KeyboardMode.LETTERS) {
            KeyboardModel.quickKeys(webContext).forEach { spec ->
                val chip = makeKeyButton(spec).apply { textSize = 16f }
                header.addView(chip, LinearLayout.LayoutParams(dp(120), dp(42)).apply { setMargins(dp(4), 0, dp(4), 0) })
                keyViews += chip
            }
        }
        val closeButton = makeKeyButton(KeySpec("ĐÓNG", KeyType.CLOSE)).apply {
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(dp(110), dp(42))
        }
        header.addView(closeButton)
        keyViews += closeButton
        root.addView(header)
        KeyboardModel.rows(mode, shift, actionLabel, webContext, capsLock).forEach { row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                clipChildren = false
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            }
            row.keys.forEach { spec ->
                val button = makeKeyButton(spec)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, spec.weight).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                }
                line.addView(button, params)
                keyViews += button
            }
            root.addView(line)
        }
        focusIndex = if (resetFocus) defaultFocusIndex() else {
            val restored = oldSpec?.let { previous ->
                keyViews.indexOfFirst { candidate ->
                    val current = candidate.tag as? KeySpec ?: return@indexOfFirst false
                    val isCharKey = previous.type == KeyType.TEXT
                    current.type == previous.type &&
                        (!isCharKey || current.output.equals(previous.output, ignoreCase = true) ||
                            current.label == previous.label)
                }
            } ?: -1
            if (restored >= 0) restored else defaultFocusIndex()
        }
        root.post { focusCurrent() }
    }

    private fun defaultFocusIndex(): Int {
        val wanted = when (mode) {
            KeyboardMode.NUMBERS, KeyboardMode.SYMBOLS -> "5"
            KeyboardMode.LETTERS -> "g"
        }
        return keyViews.indexOfFirst { (it.tag as? KeySpec)?.output == wanted }.takeIf { it >= 0 } ?: 0
    }

    private fun makeKeyButton(spec: KeySpec): Button = Button(this).apply {
        text = spec.label
        textSize = if (spec.label.length > 9) dimensionSp(R.dimen.ime_key_small_text_size) else dimensionSp(R.dimen.ime_key_text_size)
        isAllCaps = false
        gravity = Gravity.CENTER
        stateListAnimator = null
        tag = spec
        applyStyle(this, false, false)
        contentDescription = spec.label
        tag = spec
        isFocusable = true
        isFocusableInTouchMode = true
        var pointerHover = false
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) focusIndex = keyViews.indexOf(this).coerceAtLeast(0)
            applyStyle(this, hasFocus, pointerHover)
        }
        setOnHoverListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> pointerHover = true
                MotionEvent.ACTION_HOVER_EXIT -> pointerHover = false
            }
            applyStyle(view as Button, view.hasFocus(), pointerHover)
            false
        }
        setOnClickListener {
            focusIndex = keyViews.indexOf(this).coerceAtLeast(0)
            requestFocus()
            handleKey(spec)
        }
        if (spec.type == KeyType.DELETE) setOnLongClickListener { startDeleteRepeat(); true }
        // A focusable-in-touch-mode view normally spends its first tap only taking focus.
        // Taking focus on DOWN makes a single mouse/touch click activate the key.
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> if (!view.isFocused) view.requestFocus()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (spec.type == KeyType.DELETE) stopDeleteRepeat()
            }
            false
        }
    }

    private fun moveFocus(direction: MoveDirection) {
        if (keyViews.isEmpty()) return
        val rectangles = keyViews.map { view ->
            val location = IntArray(2); view.getLocationOnScreen(location)
            KeyRect(location[0].toFloat(), location[1].toFloat(),
                (location[0] + view.width).toFloat(), (location[1] + view.height).toFloat())
        }
        focusIndex = FocusNavigator.findNext(rectangles, focusIndex.coerceIn(rectangles.indices), direction)
        focusCurrent()
    }

    private fun focusCurrent() {
        keyViews.getOrNull(focusIndex)?.requestFocus()
    }

    private fun activateFocusedKey() {
        val button = keyViews.getOrNull(focusIndex) ?: return
        val spec = button.tag as? KeySpec ?: return
        if (spec.type == KeyType.DELETE) {
            handleKey(spec)
            startDeleteRepeat()
        } else button.performClick()
    }

    private fun handleKey(key: KeySpec) {
        when (key.type) {
            KeyType.TEXT -> {
                commit(key.output)
                val hadOneShotShift = shift && !capsLock
                if (hadOneShotShift) shift = false
                if (hadOneShotShift) rebuild(false)
            }
            KeyType.SHIFT -> {
                // off -> once -> caps lock -> off
                if (!shift) shift = true else if (!capsLock) capsLock = true else { shift = false; capsLock = false }
                rebuild(false)
            }
            KeyType.DELETE -> deleteOne()
            KeyType.SPACE -> commit(" ")
            KeyType.ENTER -> performEditorAction()
            KeyType.MODE_SYMBOLS -> { mode = KeyboardMode.SYMBOLS; rebuild(true) }
            KeyType.MODE_LETTERS -> { mode = KeyboardMode.LETTERS; rebuild(true) }
            KeyType.CLOSE -> requestHideSelf(0)
        }
    }

    private fun commit(text: String) { currentInputConnection?.commitText(text, 1) }
    private fun deleteOne() {
        val connection = currentInputConnection ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) connection.deleteSurroundingTextInCodePoints(1, 0)
        else connection.deleteSurroundingText(1, 0)
    }

    private fun startDeleteRepeat() {
        stopDeleteRepeat()
        deleteRepeater = object : Runnable {
            override fun run() { deleteOne(); handler.postDelayed(this, 70) }
        }.also { handler.postDelayed(it, 420) }
    }
    private fun stopDeleteRepeat() { deleteRepeater?.let(handler::removeCallbacks); deleteRepeater = null }

    private fun performEditorAction() {
        val info = currentInputEditorInfo
        val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            currentInputConnection?.performEditorAction(action)
        } else {
            currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private fun actionText(info: EditorInfo?): String = when (info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
        EditorInfo.IME_ACTION_SEARCH -> "Tìm"
        EditorInfo.IME_ACTION_GO -> "Đi"
        EditorInfo.IME_ACTION_DONE -> "Xong"
        EditorInfo.IME_ACTION_NEXT -> "Tiếp"
        EditorInfo.IME_ACTION_NONE, EditorInfo.IME_ACTION_UNSPECIFIED, null -> "↵"
        else -> "↵"
    }

    /** Android-TV look: flat dark keys; focused key becomes a bright, slightly enlarged tile. */
    private fun applyStyle(button: Button, focused: Boolean, hovered: Boolean) {
        val spec = button.tag as? KeySpec
        val active = spec != null && spec.type == KeyType.SHIFT && shift
        val fill = when {
            focused -> Color.WHITE
            hovered -> 0xFF4A5260.toInt()
            active -> getColor(R.color.accent)
            spec?.type == KeyType.ENTER -> getColor(R.color.accent)
            else -> getColor(R.color.key_background)
        }
        button.background = GradientDrawable().apply {
            setColor(fill)
            cornerRadius = resources.getDimension(R.dimen.ime_corner_radius)
            if (focused) setStroke(dp(3), getColor(R.color.focus))
        }
        button.setTextColor(if (focused) getColor(R.color.keyboard_background) else Color.WHITE)
        val scale = if (focused) 1.08f else 1f
        button.animate().scaleX(scale).scaleY(scale).setDuration(90).start()
        button.elevation = if (focused) dp(8).toFloat() else 0f
    }
    private fun dimensionSp(resource: Int) = resources.getDimension(resource) / resources.displayMetrics.scaledDensity
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
