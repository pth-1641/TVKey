package com.tvkey.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class RealLayoutTest {
    @Test fun downFromGUsesRowBelow() {
        // Coordinates measured on the 1920x1080 emulator.
        val row2 = listOf(34, 241, 448, 655, 854, 1069, 1276, 1483, 1690).map { KeyRect(it.toFloat(), 737f, it + 195f, 833f) }.toMutableList()
        // the focused key (g) is drawn 8% larger, so its centre sits a few px above its row neighbours
        row2[4] = KeyRect(854f, 733f, 1049f, 829f)
        val v = KeyRect(870f, 845f, 1045f, 941f)
        val c = KeyRect(683f, 845f, 858f, 941f)
        val b = KeyRect(1058f, 845f, 1233f, 941f)
        val keys = row2 + listOf(c, v, b)
        val g = 4
        val next = FocusNavigator.findNext(keys, g, MoveDirection.DOWN)
        assertEquals(keys.indexOf(v), next)
    }
}

class RealLayoutRightTest {
    @Test fun rightFromVGoesToBNotSpaceBelow() {
        // v is focused (drawn 8% larger); b is its right neighbour, Space is in the row below.
        val v = KeyRect(863f, 841f, 1039f, 937f)
        val b = KeyRect(1058f, 845f, 1234f, 941f)
        val n = KeyRect(1246f, 845f, 1422f, 941f)
        val space = KeyRect(709f, 953f, 1485f, 1050f)
        val tim = KeyRect(1497f, 953f, 1886f, 1050f)
        val keys = listOf(v, b, n, space, tim)
        assertEquals(1, FocusNavigator.findNext(keys, 0, MoveDirection.RIGHT))
    }
}

class FullKeyboardTest {
    private val rects = listOf(
        "1160,527,1400,611", "1416,527,1656,611", "1664,527,1884,611",
        "34,629,208,725", "220,629,394,725", "406,629,580,725", "592,629,766,725", "778,629,952,725",
        "964,629,1138,725", "1150,629,1325,725", "1337,629,1512,725", "1524,629,1699,725", "1711,629,1886,725",
        "34,737,229,833", "241,737,436,833", "448,737,643,833", "655,737,850,833", "854,737,1049,833",
        "1069,737,1264,833", "1276,737,1471,833", "1483,737,1678,833", "1690,737,1886,833",
        "34,845,297,941", "309,845,484,941", "496,845,671,941", "683,845,858,941", "870,845,1046,941",
        "1058,845,1234,941", "1246,845,1422,941", "1434,845,1610,941", "1622,845,1886,941",
        "34,953,227,1050", "239,953,452,1050", "464,953,697,1050", "709,953,1485,1050", "1497,953,1886,1050",
    ).map { s -> s.split(",").map { it.toFloat() }.let { KeyRect(it[0], it[1], it[2], it[3]) } }.toMutableList()

    @Test fun rightFromFocusedVGoesToB() {
        rects[26] = KeyRect(863f, 841f, 1039f, 937f)   // v, mid-animation as seen on device
        assertEquals(27, FocusNavigator.findNext(rects, 26, MoveDirection.RIGHT))
    }

    @Test fun everyKeyReachesItsNeighbours() {
        // From every key, LEFT/RIGHT stay in the same row whenever a neighbour exists there.
        for (i in 3 until rects.size) for (dir in listOf(MoveDirection.LEFT, MoveDirection.RIGHT)) {
            val next = FocusNavigator.findNext(rects, i, dir)
            val sameRowCandidates = rects.indices.filter {
                it != i && Math.abs(rects[it].centerY - rects[i].centerY) < 20f &&
                    (if (dir == MoveDirection.RIGHT) rects[it].centerX > rects[i].centerX else rects[it].centerX < rects[i].centerX)
            }
            if (sameRowCandidates.isNotEmpty()) {
                assertEquals("from $i $dir", true, next in sameRowCandidates)
            }
        }
    }
}
