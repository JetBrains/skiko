package org.jetbrains.skiko

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.posix.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun execPath(): String {
    return NSBundle.mainBundle.bundlePath
}

class BasicIosTests {
    @Test
    fun resourceAccess() {
        // Binary currently compiled into `build/bin/iosX64/debugTest`.
        val resPath = "${execPath()}/../../../../src/iosTest/resources"
        val path = "$resPath/data.txt"
        val f = fopen(path, "r") ?: throw Error("cannot open $path: ${strerror(errno)}")
        val buf = ByteArray(200)
        buf.usePinned {
            val rv = fread(it.addressOf(0), 1, buf.size.toULong(), f)
            assertTrue { rv != 0UL }
        }
        assertEquals("This is test data.\n", buf.toKString())
    }
}
