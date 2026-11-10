package com.tvkey.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusNavigatorTest {
    @Test fun movesAcrossWideSpaceWithoutGettingTrapped() {
        val rects = listOf(
            KeyRect(0f, 0f, 50f, 50f),
            KeyRect(55f, 0f, 105f, 50f),
            KeyRect(0f, 60f, 300f, 110f),
            KeyRect(305f, 60f, 355f, 110f),
        )
        assertEquals(3, FocusNavigator.findNext(rects, 2, MoveDirection.RIGHT))
        assertEquals(1, FocusNavigator.findNext(rects, 2, MoveDirection.UP))
    }

    @Test fun choosesAlignedKeyOverDiagonal() {
        val rects = listOf(
            KeyRect(0f, 0f, 40f, 40f),
            KeyRect(45f, 0f, 85f, 40f),
            KeyRect(90f, 45f, 130f, 85f),
        )
        assertEquals(1, FocusNavigator.findNext(rects, 0, MoveDirection.RIGHT))
    }

    @Test fun downGoesToAdjacentRowNotWideKeyTwoRowsAway() {
        // Mirrors the Vietnamese accent screen: 7 keys, 6 keys, 6 keys, then a wide Space row.
        val row1 = (0 until 7).map { KeyRect(it * 100f, 0f, it * 100f + 95f, 50f) }
        val row2 = (0 until 6).map { KeyRect(it * 117f, 60f, it * 117f + 112f, 110f) }
        val row3 = (0 until 6).map { KeyRect(it * 117f, 120f, it * 117f + 112f, 170f) }
        val space = KeyRect(230f, 180f, 520f, 230f)
        val all = row1 + row2 + row3 + listOf(space)
        val from = 3 // fourth key of row 1
        val down1 = FocusNavigator.findNext(all, from, MoveDirection.DOWN)
        assertEquals(true, down1 in 7..12)
        val down2 = FocusNavigator.findNext(all, down1, MoveDirection.DOWN)
        assertEquals(true, down2 in 13..18)
    }

    @Test fun staysPutAtEdges() {
        val rects = listOf(KeyRect(0f, 0f, 50f, 50f), KeyRect(55f, 0f, 105f, 50f))
        assertEquals(0, FocusNavigator.findNext(rects, 0, MoveDirection.LEFT))
        assertEquals(1, FocusNavigator.findNext(rects, 1, MoveDirection.DOWN))
    }
}
