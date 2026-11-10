package com.tvkey.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardModelTest {
    private fun keys(mode: KeyboardMode, shift: Boolean = false, web: Boolean = false, caps: Boolean = false) =
        KeyboardModel.rows(mode, shift, "Tìm", web, caps).flatMap { it.keys }

    @Test fun everyModeHasSpaceDeleteAndEnter() {
        KeyboardMode.values().forEach { mode ->
            val k = keys(mode)
            assertTrue("$mode space", k.any { it.type == KeyType.SPACE })
            assertTrue("$mode delete", k.any { it.type == KeyType.DELETE })
            assertTrue("$mode enter", k.any { it.type == KeyType.ENTER && it.label == "Tìm" })
        }
    }

    @Test fun lettersAreOnlyPlainEnglish() {
        val letters = keys(KeyboardMode.LETTERS).filter { it.type == KeyType.TEXT && it.output.single().isLetter() }.map { it.output }
        assertEquals(26, letters.size)
        assertTrue(letters.all { it.single() in 'a'..'z' })
        assertEquals("1234567890", keys(KeyboardMode.LETTERS).take(10).joinToString("") { it.hint ?: "" })
    }

    @Test fun shiftUppercasesLetters() {
        assertTrue(keys(KeyboardMode.LETTERS, shift = true).any { it.output == "Q" })
        assertFalse(keys(KeyboardMode.LETTERS).any { it.output == "Q" })
    }

    @Test fun bottomRowMatchesGoogleTv() {
        val bottom = KeyboardModel.rows(KeyboardMode.LETTERS, false, "↵", false, false).last().keys
        assertEquals(listOf("?123", "◀", "▶", "␣", ".", "↵"), bottom.map { it.label })
        assertEquals(listOf(KeyType.CURSOR_LEFT, KeyType.CURSOR_RIGHT), bottom.drop(1).take(2).map { it.type })
        val web = KeyboardModel.rows(KeyboardMode.LETTERS, false, "↵", true, false).last().keys
        assertEquals("@", web[4].output)
    }

    @Test fun symbolPagesLinkToEachOther() {
        val p1 = keys(KeyboardMode.SYMBOLS)
        val p2 = keys(KeyboardMode.SYMBOLS2)
        assertTrue(p1.any { it.type == KeyType.MODE_SYMBOLS2 && it.label == "=\\<" })
        assertTrue(p2.any { it.type == KeyType.MODE_SYMBOLS && it.label == "123?" })
        assertTrue(p2.any { it.output == "€" } && p2.any { it.output == "π" })
        assertTrue(p1.any { it.type == KeyType.MODE_LETTERS } && p2.any { it.type == KeyType.MODE_LETTERS })
    }

    @Test fun capsLockChangesShiftGlyph() {
        val glyph = { caps: Boolean -> keys(KeyboardMode.LETTERS, true, false, caps).first { it.type == KeyType.SHIFT }.label }
        assertEquals("⇧", glyph(false)); assertEquals("⇪", glyph(true))
    }
}
