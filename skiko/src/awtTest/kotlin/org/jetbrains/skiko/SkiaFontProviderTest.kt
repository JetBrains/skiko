package org.jetbrains.skiko

import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.context.isRunningOnJetBrainsRuntime
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skiko.util.assertOpensAreSet
import org.junit.Assume
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class SkiaFontProviderTest {
    private val provider = FontProvider.Skia

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
    fun `should be able to resolve logical fonts when running on JetBrains Runtime`() = runTest {
        Assume.assumeTrue(isRunningOnJetBrainsRuntime())

        assertOpensAreSet()
        val logicalFamilies = AwtFontUtils.fontFamilyNamesOrNull()!!
            .onlyAwtLogicalFamilies()
            .toSet()

        val missingLogicalFamilies = mutableSetOf<String>()
        for (logicalFamily in logicalFamilies) {
            val resolved = AwtFontUtils.resolvePhysicalFontNameOrNull(logicalFamily)
            if (resolved == null) {
                missingLogicalFamilies += logicalFamily
            }
        }

        assertTrue(
            actual = missingLogicalFamilies.isEmpty(),
            message = "These logical font families can't be resolved:\n" +
                    missingLogicalFamilies.joinToString("\n") { " * $it" }
        )
    }

    @Test
    fun `should provide all the system fonts also available via AWT`() = runTest {
        // Listing of font family names is broken on non-macOS JVM implementations,
        // except when running on the JetBrains Runtime. Our matching logic only
        // works on the JetBrains Runtime.
        Assume.assumeTrue("Not running on the JetBrains Runtime", isRunningOnJetBrainsRuntime())

        val awtFamilies = AwtFontUtils.fontFamilyNamesOrNull()!!
            .ignoreVirtualAwtFontFamilies()
            .ignoreEmbeddedFontFamilies()
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

private fun Iterable<String>.ignoreEmbeddedFontFamilies(): List<String> {
    val embeddedFamilyKeys = runBlocking { JvmEmbeddedFontProvider.familyNames() }
        .map { FontFamilyKey(it) }
    val embeddedMappedFamilyKeys = runBlocking { JvmEmbeddedFontProvider.embeddedFontFamilyMap() }
        .map { FontFamilyKey(it.value) }

    return filterNot { FontFamilyKey(it) in embeddedFamilyKeys }
        .filterNot { FontFamilyKey(it) in embeddedMappedFamilyKeys }
}

private fun Iterable<String>.onlyAwtLogicalFamilies() =
    filter { FontFamilyKey(it) in FontFamilyKey.Awt.awtLogicalFonts }
