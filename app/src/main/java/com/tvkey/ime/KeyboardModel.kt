package com.tvkey.ime

enum class KeyboardMode { LETTERS, SYMBOLS, NUMBERS }

enum class KeyType {
    TEXT, SHIFT, DELETE, SPACE, ENTER, MODE_SYMBOLS, MODE_LETTERS,
    CLOSE,
}

data class KeySpec(
    val label: String,
    val type: KeyType = KeyType.TEXT,
    val output: String = label,
    val weight: Float = 1f,
)

data class KeyboardRow(val keys: List<KeySpec>)

/** Keeps the layout declarative and independent from Android views. */
object KeyboardModel {
    fun rows(
        mode: KeyboardMode,
        shift: Boolean,
        editorActionLabel: String,
        webContext: Boolean = false,
        capsLock: Boolean = false,
    ): List<KeyboardRow> = when (mode) {
        KeyboardMode.LETTERS -> letterRows(shift, editorActionLabel, webContext, capsLock)
        KeyboardMode.SYMBOLS -> symbolRows(editorActionLabel)
        KeyboardMode.NUMBERS -> numberRows(editorActionLabel)
    }

    /** Quick-insert chips shown in the header row (Android TV style suggestions). */
    fun quickKeys(webContext: Boolean): List<KeySpec> =
        if (webContext) listOf(KeySpec(".com"), KeySpec("www."), KeySpec(".vn"))
        else listOf(KeySpec("youtube"), KeySpec(".com"))

    private const val SPACE_LABEL = "Dấu cách"

    /** Gboard-style: number row, three letter rows, then ?123 , Space . Enter. */
    private fun letterRows(shift: Boolean, action: String, webContext: Boolean, capsLock: Boolean): List<KeyboardRow> {
        fun letters(text: String) = text.map { char ->
            val out = if (shift) char.uppercaseChar().toString() else char.toString()
            KeySpec(out)
        }
        return listOf(
            KeyboardRow("1234567890".map { KeySpec(it.toString()) }),
            KeyboardRow(letters("qwertyuiop")),
            KeyboardRow(letters("asdfghjkl")),
            KeyboardRow(
                listOf(KeySpec(if (capsLock) "⇪" else "⇧", KeyType.SHIFT, weight = 1.5f)) +
                    letters("zxcvbnm") +
                    KeySpec("⌫", KeyType.DELETE, weight = 1.5f)
            ),
            KeyboardRow(
                listOf(
                    KeySpec("?123", KeyType.MODE_SYMBOLS, weight = 1.5f),
                    KeySpec(if (webContext) "@" else ","),
                    KeySpec(SPACE_LABEL, KeyType.SPACE, weight = 5f),
                    KeySpec("."),
                    KeySpec(action, KeyType.ENTER, weight = 1.5f),
                )
            ),
        )
    }

    private fun symbolRows(action: String): List<KeyboardRow> = listOf(
        KeyboardRow("1234567890".map { KeySpec(it.toString()) }),
        KeyboardRow("@#$%&-+()/".map { KeySpec(it.toString()) }),
        KeyboardRow("*\"':;!?=_".map { KeySpec(it.toString()) } + KeySpec("⌫", KeyType.DELETE)),
        KeyboardRow(
            listOf(
                KeySpec("ABC", KeyType.MODE_LETTERS, weight = 1.5f),
                KeySpec(","),
                KeySpec(SPACE_LABEL, KeyType.SPACE, weight = 5f),
                KeySpec("."),
                KeySpec(action, KeyType.ENTER, weight = 1.5f),
            )
        ),
    )

    private fun numberRows(action: String): List<KeyboardRow> = listOf(
        KeyboardRow("1234567890".map { KeySpec(it.toString()) }),
        KeyboardRow("-+.,()/*#%".map { KeySpec(it.toString()) }),
        KeyboardRow(
            listOf(
                KeySpec("ABC", KeyType.MODE_LETTERS, weight = 1.5f),
                KeySpec(SPACE_LABEL, KeyType.SPACE, weight = 4f),
                KeySpec("⌫", KeyType.DELETE, weight = 1.5f),
                KeySpec(action, KeyType.ENTER, weight = 2f),
            )
        ),
    )
}
