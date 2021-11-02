package org.jetbrains.skia

import org.jetbrains.skia.impl.withStringResult
import org.jetbrains.skiko.tests.nativeStringByIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class ManagedStringTest {

    @Test
    fun basicTest() {
        val s1 = withStringResult {
            nativeStringByIndex(0)
        }
        assertEquals("Hello", s1)
        val s2 = withStringResult {
            nativeStringByIndex(1)
        }
        assertEquals("Привет", s2)
        val s3 = withStringResult {
            nativeStringByIndex(2)
        }
        assertEquals("你好", s3)
    }

    @Test
    fun canCreateAndReadManagedString() {
        val ms1 = ManagedString("Hello")
        assertEquals("Hello", ms1.toString())

        val ms2 = ManagedString("Привет!")
        assertEquals("Привет!", ms2.toString())

        val ms3 = ManagedString("你好!")
        assertEquals("你好!", ms3.toString())
    }

    @Test
    fun canAppend() {
        val ms = ManagedString("Hello").append(" World!")
        assertEquals("Hello World!", ms.toString())
    }

    @Test
    fun canInsert() {
        val ms = ManagedString("World!").insert(0, "Hello ")
        assertEquals("Hello World!", ms.toString())
    }

    @Test
    fun canRemove() {
        val ms = ManagedString("World!").remove(from = 2)
        assertEquals("Wo", ms.toString())

        val ms2 = ManagedString("World!").remove(from = 2, length = 2)
        assertEquals("Wod!", ms2.toString())
    }
}
