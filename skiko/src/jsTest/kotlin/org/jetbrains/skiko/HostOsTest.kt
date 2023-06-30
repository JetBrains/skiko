package org.jetbrains.skiko.tests.org.jetbrains.skiko

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.detectHostOs
import org.jetbrains.skiko.hostOs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HostOsTest {
    @Test
    fun canGetHostOs() {
        assertNotEquals(OS.JS, hostOs)
    }

    @Test
    fun canGetLinux() {
        spoofUserAgentData("linux")
        assertEquals(OS.Linux, detectHostOs())
    }

    @Test
    fun canGetWindows() {
        spoofUserAgentData("Windows")
        assertEquals(OS.Windows, detectHostOs())
    }

    @Test
    fun canGetMacos() {
        spoofUserAgentData("macos")
        assertEquals(OS.MacOS, detectHostOs())
    }

    @Test
    fun canGetAndroid() {
        spoofUserAgentData("android")
        assertEquals(OS.Android, detectHostOs())
    }

    @Test
    fun canGetIos() {
        spoofUserAgentData("ios")
        assertEquals(OS.Ios, detectHostOs())
    }


    @Test
    fun fallbackToUnknown() {
        spoofUserAgentData("somerandomedata")
        assertEquals(OS.Unknown, detectHostOs())
    }
}

private fun spoofUserAgentData(newValue: String) =
    js("""navigator.__defineGetter__('userAgentData', function () { return { platform:newValue }; });""")

