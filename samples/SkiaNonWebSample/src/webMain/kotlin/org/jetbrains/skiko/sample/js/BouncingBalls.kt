package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skiko.SkikoRenderDelegate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class BouncingBalls: SkikoRenderDelegate {
    private data class Circle(var x: Float, var y: Float, var r: Float)

    companion object {
        private fun moveCircle(c: Circle, s: Float, angle: Float, width: Int, height: Int, r: Float) {
            c.x = (c.x + s * sin(angle)).coerceAtLeast(r).coerceAtMost(width.toFloat() - r)
            c.y = (c.y + s * cos(angle)).coerceAtLeast(r).coerceAtMost(height.toFloat() - r)
        }

        private fun calculatePosition(circle: Circle, boundingWidth: Int, boundingHeight: Int): Position {
            val southmost = circle.y + circle.r
            val northmost = circle.y - circle.r
            val westmost = circle.x - circle.r
            val eastmost = circle.x + circle.r

            return when {
                southmost >= boundingHeight -> Position.TOUCHES_SOUTH
                northmost <= 0 -> Position.TOUCHES_NORTH
                eastmost >= boundingWidth -> Position.TOUCHES_EAST
                westmost <= 0 -> Position.TOUCHES_WEST
                else -> Position.INSIDE
            }
        }
    }

    private enum class Position {
        INSIDE,
        TOUCHES_SOUTH,
        TOUCHES_NORTH,
        TOUCHES_WEST,
        TOUCHES_EAST
    }


    private class BouncingBall(
        val circle: Circle,
        val velocity: Float,
        var angle: Double
    ) {
        fun recalculate(width: Int, height: Int, dt: Float) {
            val position = calculatePosition(circle, width, height)

            val dtMillis = dt / 1000000

            when (position) {
                Position.TOUCHES_SOUTH -> angle = PI - angle
                Position.TOUCHES_EAST -> angle = -angle
                Position.TOUCHES_WEST -> angle = -angle
                Position.TOUCHES_NORTH -> angle = PI - angle
                Position.INSIDE -> angle
            }

            moveCircle(circle, velocity * (dtMillis.coerceAtMost(500f) / 1000), angle.toFloat(), width, height, circle.r)
        }
    }

    private var prevTimestamp: Long = 0L

    private fun Color4f.asPaint() = Paint().apply {
        color4f = this@asPaint
        mode = PaintMode.FILL
        isAntiAlias = true
    }

    private val data = listOf(
        BouncingBall(Circle(200f, 50f, 25f), 172f, PI / 4),
        BouncingBall(Circle(100f, 100f, 10f), 162f, -PI / 3),
        BouncingBall(Circle(150f, 120f, 30f), 168f, 3 * PI / 4),
        BouncingBall(Circle(100f, 10f, 25f), 208f, -PI / 6),
        BouncingBall(Circle(120f, 100f, 40f), 120f, -1.1 * PI)
    ).zip(listOf(
        Color4f(0f, 1f, 0f, 0.8f).asPaint(),
        Color4f(0f, 0f, 1f, 0.8f).asPaint(),
        Color4f(1f, 0f, 0f, 0.8f).asPaint(),
        Color4f(1f, 0f, 1f, 0.8f).asPaint(),
        Color4f(0f, 1f, 1f, 0.8f).asPaint()
    ))

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val dtime = (nanoTime - prevTimestamp)
        prevTimestamp = nanoTime

        data.forEach { (ball, paint) ->
            ball.recalculate(width, height, dtime.toFloat())
            canvas.drawCircle(ball.circle.x, ball.circle.y, ball.circle.r, paint)
        }
    }
}