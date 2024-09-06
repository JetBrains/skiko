package org.jetbrains.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skikoInvokeV
import org.jetbrains.skia.someFoo
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class tttesttss {

//    @Test
//    fun t() = runTest {
//        assertEquals(10, someFoo())
//    }

    @Test
    fun t2() = runTest {
        println("Start test")
        skikoInvokeV(1)
        withContext(Dispatchers.Default) {
            delay(100)
        }
        println("Test finished")
    }

    @Test
    fun test3() = runTest {
        println("test3")
    }

    @Test
    fun test4()  {
        println("test4")
    }
}