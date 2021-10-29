package org.jetbrains.skia

import org.jetbrains.skia.impl.withStringResult
import org.jetbrains.skiko.tests.returnNativeString
import kotlin.test.Test
import kotlin.test.assertEquals

class ManagedStringTest {
    @Test
    fun basicTest() {
        val s1 = withStringResult {
            returnNativeString(0)
        }
        assertEquals("Hello", s1)
        val s2 = withStringResult {
            returnNativeString(1)
        }
        assertEquals("Привет", s2)
    }
}
