package org.jetbrains.skiko.awt.font

import org.jetbrains.skiko.isRunningOnJetBrainsRuntime
import org.jetbrains.skiko.tests.runTest
import org.junit.Assume
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.streams.asSequence
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmEmbeddedFontProviderTest {
    private val provider = JvmEmbeddedFontProvider

    @Test
    fun `should be able to access JBR features when running on JBR`() = runTest {
        Assume.assumeTrue(isRunningOnJetBrainsRuntime())
        assertTrue(JvmEmbeddedFontProvider.canUseJetBrainsRuntimeFeatures, "JBR features should be available")
    }

    @Test
    fun `should not be able to access JBR features when not running on JBR`() = runTest {
        Assume.assumeFalse(isRunningOnJetBrainsRuntime())
        assertFalse(JvmEmbeddedFontProvider.canUseJetBrainsRuntimeFeatures, "JBR features should not be available")
    }

    @Test
    fun `should read JBR embedded font family name mappings`() = runTest {
        Assume.assumeTrue("Not running on the JetBrains Runtime", isRunningOnJetBrainsRuntime())

        val expectedFontFamilyMap = mapOf("Roboto-Light" to "Roboto Light", "Roboto-Thin" to "Roboto Thin")

        val detected = JvmEmbeddedFontProvider.embeddedFontFamilyMap()
        val missingMapEntries = mutableSetOf<Map.Entry<String, String>>()
        val badMapEntries = mutableMapOf<Map.Entry<String, String>, String>()
        for (familyMapEntry in expectedFontFamilyMap) {
            val key = familyMapEntry.key
            if (!detected.containsKey(key)) {
                missingMapEntries += familyMapEntry
            } else if (detected[key] != familyMapEntry.value) {
                badMapEntries += familyMapEntry to detected[key]!!
            }
        }

        assertTrue(
            actual = missingMapEntries.isEmpty(),
            message = "These JBR embedded family map entries didn't get picked up:\n" +
                    missingMapEntries.joinToString("\n") { " * $it" }
        )

        assertTrue(
            actual = badMapEntries.isEmpty(),
            message = "These JBR embedded family map entries are wrong:\n" +
                    badMapEntries.entries
                        .joinToString("\n") { (expected, actual) ->
                            " * $expected (but was: $actual)"
                        }
        )
    }

    @Test
    fun `should include all JBR embedded fonts`() = runTest {
        Assume.assumeTrue("Not running on the JetBrains Runtime", isRunningOnJetBrainsRuntime())

        val jbrFontPaths = getJbrEmbeddedFontPaths()
        assertTrue(jbrFontPaths.isNotEmpty(), "JBR embeds fonts but none was picked up by the test code")

        val detected = JvmEmbeddedFontProvider.embeddedFontFilePaths()
        val missingEmbeddedFiles = mutableSetOf<String>()
        for (jbrFontPath in jbrFontPaths) {
            if (!detected.contains(jbrFontPath)) {
                missingEmbeddedFiles += jbrFontPath
            }
        }

        assertTrue(
            actual = missingEmbeddedFiles.isEmpty(),
            message = "These JBR embedded font files didn't get picked up:\n" +
                    missingEmbeddedFiles.joinToString("\n") { " * $it" }
        )
    }

    @Test
    fun `should include all non-JBR embedded fonts`() = runTest {
        Assume.assumeFalse("Running on the JetBrains Runtime", isRunningOnJetBrainsRuntime())

        val embeddedFontPaths = getNonJbrEmbeddedFontPaths()
        Assume.assumeTrue("This JVM has no embedded fonts", embeddedFontPaths.isNotEmpty())

        val detected = JvmEmbeddedFontProvider.embeddedFontFilePaths()
        val missingEmbeddedFiles = mutableSetOf<String>()
        for (jbrFontPath in embeddedFontPaths) {
            if (!detected.contains(jbrFontPath)) {
                missingEmbeddedFiles += jbrFontPath
            }
        }

        assertTrue(
            actual = missingEmbeddedFiles.isEmpty(),
            message = "These non-JBR embedded font files didn't get picked up:\n" +
                    missingEmbeddedFiles.joinToString("\n") { " * $it" }
        )
    }

    private fun getJbrEmbeddedFontPaths(): List<String> {
        val javaHome = System.getProperty("java.home")
        val jbrFontsDirPath = Path(javaHome, "lib", "fonts")
        assertTrue(jbrFontsDirPath.exists(), "JetBrains Runtime fonts directory doesn't exist")

        return Files.walk(jbrFontsDirPath).asSequence()
            .filter { path -> path.looksLikeFontFile() }
            .map { it.absolutePathString() }
            .filterNot {
                // The JBR ships with two Bold Italic versions of JetBrains Mono; given
                // a font family can only accept one typeface per FontStyle, the prod code
                // silently drops the second of the two: JetBrainsMono-BoldItalic.ttf, as
                // it comes alphabetically after JetBrainsMono-Bold-Italic.ttf.
                it.endsWith("JetBrainsMono-BoldItalic.ttf", ignoreCase = true)
            }
            .toList()
    }

    private fun getNonJbrEmbeddedFontPaths(): List<String> {
        val javaHome = Path(System.getProperty("java.home"))
        return Files.walk(javaHome).asSequence()
            .filter { path -> path.looksLikeFontFile() }
            .map { it.absolutePathString() }
            .toList()
    }

    private fun Path.looksLikeFontFile(): Boolean =
        extension.endsWith("ttf", ignoreCase = true) ||
                extension.endsWith("otf", ignoreCase = true) ||
                extension.endsWith("ttc", ignoreCase = true)
}