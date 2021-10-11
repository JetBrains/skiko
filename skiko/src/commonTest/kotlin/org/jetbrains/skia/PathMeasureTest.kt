package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Ignore

class PathMeasureTest {

    @Test
    fun getRSXformTest() = runTest {
        Path().moveTo(0f, 0f).lineTo(40f, 0f).moveTo(0f, 40f).lineTo(10f, 50f).use { path ->
            PathMeasure(path, false).use { measure ->
                assertEquals(RSXform(1.0f, 0f, 0.5f, 0f), measure.getRSXform(0.5f))
            }
        }
    }

    @Test
    @Ignore
    fun getTangent() = runTest {
        Path().moveTo(0f, 0f).lineTo(20f, 0f).moveTo(0f, 40f).lineTo(30f, 50f).use { path ->
            PathMeasure(path, false).use { measure ->
                assertEquals(Point(1f, 0f), measure.getTangent(2f))
            }
        }
    }

    @Test
    @Ignore
    fun pathMeasureTest() = runTest {
        Path().moveTo(0f, 0f).lineTo(40f, 0f).moveTo(0f, 40f).lineTo(10f, 50f).use { path ->
            PathMeasure(path, false).use { measure ->
                Path().lineTo(10f, 10f).use { path2 ->
                    assertEquals(40f, measure.length)
                    assertCloseEnough(Point(0f, 0f), measure.getPosition(0f))
//                    assertCloseEnough(Point(1f, 0f), measure.getTangent(0f))
//                    assertCloseEnough(Point(20f, 0f), measure.getPosition(20f))
//                    assertCloseEnough(Point(1f, 0f), measure.getTangent(20f))
//                    assertEquals(false, measure.isClosed)
//                    assertCloseEnough(
//                        Matrix33.makeTranslate(20f, 0f), measure.getMatrix(
//                            20f,
//                            getPosition = true,
//                            getTangent = false
//                        )
//                    )
//                    assertCloseEnough(
//                        Matrix33.makeRotate(0f), measure.getMatrix(
//                            20f,
//                            getPosition = false,
//                            getTangent = true
//                        )
//                    )
//                    assertCloseEnough(
//                        Matrix33.makeTranslate(20f, 0f).makeConcat(Matrix33.makeRotate(0f)),
//                        measure.getMatrix(20f, getPosition = true, getTangent = true)
//                    )
//                    measure.nextContour()
//                    assertCloseEnough(14.14213f, measure.length)
//                    assertCloseEnough(Point(0f, 40f), measure.getPosition(0f))
//                    assertCloseEnough(Point(0.70710677f, 0.70710677f), measure.getTangent(0f))
//                    assertCloseEnough(Point(4.949747f, 44.949745f), measure.getPosition(7f))
//                    assertCloseEnough(Point(0.70710677f, 0.70710677f), measure.getTangent(7f))
//                    assertCloseEnough(
//                        Matrix33.makeTranslate(4.949747f, 44.949745f), measure.getMatrix(
//                            7f,
//                            getPosition = true,
//                            getTangent = false
//                        )
//                    )
//                    assertCloseEnough(
//                        Matrix33.makeRotate(45f), measure.getMatrix(
//                            7f,
//                            getPosition = false,
//                            getTangent = true
//                        )
//                    )
//                    assertCloseEnough(
//                        Matrix33.makeTranslate(4.949747f, 44.949745f).makeConcat(Matrix33.makeRotate(45f)),
//                        measure.getMatrix(7f, getPosition = true, getTangent = true)
//                    )
//                    measure.setPath(path2, false)
//                    assertCloseEnough(14.142136f, measure.length)
                }
            }
        }
    }
}