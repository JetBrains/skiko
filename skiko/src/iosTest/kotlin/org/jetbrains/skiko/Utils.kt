package org.jetbrains.skiko

import platform.posix.fopen
import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTestsIOS {
    @Test
    fun simple() {
        val f = fopen("/etc/passwd", "r")
        println(f)
    }
}
