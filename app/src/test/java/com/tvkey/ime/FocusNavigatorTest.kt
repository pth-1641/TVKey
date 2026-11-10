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
}
