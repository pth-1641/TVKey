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
 * Chooses the nearest key in the requested half-plane. Perpendicular offset is
 * penalised more heavily than forward distance so movement follows visual rows
 * and columns, including rows containing wide keys such as Space.
 */
object FocusNavigator {
    fun findNext(keys: List<KeyRect>, currentIndex: Int, direction: MoveDirection): Int {
        if (keys.size < 2 || currentIndex !in keys.indices) return currentIndex
        val current = keys[currentIndex]
        var bestIndex = currentIndex
        var bestScore = Float.MAX_VALUE

        keys.forEachIndexed { index, candidate ->
            if (index == currentIndex) return@forEachIndexed
            val forward = when (direction) {
                MoveDirection.LEFT -> current.centerX - candidate.centerX
                MoveDirection.RIGHT -> candidate.centerX - current.centerX
                MoveDirection.UP -> current.centerY - candidate.centerY
                MoveDirection.DOWN -> candidate.centerY - current.centerY
            }
            if (forward <= 0.5f) return@forEachIndexed
            val perpendicular = if (direction == MoveDirection.LEFT || direction == MoveDirection.RIGHT) {
                abs(candidate.centerY - current.centerY)
            } else {
                abs(candidate.centerX - current.centerX)
            }
            // A straight neighbour always wins over a diagonal neighbour with
            // the same forward distance. The current row is additionally
            // preferred, which prevents a wide key from trapping focus.
            val rowPenalty = if (direction == MoveDirection.LEFT || direction == MoveDirection.RIGHT) {
                if (rangesOverlap(current.top, current.bottom, candidate.top, candidate.bottom)) 0f else 1f
            } else {
                if (rangesOverlap(current.left, current.right, candidate.left, candidate.right)) 0f else 1f
            }
            val score = forward + perpendicular * 2.5f + rowPenalty * 0.001f
            if (score < bestScore - 0.0001f || (abs(score - bestScore) < 0.0001f && index < bestIndex)) {
                bestScore = score
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun rangesOverlap(aStart: Float, aEnd: Float, bStart: Float, bEnd: Float): Boolean =
        aStart < bEnd && bStart < aEnd
}
