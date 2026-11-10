package com.tvkey.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardModelTest {
    private fun types(mode: KeyboardMode, language: String = "EN", shift: Boolean = false, web: Boolean = false) =
        KeyboardModel.rows(mode, language, shift, "Tìm", web).flatMap { it.keys }

    @Test fun everyModeHasSpaceDeleteAndEnter() {
        KeyboardMode.values().forEach { mode ->
            val keys = types(mode)
            assertTrue("$mode space", keys.any { it.type == KeyType.SPACE })
            assertTrue("$mode delete", keys.any { it.type == KeyType.DELETE })
            assertTrue("$mode enter", keys.any { it.type == KeyType.ENTER && it.label == "Tìm" })
        }
    }

    @Test fun shiftUppercasesLetters() {
        assertTrue(types(KeyboardMode.LETTERS, shift = true).any { it.output == "Q" })
        assertFalse(types(KeyboardMode.LETTERS, shift = false).any { it.output == "Q" })
    }

    @Test fun accentKeyOnlyInVietnamese() {
        assertFalse(types(KeyboardMode.LETTERS, "EN").any { it.type == KeyType.ACCENT_BASE })
        assertTrue(types(KeyboardMode.LETTERS, "VI").any { it.label == "DẤU" })
    }

    @Test fun urlContextAddsAtAndDot() {
        assertTrue(types(KeyboardMode.LETTERS, web = true).any { it.output == "@" })
        assertFalse(types(KeyboardMode.LETTERS, web = false).any { it.output == "@" })
    }

    @Test fun vietnameseModeOffersAllTones() {
        val tones = types(KeyboardMode.VIETNAMESE).filter { it.type == KeyType.TONE }.map { it.tone }
        assertEquals(6, tones.size)
        assertTrue(tones.containsAll(listOf(0, 1, 2, 3, 4, VietnameseAccents.NO_TONE)))
    }

    @Test fun quickKeysDependOnContext() {
        assertTrue(KeyboardModel.quickKeys(true).any { it.output == "www." })
        assertTrue(KeyboardModel.quickKeys(false).any { it.output == "youtube" })
    }

    @Test fun capsLockChangesShiftGlyph() {
        val shiftKey = { caps: Boolean ->
            KeyboardModel.rows(KeyboardMode.LETTERS, "EN", true, "Enter", false, caps)
                .flatMap { it.keys }.first { it.type == KeyType.SHIFT }.label
        }
        assertEquals("⇧", shiftKey(false)); assertEquals("⇪", shiftKey(true))
    }
}
