package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PathUtilsTest {

    @Test
    fun fillPathWithPaint() = runTest {
        val paint = Paint().apply {
            color = 0xff000000.toInt()
            strokeWidth = 10f
            mode = PaintMode.STROKE
        }
        val path = Path().lineTo(40f, 40f)

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
        val path = Path().arcTo(Rect(0f, 0f, 40f, 40f), 0f, 90f, false)

        val fillPath1 = PathUtils.fillPathWithPaint(path, paint,null, 1f)
        val fillPath001 = PathUtils.fillPathWithPaint(path, paint,null, 0.01f)

        // assert 1f scale has higher precision (more points) than 0.01f
        assertTrue(fillPath1.pointsCount > fillPath001.pointsCount)
    }

}
