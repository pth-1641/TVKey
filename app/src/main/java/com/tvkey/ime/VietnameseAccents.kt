package com.tvkey.ime

import java.util.Locale

/** Remote-friendly Vietnamese composition: choose a base vowel, then a tone. */
object VietnameseAccents {
    const val NO_TONE = -1
    const val ACUTE = 0       // sắc
    const val GRAVE = 1       // huyền
    const val HOOK = 2        // hỏi
    const val TILDE = 3       // ngã
    const val DOT_BELOW = 4   // nặng

    private val forms = mapOf(
        'a' to "aáàảãạ",
        'ă' to "ăắằẳẵặ",
        'â' to "âấầẩẫậ",
        'e' to "eéèẻẽẹ",
        'ê' to "êếềểễệ",
        'i' to "iíìỉĩị",
        'o' to "oóòỏõọ",
        'ô' to "ôốồổỗộ",
        'ơ' to "ơớờởỡợ",
        'u' to "uúùủũụ",
        'ư' to "ưứừửữự",
        'y' to "yýỳỷỹỵ",
        'd' to "d",
        'đ' to "đ",
    )

    /**
     * Returns a composed Unicode character. Unknown bases or invalid tone
     * indexes return the base unchanged; no user text is retained anywhere.
     */
    fun apply(base: String, tone: Int, uppercase: Boolean = false): String {
        val normalized = base.lowercase(Locale.ROOT)
        if (normalized.length != 1) return base
        val row = forms[normalized[0]] ?: return base
        if (tone < NO_TONE || tone >= row.length - 1) {
            // tone == -1 is valid (no tone); values above the available row are invalid.
            if (tone != NO_TONE) return base
        }
        val value = row[if (tone == NO_TONE) 0 else tone + 1].toString()
        return if (uppercase) value.uppercase(Locale.ROOT) else value
    }

    fun isVowelBase(base: String): Boolean {
        val normalized = base.lowercase(Locale.ROOT)
        return normalized.length == 1 && normalized[0] in forms.keys && normalized[0] != 'd' && normalized[0] != 'đ'
    }
}
