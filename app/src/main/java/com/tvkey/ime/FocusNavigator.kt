package com.tvkey.ime

import kotlin.math.abs

enum class MoveDirection { LEFT, RIGHT, UP, DOWN }

/** Pure-JVM geometry used by the D-pad focus algorithm. */
data class KeyRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

/**
 * Chooses the next key for a D-pad move. Movement first picks the nearest row
 * (or column) in the requested direction, and only then the key in that band
 * that is best aligned with the current one. This keeps focus on adjacent rows
 * even when a wide key (Space, Enter) further away is better aligned, and a
 * wide key never traps focus because alignment is measured by interval overlap.
 */
object FocusNavigator {
    fun findNext(keys: List<KeyRect>, currentIndex: Int, direction: MoveDirection): Int {
        if (keys.size < 2 || currentIndex !in keys.indices) return currentIndex
        val current = keys[currentIndex]
        val horizontal = direction == MoveDirection.LEFT || direction == MoveDirection.RIGHT

        fun forward(k: KeyRect) = when (direction) {
            MoveDirection.LEFT -> current.centerX - k.centerX
            MoveDirection.RIGHT -> k.centerX - current.centerX
            MoveDirection.UP -> current.centerY - k.centerY
            MoveDirection.DOWN -> k.centerY - current.centerY
        }
        // Gap between the keys' extents on the axis perpendicular to the move (0 = overlapping).
        fun gap(k: KeyRect) = if (horizontal) intervalGap(current.top, current.bottom, k.top, k.bottom)
        else intervalGap(current.left, current.right, k.left, k.right)
        fun offCenter(k: KeyRect) = if (horizontal) abs(k.centerY - current.centerY) else abs(k.centerX - current.centerX)

        // A key is "ahead" only when its centre is past the edge of the current key. Comparing
        // against half the extent (not 0) ignores sub-pixel/scale offsets: the focused key is drawn
        // slightly enlarged, which shifts its centre a few pixels relative to keys in its own row.
        val extent = if (horizontal) current.right - current.left else current.bottom - current.top
        val candidates = keys.indices.filter { it != currentIndex && forward(keys[it]) > extent * 0.5f }
        if (candidates.isEmpty()) return currentIndex

        if (horizontal) {
            // Stay in the current row: the nearest key that overlaps this key vertically. Only when
            // the row has nothing further in that direction do we look at other rows.
            val sameRow = candidates.filter { gap(keys[it]) == 0f }
            val pool = sameRow.ifEmpty { candidates }
            return pool.minWithOrNull(
                compareBy<Int>({ gap(keys[it]) }, { forward(keys[it]) }, { offCenter(keys[it]) }, { it })
            ) ?: currentIndex
        }

        // Vertical: take the nearest row first (the tolerance groups keys of one row), then the key
        // in that row that is best aligned. A distant wide key never beats an adjacent row.
        val nearest = candidates.minOf { forward(keys[it]) }
        val band = candidates.filter { forward(keys[it]) <= nearest + extent * 0.5f }
        return band.minWithOrNull(
            compareBy<Int>({ gap(keys[it]) > 0f }, { gap(keys[it]) }, { offCenter(keys[it]) }, { forward(keys[it]) }, { it })
        ) ?: currentIndex
    }

    private fun intervalGap(aStart: Float, aEnd: Float, bStart: Float, bEnd: Float): Float =
        maxOf(0f, maxOf(aStart, bStart) - minOf(aEnd, bEnd))
}
