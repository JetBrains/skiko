package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegionTest {
    @Test
    fun canGetBounds() {
        val bounds = IRect(10, 10, 30, 30)
        val region = Region().apply { setRect(bounds) }

        assertEquals(bounds, region.bounds)
    }

    @Test
    fun canSetRects() {
        val bounds = IRect(10, 10, 30, 30)
        val region = Region().apply {
            setRects(arrayOf(
                IRect(10, 10, 25, 25),
                IRect(15, 15, 30, 30),
            ))
        }

        assertEquals(bounds, region.bounds)
    }

    @Test
    fun classificationAndCopies() {
        Region().use { source ->
            Region().use { copy ->
                assertTrue(source.isEmpty)
                assertFalse(source.isRect)
                assertFalse(source.isComplex)
                assertEquals(0, source.computeRegionComplexity())

                assertTrue(source.setRect(IRect(0, 0, 20, 20)))
                assertFalse(source.isEmpty)
                assertTrue(source.isRect)
                assertFalse(source.isComplex)
                assertEquals(1, source.computeRegionComplexity())
                assertTrue(copy.set(source))
                assertEquals(source.bounds, copy.bounds)

                assertTrue(source.setRects(arrayOf(IRect(0, 0, 10, 10), IRect(20, 0, 30, 10))))
                assertTrue(source.isComplex)
                assertFalse(source.isRect)
                assertTrue(source.computeRegionComplexity() > 1)
                assertTrue(copy.setRegion(source))
                copy.translate(5, -3)
                assertEquals(IRect(5, -3, 35, 7), copy.bounds)
                assertTrue(copy.contains(6, 0))
                assertFalse(copy.contains(16, 0))
                assertEquals(IRect(0, 0, 30, 10), source.bounds)

                assertFalse(copy.setEmpty())
                assertTrue(copy.isEmpty)
                assertEquals(0, copy.computeRegionComplexity())
                assertFalse(source.set(copy))
                assertTrue(source.isEmpty)
            }
        }
    }

    @Test
    fun containmentAndIntersection() {
        Region().use { region ->
            Region().use { other ->
                region.setRect(IRect(0, 0, 20, 20))
                val inside = IRect(5, 5, 10, 10)
                val overlap = IRect(15, 15, 25, 25)
                val outside = IRect(30, 30, 40, 40)

                assertTrue(region.contains(0, 0))
                assertFalse(region.contains(20, 10))
                assertTrue(region.contains(inside))
                assertFalse(region.contains(overlap))
                assertTrue(region.intersects(overlap))
                assertFalse(region.intersects(outside))
                assertTrue(region.quickContains(inside))
                assertFalse(region.quickContains(overlap))
                assertFalse(region.quickReject(overlap))
                assertTrue(region.quickReject(outside))

                other.setRect(inside)
                assertTrue(region.contains(other))
                assertTrue(region.intersects(other))
                assertFalse(region.quickReject(other))
                other.setRect(overlap)
                assertFalse(region.contains(other))
                assertTrue(region.intersects(other))
                other.setRect(outside)
                assertFalse(region.intersects(other))
                assertTrue(region.quickReject(other))
            }
        }
    }

    @Test
    fun boundaryPathsAndClippedRasterization() {
        Region().use { region ->
            Region().use { clip ->
                clip.setRect(IRect(5, 5, 15, 15))
                Path.Rect(Rect(0f, 0f, 10f, 20f)).use { path ->
                    assertTrue(region.setPath(path, clip))
                }
                val bounds = Rect(5f, 5f, 10f, 15f)
                assertEquals(IRect(5, 5, 10, 15), region.bounds)
                region.getBoundaryPath().use { boundary ->
                    assertEquals(bounds, boundary.bounds)
                    assertTrue(boundary.contains(7f, 10f))
                    assertFalse(boundary.contains(12f, 10f))
                }
                PathBuilder().use { builder ->
                    assertTrue(region.addBoundaryPath(builder))
                    builder.detach().use { boundary ->
                        assertEquals(bounds, boundary.bounds)
                        assertTrue(boundary.isLastContourClosed)
                    }
                }
                assertFalse(region.setEmpty())
                region.getBoundaryPath().use { assertTrue(it.isEmpty) }
                PathBuilder().use { builder ->
                    assertFalse(region.addBoundaryPath(builder))
                    builder.detach().use { assertTrue(it.isEmpty) }
                }
            }
        }
    }

    @Test
    fun regionOperationOverloads() {
        val left = IRect(0, 0, 20, 20)
        val right = IRect(10, 0, 30, 20)
        Region().use { first ->
            Region().use { second ->
                Region().use { result ->
                    first.setRect(left)
                    second.setRect(right)

                    assertTrue(result.set(first))
                    assertTrue(result.op(second, Region.Op.INTERSECT))
                    assertEquals(IRect(10, 0, 20, 20), result.bounds)

                    assertTrue(result.op(left, second, Region.Op.DIFFERENCE))
                    assertEquals(IRect(0, 0, 10, 20), result.bounds)
                    assertTrue(result.contains(5, 10))
                    assertFalse(result.contains(15, 10))

                    assertTrue(result.op(second, left, Region.Op.DIFFERENCE))
                    assertEquals(IRect(20, 0, 30, 20), result.bounds)
                    assertTrue(result.contains(25, 10))
                    assertFalse(result.contains(15, 10))

                    assertTrue(result.op(first, second, Region.Op.DIFFERENCE))
                    assertEquals(IRect(0, 0, 10, 20), result.bounds)
                    assertEquals(left, first.bounds)
                    assertEquals(right, second.bounds)
                }
            }
        }
    }
}
