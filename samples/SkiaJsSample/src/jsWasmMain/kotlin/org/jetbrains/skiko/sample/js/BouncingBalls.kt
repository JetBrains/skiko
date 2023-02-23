package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.*
import org.jetbrains.skiko.SkikoView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class BouncingBalls: SkikoView {
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
        var angle: Double,
        val colorPaint: Paint
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

    private fun createBouncingBall(offset: Pair<Float, Float> = randomOffset()): BouncingBall {
        return BouncingBall(
            circle = Circle(x = offset.first, y = offset.second, r = random.nextInt(1, 20).toFloat()),
            velocity = random.nextInt(100, 200).toFloat(),
            angle = angles.random(),
            colorPaint = colors.random().asPaint()
        )
    }

    private val data = (0..1000).map {
        createBouncingBall()
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val dtime = (nanoTime - prevTimestamp)
        prevTimestamp = nanoTime

        data.forEach {ball ->
            ball.recalculate(width, height, dtime.toFloat())
            canvas.drawCircle(ball.circle.x, ball.circle.y, ball.circle.r, ball.colorPaint)
        }
    }
}

private val random = Random(100)
private val angles = listOf(PI / 4, -PI / 3, 3 * PI / 4, -PI / 6, -1.1 * PI)
private val colors = listOf(Color4f(0f, 1f, 0f, 0.8f),
    Color4f(0f, 0f, 1f, 0.8f),
    Color4f(1f, 0f, 0f, 0.8f),
    Color4f(1f, 0f, 1f, 0.8f),
    Color4f(0f, 1f, 1f, 0.8f),
    Color4f(0f, 1f, 0f, 0.8f),
    Color4f(0f, 0f, 1f, 0.8f),
    Color4f(1f, 0f, 0f, 0.8f),
    Color4f(1f, 0f, 1f, 0.8f),
    Color4f(0f, 1f, 1f, 0.8f)
)

private fun randomOffset(): Pair<Float, Float> {
    return random.nextInt(10, 200).toFloat() to
            random.nextInt(10, 200).toFloat()
}