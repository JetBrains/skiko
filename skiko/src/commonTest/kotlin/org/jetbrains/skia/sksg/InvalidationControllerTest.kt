package org.jetbrains.skia.sksg

import org.jetbrains.skia.Rect
import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertTrue

class InvalidationControllerTest {
    @Test
    fun canCreateWithBounds() {
        val controller = InvalidationController()
        assertTrue(controller.bounds.isEmpty)
        controller.invalidate(1.0f, 1.0f, 10.0f, 10.0f, null)
        assertCloseEnough(Rect(1.0f, 1.0f, 10.0f, 10.0f), controller.bounds)
        controller.reset()
        assertTrue(controller.bounds.isEmpty)
    }
}