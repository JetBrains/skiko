package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Circle(var x: Float, var y: Float, var r: Float)

private fun Canvas.draw(circle: Circle, paint: Paint) {
    drawCircle(circle.x, circle.y, circle.r, paint)
}

private fun Circle.move(s: Float, angle: Float) {
    x += s * sin(angle)
    y += s * cos(angle)
}

private enum class Position {
    INSIDE,
    TOUCHES_SOUTH,
    TOUCHES_NORTH,
    TOUCHES_WEST,
    TOUCHES_EAST
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

private class BouncingBall(
    val circle: Circle,
    val width: Int,
    val height: Int,
    val velocity: Float,
    var angle: Double
) {
    fun recalculate(dt: Float) {
        val position = calculatePosition(circle, width, height)

        when (position) {
            Position.TOUCHES_SOUTH -> angle = PI - angle
            Position.TOUCHES_EAST -> angle = -angle
            Position.TOUCHES_WEST -> angle = -angle
            Position.TOUCHES_NORTH -> angle = PI - angle
        }

        circle.move(velocity * (dt / 1000), angle.toFloat())
    }
}

private fun Canvas.draw(ball: BouncingBall, paint: Paint) {
    draw(ball.circle, paint)
}

private class BouncingBallsApp(htmlCanvas: HTMLCanvasElement, private var prevTimestamp: Double) : CanvasRenderer(htmlCanvas) {
    private fun Color4f.asPaint() = Paint().apply {
        color4f = this@asPaint
        mode = PaintMode.FILL
        isAntiAlias = true
    }

    val data = listOf(
        BouncingBall(Circle(200f, 50f, 25f), width, height, 172f, PI / 4),
        BouncingBall(Circle(100f, 100f, 10f), width, height, 162f, -PI / 3),
        BouncingBall(Circle(150f, 120f, 30f), width, height, 168f, 3 * PI / 4),
        BouncingBall(Circle(100f, 10f, 25f), width, height, 208f, -PI / 6),
        BouncingBall(Circle(120f, 100f, 40f), width, height, 120f, -1.1 * PI)
    ).zip(listOf(
        Color4f(0f, 1f, 0f, 0.8f).asPaint(),
        Color4f(0f, 0f, 1f, 0.8f).asPaint(),
        Color4f(1f, 0f, 0f, 0.8f).asPaint(),
        Color4f(1f, 0f, 1f, 0.8f).asPaint(),
        Color4f(0f, 1f, 1f, 0.8f).asPaint()
    ))

    override fun drawFrame(currentTimestamp: Double) {
        val dtime = (currentTimestamp - prevTimestamp)
        prevTimestamp = currentTimestamp

        data.forEach { (ball, paint) ->
            ball.recalculate(dtime.toFloat())
            canvas.draw(ball, paint)
        }
    }
}

private class DemoApp(htmlCanvas: HTMLCanvasElement) : CanvasRenderer(htmlCanvas) {
    private val paint = Paint()

    override fun drawFrame(currentTimestamp: Double) {
        canvas.draw(Circle(200f, 50f, 25f), paint)
        canvas.drawLine(100f, 100f, 200f, 200f, paint)

        canvas.drawRect(Rect(10f, 20f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
    }
}

fun main() {
    onWasmReady {
        val syncNow = window.performance.now()
        BouncingBallsApp(document.getElementById("c") as HTMLCanvasElement, syncNow).draw()
        BouncingBallsApp(document.getElementById("b") as HTMLCanvasElement, syncNow).draw()
        DemoApp(document.getElementById("a") as HTMLCanvasElement).draw()
    }
}
