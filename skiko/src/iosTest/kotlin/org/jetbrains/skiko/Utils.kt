package org.jetbrains.skiko

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.posix.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun resourcePath(): String {
    val kexeDir = NSBundle.mainBundle.bundlePath
    // Binary currently compiled into `build/bin/iosX64/debugTest`.
    return "$kexeDir/../../../../src/iosTest/resources"
}

class BasicIosTests {
    @Test
    fun resourceAccess() {
        val path = "${resourcePath()}/data.txt"
        val f = fopen(path, "r") ?: throw Error("cannot open $path: ${strerror(errno)?.toKString()}")
        val buf = ByteArray(200)
        buf.usePinned {
            val rv = fread(it.addressOf(0), 1u, buf.size.toULong(), f)
            assertTrue { rv != 0UL }
        }
        assertEquals("This is test data.\n", buf.toKString())
        fclose(f)
    }
}
