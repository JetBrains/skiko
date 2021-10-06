package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skia.tests.assertCloseEnough


class PathTests {
    @Test
    fun iterTest() = runTest {
        Path().moveTo(10f, 10f).lineTo(20f, 0f).lineTo(20f, 20f).closePath().use { p ->
            p.iterator().use { i ->
                assertTrue(i.hasNext())

                var s = i.next()!!
                assertEquals(PathVerb.MOVE, s.verb)
                assertEquals(Point(10f, 10f), s.p0)
                assertTrue(s.isClosedContour, "segment[0] is a closed contour")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.LINE, s.verb)
                assertEquals(Point(10f, 10f), s.p0)
                assertEquals(Point(20f, 0f), s.p1)
                assertFalse(s.isCloseLine, "segment[1] is a closed line")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.LINE, s.verb)
                assertEquals(Point(20f, 0f), s.p0)
                assertEquals(Point(20f, 20f), s.p1)
                assertFalse(s.isCloseLine, "segment[2] is a closed line")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.LINE, s.verb)
                assertEquals(Point(20f, 20f), s.p0)
                assertEquals(Point(10f, 10f), s.p1)
                assertTrue(s.isCloseLine, "segment[3] is a closed line")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.CLOSE, s.verb)
                assertEquals(Point(10f, 10f), s.p0)
                assertFalse(i.hasNext())
                assertFailsWith<NoSuchElementException> {
                    i.next()
                }
            }
        }
    }

    @Test
    fun convexityTest() {
        Path().lineTo(40f, 20f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p -> assertTrue(p.isConvex) }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertFalse(p.isConvex)
        }
    }


}