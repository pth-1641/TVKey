package com.tvkey.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class VietnameseAccentsTest {
    @Test fun composesAllTonePositionsAndUppercase() {
        assertEquals("ố", VietnameseAccents.apply("ô", VietnameseAccents.ACUTE))
        assertEquals("à", VietnameseAccents.apply("a", VietnameseAccents.GRAVE))
        assertEquals("Đ", VietnameseAccents.apply("đ", VietnameseAccents.NO_TONE, true))
    }

    @Test fun composesTheAcceptancePhrase() {
        val phrase = "b${VietnameseAccents.apply("ô", VietnameseAccents.ACUTE)} " +
            "m${VietnameseAccents.apply("e", VietnameseAccents.DOT_BELOW)} xem tivi"
        assertEquals("bố mẹ xem tivi", phrase)
    }
}
