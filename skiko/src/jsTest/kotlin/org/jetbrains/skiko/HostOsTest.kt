package org.jetbrains.skiko

import kotlin.test.Test
import kotlin.test.assertEquals

class HostOsTest {
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

private fun spoofUserAgentData(newValue: String) {
    val userAgentData = js("({})")
    userAgentData.platform = newValue
    js(
        """Object.defineProperty(navigator, 'userAgentData', {
            configurable: true,
            value: userAgentData
        });"""
    )
}
