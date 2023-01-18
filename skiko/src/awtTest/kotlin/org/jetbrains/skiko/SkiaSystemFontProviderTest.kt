package org.jetbrains.skiko

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assume
import org.junit.Test
import kotlin.test.*


@OptIn(ExperimentalCoroutinesApi::class)
class SkiaSystemFontProviderTest {
    private val provider = SystemFontProvider.skia

    @Test
    fun `should contain at least some font families`() = runTest {
        val families = provider.familyNames()
        assertTrue(families.isNotEmpty(), "Available font families must not be empty")
    }

    @Test
    fun `should include 'System Font' and its AWT alias in system font families (only on macOS)`() = runTest {
        Assume.assumeTrue(hostOs == OS.MacOS)

        val systemFamilies = provider.familyNames()
        assertContains(
            iterable = systemFamilies,
            element = FontFamilyKey.Apple.AppleSystemUiFont.familyName,
            message = ".AppleSystemUIFont not found in system families"
        )
        assertContains(
            iterable = systemFamilies,
            element = FontFamilyKey.Apple.SystemFont.familyName,
            message = "System Font not found in system families"
        )
    }

    @Test
    fun `should provide all the system fonts also available via AWT`() = runTest {
        // Listing of font family names is broken on non-macOS JVM implementations,
        // except when running on the JetBrains Runtime. Our matching logic only
        // works on the JetBrains Runtime.
        Assume.assumeTrue(isRunningOnJetBrainsRuntime())

        val awtFamilies = AwtFontUtils.fontFamilyNamesOrNull()!!
            .ignoreVirtualAwtFontFamilies()
            .toSet()

        val skiaFamilies = provider.familyNames()

        val missingAwtFamilies = mutableSetOf<String>()
        for (awtFamily in awtFamilies) {
            if (awtFamily !in skiaFamilies) {
                missingAwtFamilies += awtFamily
            }
        }

        assertTrue(
            missingAwtFamilies.isEmpty(),
            "These AWT font families are missing:\n${missingAwtFamilies.joinToString("\n") { " * $it" }}"
        )
    }
}

private fun Iterable<String>.ignoreVirtualAwtFontFamilies() =
    filterNot { FontFamilyKey(it) in FontFamilyKey.Awt.awtLogicalFonts }

private fun isRunningOnJetBrainsRuntime() =
    System.getProperty("java.vendor")
        .equals("JetBrains s.r.o.", ignoreCase = true)
