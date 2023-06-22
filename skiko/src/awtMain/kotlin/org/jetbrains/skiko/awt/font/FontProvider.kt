@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package org.jetbrains.skiko.awt.font

import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skiko.awt.font.FontProvider.Companion.Skia
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute

/**
 * A cache of font family names available in the system.
 *
 * You can get the default, global implementation from [Skia].
 */
internal interface FontProvider {

    /**
     * Invalidate the system font cache, causing the list of font families available
     * at the system level to be refreshed.
     */
    fun invalidate()

    /**
     * List all known system font family names.
     *
     * You can call [invalidate] if you need to refresh this list.
     * This function will suspend until the caching is complete.
     *
     * @see invalidate
     */
    suspend fun familyNames(): Set<String>


    /**
     * Check if the given [familyName] exists in the system.
     *
     * This function will suspend until the caching is complete.
     *
     * @see invalidate
     * @see familyNames
     */
    suspend fun contains(familyName: String): Boolean

    /**
     * Load a [Typeface] from the system, given a [familyName] and a [fontStyle].
     */
    suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface?

    /**
     * Load a [FontFamily] from the system, given a [familyName].
     */
    suspend fun getFontFamilyOrNull(familyName: String): FontFamily?

    companion object {

        /**
         * The default, global implementation of the interface.
         * It uses Skia APIs to enumerate available font families.
         */
        val Skia: FontProvider
            get() = SkiaFontProvider

        /**
         * The default, global implementation of the interface.
         * It uses Skia APIs to enumerate available font families.
         */
        val JvmEmbedded: FontProvider
            get() = JvmEmbeddedFontProvider
    }
}

internal fun Path.isParentOf(other: Path): Boolean {
    val parent = absolute().normalize()
    val child = other.absolute().normalize()

    if (parent.nameCount >= other.nameCount) return false

    val childParent = child.parent ?: return false
    return parent.isSameFileAs(childParent) || parent.isParentOf(childParent)
}

internal fun Path.isSameFileAs(other: Path): Boolean =
    try {
        // Try to use Files.isSameFile() as it also follows symlinks
        Files.isSameFile(this, other)
    } catch (e: IOException) {
        // Fall back on simple path equivalence
        absolute().normalize() == other.absolute().normalize()
    }
