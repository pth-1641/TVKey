package com.tvkey.ime

enum class KeyboardMode { LETTERS, SYMBOLS, NUMBERS, VIETNAMESE }

enum class KeyType {
    TEXT, SHIFT, DELETE, SPACE, ENTER, MODE_SYMBOLS, MODE_LETTERS,
    LANGUAGE, CLOSE, ACCENT_BASE, TONE,
}

data class KeySpec(
    val label: String,
    val type: KeyType = KeyType.TEXT,
    val output: String = label,
    val weight: Float = 1f,
    val tone: Int = VietnameseAccents.NO_TONE,
)

data class KeyboardRow(val keys: List<KeySpec>)

/** Keeps the layout declarative and independent from Android views. */
object KeyboardModel {
    fun rows(
        mode: KeyboardMode,
        language: String,
        shift: Boolean,
        editorActionLabel: String,
        webContext: Boolean = false,
        capsLock: Boolean = false,
    ): List<KeyboardRow> = when (mode) {
        KeyboardMode.LETTERS -> letterRows(language, shift, editorActionLabel, webContext, capsLock)
        KeyboardMode.SYMBOLS -> symbolRows(editorActionLabel)
        KeyboardMode.NUMBERS -> numberRows(editorActionLabel)
        KeyboardMode.VIETNAMESE -> vietnameseRows(editorActionLabel, shift)
    }

    /** Quick-insert chips shown in the header row (Android TV style suggestions). */
    fun quickKeys(webContext: Boolean): List<KeySpec> =
        if (webContext) listOf(KeySpec(".com"), KeySpec("www."), KeySpec(".vn"))
        else listOf(KeySpec("youtube"), KeySpec(".com"))

    private fun letterRows(language: String, shift: Boolean, action: String, webContext: Boolean, capsLock: Boolean): List<KeyboardRow> {
        fun letters(text: String) = text.map { char ->
            val out = if (shift) char.uppercaseChar().toString() else char.toString()
            KeySpec(out)
        }
        return listOf(
            KeyboardRow(letters("qwertyuiop")),
            KeyboardRow(letters("asdfghjkl")),
            KeyboardRow(
                listOf(KeySpec(if (capsLock) "⇪" else "⇧", KeyType.SHIFT, weight = 1.5f)) +
                    letters("zxcvbnm") +
                    KeySpec("⌫", KeyType.DELETE, weight = 1.5f)
            ),
            KeyboardRow(
                listOf(
                    KeySpec("123", KeyType.MODE_SYMBOLS),
                    KeySpec(language, KeyType.LANGUAGE, weight = 1.1f),
                ) + (if (webContext) listOf(KeySpec("@", output = "@"), KeySpec(".", output = ".")) else emptyList()) +
                    (if (language == "VI") listOf(KeySpec("DẤU", KeyType.ACCENT_BASE, weight = 1.2f)) else emptyList()) +
                    listOf(
                    KeySpec("Space", KeyType.SPACE, weight = 4f),
                    KeySpec(action, KeyType.ENTER, weight = 2f),
                    )
            ),
        )
    }

    private fun symbolRows(action: String): List<KeyboardRow> = listOf(
        KeyboardRow("1234567890".map { KeySpec(it.toString()) }),
        KeyboardRow("-=_+[]{}|\\".map { KeySpec(it.toString()) }),
        KeyboardRow(";:'\",./?!@".map { KeySpec(it.toString()) }),
        KeyboardRow(
            listOf(
                KeySpec("ABC", KeyType.MODE_LETTERS),
                KeySpec("#", weight = 1f),
                KeySpec("&", weight = 1f),
                KeySpec("Space", KeyType.SPACE, weight = 3.5f),
                KeySpec("⌫", KeyType.DELETE),
                KeySpec(action, KeyType.ENTER, weight = 1.8f),
            )
        ),
    )

    private fun numberRows(action: String): List<KeyboardRow> = listOf(
        KeyboardRow("1234567890".map { KeySpec(it.toString()) }),
        KeyboardRow("-+.,()/*#%".map { KeySpec(it.toString()) }),
        KeyboardRow(
            listOf(
                KeySpec("ABC", KeyType.MODE_LETTERS),
                KeySpec("⇧", KeyType.SHIFT),
                KeySpec("Space", KeyType.SPACE, weight = 4f),
                KeySpec("⌫", KeyType.DELETE, weight = 1.4f),
                KeySpec(action, KeyType.ENTER, weight = 2f),
            )
        ),
    )

    private fun vietnameseRows(action: String, shift: Boolean): List<KeyboardRow> {
        val specialBases = listOf("ă", "â", "ê", "ô", "ơ", "ư", "đ").map {
            val label = if (shift) it.uppercase() else it
            KeySpec(label, KeyType.ACCENT_BASE, output = it)
        }
        val vowels = listOf("a", "e", "i", "o", "u", "y").map {
            val label = if (shift) it.uppercase() else it
            KeySpec(label, KeyType.ACCENT_BASE, output = it)
        }
        val tones = listOf(
            KeySpec("Sắc", KeyType.TONE, tone = VietnameseAccents.ACUTE),
            KeySpec("Huyền", KeyType.TONE, tone = VietnameseAccents.GRAVE),
            KeySpec("Hỏi", KeyType.TONE, tone = VietnameseAccents.HOOK),
            KeySpec("Ngã", KeyType.TONE, tone = VietnameseAccents.TILDE),
            KeySpec("Nặng", KeyType.TONE, tone = VietnameseAccents.DOT_BELOW),
            KeySpec("Không", KeyType.TONE, tone = VietnameseAccents.NO_TONE),
        )
        return listOf(
            KeyboardRow(specialBases),
            KeyboardRow(vowels),
            KeyboardRow(tones),
            KeyboardRow(
                listOf(
                    KeySpec(if (shift) "⇧ HOA" else "⇧", KeyType.SHIFT),
                    KeySpec("ABC", KeyType.MODE_LETTERS),
                    KeySpec("123", KeyType.MODE_SYMBOLS),
                    KeySpec("Space", KeyType.SPACE, weight = 4f),
                    KeySpec("⌫", KeyType.DELETE),
                    KeySpec(action, KeyType.ENTER, weight = 1.8f),
                )
            ),
        )
    }
}
