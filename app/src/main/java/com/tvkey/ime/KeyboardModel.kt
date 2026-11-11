package com.tvkey.ime

enum class KeyboardMode { LETTERS, SYMBOLS, SYMBOLS2, NUMBERS }

enum class KeyType {
    TEXT, SHIFT, DELETE, SPACE, ENTER, MODE_SYMBOLS, MODE_SYMBOLS2, MODE_LETTERS,
    CURSOR_LEFT, CURSOR_RIGHT, CLEAR_ALL,
}

data class KeySpec(
    val label: String,
    val type: KeyType = KeyType.TEXT,
    val output: String = label,
    val weight: Float = 1f,
    /** Small corner hint (e.g. the digit on a letter key); long-press/long-click types it. */
    val hint: String? = null,
)

data class KeyboardRow(val keys: List<KeySpec>)

/** Keeps the layout declarative and independent from Android views. Mirrors the Google TV keyboard. */
object KeyboardModel {
    /** Open-box glyph, as on the Google TV keyboard. */
    const val SPACE_LABEL = "␣"
    const val CLEAR_LABEL = "Xóa hết"

    fun rows(
        mode: KeyboardMode,
        shift: Boolean,
        editorActionLabel: String,
        webContext: Boolean = false,
        capsLock: Boolean = false,
    ): List<KeyboardRow> = when (mode) {
        KeyboardMode.LETTERS -> letterRows(shift, editorActionLabel, webContext, capsLock)
        KeyboardMode.SYMBOLS -> symbolRows(editorActionLabel)
        KeyboardMode.SYMBOLS2 -> symbolRows2(editorActionLabel)
        KeyboardMode.NUMBERS -> numberRows(editorActionLabel)
    }

    private fun chars(text: String) = text.map { KeySpec(it.toString()) }

    /** ◀ ▶ move the text cursor, as on the Google TV keyboard. */
    /** "Xóa hết" (clear all) is borrowed from the Samsung TV keyboard; ".com" appears for web/email fields. */
    private fun bottomRow(first: KeySpec, punctuation: String, action: String, dotCom: Boolean = false) = KeyboardRow(
        listOf(
            first,
            KeySpec("◀", KeyType.CURSOR_LEFT),
            KeySpec("▶", KeyType.CURSOR_RIGHT),
            KeySpec(SPACE_LABEL, KeyType.SPACE, weight = if (dotCom) 2.6f else 3f),
            KeySpec(punctuation),
        ) + (if (dotCom) listOf(KeySpec(".com", weight = 1.3f)) else emptyList()) + listOf(
            KeySpec(CLEAR_LABEL, KeyType.CLEAR_ALL, weight = 1.5f),
            KeySpec(action, KeyType.ENTER, weight = 1.4f),
        )
    )

    private fun letterRows(shift: Boolean, action: String, webContext: Boolean, capsLock: Boolean): List<KeyboardRow> {
        fun letters(text: String, hints: String? = null) = text.mapIndexed { i, char ->
            val out = if (shift) char.uppercaseChar().toString() else char.toString()
            KeySpec(out, hint = hints?.get(i)?.toString())
        }
        return listOf(
            KeyboardRow(letters("qwertyuiop", "1234567890")),
            KeyboardRow(letters("asdfghjkl")),
            KeyboardRow(
                listOf(KeySpec(if (capsLock) "⇪" else "⇧", KeyType.SHIFT, weight = 1.5f)) +
                    letters("zxcvbnm") +
                    KeySpec("⌫", KeyType.DELETE, weight = 1.5f)
            ),
            bottomRow(KeySpec("?123", KeyType.MODE_SYMBOLS, weight = 1.6f), if (webContext) "@" else ".", action, dotCom = webContext),
        )
    }

    private fun symbolRows(action: String): List<KeyboardRow> = listOf(
        KeyboardRow(chars("1234567890")),
        KeyboardRow(chars("@#\$_&-+()/")),
        KeyboardRow(
            listOf(KeySpec("=\\<", KeyType.MODE_SYMBOLS2, weight = 1.5f)) + chars("*\"':;!?") +
                KeySpec("%") + KeySpec("⌫", KeyType.DELETE, weight = 1.5f)
        ),
        bottomRow(KeySpec("ABC", KeyType.MODE_LETTERS, weight = 1.6f), ",", action),
    )

    private fun symbolRows2(action: String): List<KeyboardRow> = listOf(
        KeyboardRow(chars("~`|•√π÷×¶Δ")),
        KeyboardRow(chars("£¢€¥^°={}\\")),
        KeyboardRow(
            listOf(KeySpec("123?", KeyType.MODE_SYMBOLS, weight = 1.5f)) + chars("%©®✓[]<>") +
                KeySpec("⌫", KeyType.DELETE, weight = 1.5f)
        ),
        bottomRow(KeySpec("ABC", KeyType.MODE_LETTERS, weight = 1.6f), ".", action),
    )

    private fun numberRows(action: String): List<KeyboardRow> = listOf(
        KeyboardRow(chars("1234567890")),
        KeyboardRow(chars("-+.,()/*#%")),
        KeyboardRow(
            listOf(
                KeySpec("ABC", KeyType.MODE_LETTERS, weight = 1.5f),
                KeySpec("◀", KeyType.CURSOR_LEFT),
                KeySpec("▶", KeyType.CURSOR_RIGHT),
                KeySpec(SPACE_LABEL, KeyType.SPACE, weight = 2.5f),
                KeySpec("⌫", KeyType.DELETE, weight = 1.3f),
                KeySpec(CLEAR_LABEL, KeyType.CLEAR_ALL, weight = 1.5f),
                KeySpec(action, KeyType.ENTER, weight = 1.5f),
            )
        ),
    )
}
