package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class PathUtilsTest {

    @Test
    fun fillPathWithPaint() = runTest {
        val paint = Paint().apply {
            color = 0xff000000.toInt()
            strokeWidth = 10f
            mode = PaintMode.STROKE
        }
        val path = PathBuilder().lineTo(40f, 40f).detach()

        val fillPath = PathUtils.fillPathWithPaint(path, paint)

        assertTrue(fillPath.isLastContourClosed)
        assertTrue(fillPath.pointsCount > path.pointsCount)
        assertTrue(fillPath.verbsCount > path.verbsCount)
    }

    @Test
    fun fillPathWithPaintScale() = runTest {
        val paint = Paint().apply {
            color = 0xff000000.toInt()
            strokeWidth = 10f
            mode = PaintMode.STROKE
        }
        val path = PathBuilder().arcTo(0f, 0f, 40f, 40f, 0f, 90f, false).detach()

        val fillPath1Builder = PathBuilder()
        val fillPath001Builder = PathBuilder()
        assertTrue(PathUtils.fillPathWithPaint(path, paint, fillPath1Builder, null, 1f))
        assertTrue(PathUtils.fillPathWithPaint(path, paint, fillPath001Builder, null, 0.01f))
        val fillPath1 = fillPath1Builder.detach()
        val fillPath001 = fillPath001Builder.detach()

        // assert 1f scale has higher precision (more points) than 0.01f
        assertTrue(fillPath1.pointsCount > fillPath001.pointsCount)
    }

}
