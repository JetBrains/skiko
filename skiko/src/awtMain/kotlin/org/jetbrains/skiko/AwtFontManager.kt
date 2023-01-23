package org.jetbrains.skiko

import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import java.io.File

/**
 * Manages available fonts, both at the system level and custom ones.
 *
 * The main entry point is [getTypefaceOrNull]:
 *
 * ```kotlin
 * val fontManager = AwtFontManager()
 * val myTypeface = fontManager.getTypefaceOrNull("My Font")
 * ```
 *
 * While system fonts represent global state and are shared across all
 * instances, custom fonts are only available on the instance(s) on
 * which they have been registered.
 *
 * In order to free up memory, you should remove custom fonts you don't
 * need anymore.
 */
class AwtFontManager @InternalSkikoApi constructor(
    private val systemFontProvider: SystemFontProvider,
    private val customTypefaceCache: TypefaceCache
) {

    constructor() : this(SystemFontProvider.skia, TypefaceCache.inMemory())

    /**
     * Invalidate the system font cache, causing the list of font families available
     * at the system level to be refreshed.
     *
     * The system fonts cache is **global** and shared across all [AwtFontManager]
     * instances. Calling this will impact all [AwtFontManager]'s behaviour.
     *
     * Various functions of this class will not be able to proceed until the
     * caching is completed.
     */
    fun invalidateSystemFontCache() {
        systemFontProvider.invalidate()
    }

    /**
     * Get a [Typeface] by family name and style, if it exists.
     *
     * Custom fonts take precedence over system fonts.
     */
    suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        val customTypeface = customTypefaceCache.getTypefaceOrNull(familyName, fontStyle)
        if (customTypeface != null) return customTypeface

        if (!systemFontProvider.contains(familyName)) return null
        return systemFontProvider.getTypefaceOrNull(familyName, fontStyle)
    }

    /**
     * List all known font family names, including both system fonts, and
     * custom fonts added to this instance.
     */
    suspend fun familyNames(): Set<String> =
        sortedSetOf<String>()
            .also {
                it.addAll(systemFamilyNames())
                it.addAll(customFamilyNames())
            }

    /**
     * Lists all known system font family names.
     *
     * @see getTypefaceOrNull
     */
    suspend fun systemFamilyNames() = systemFontProvider.familyNames().toSortedSet()

    /**
     * List the font families for all registered custom fonts.
     *
     * @see getTypefaceOrNull
     */
    fun customFamilyNames() = customTypefaceCache.familyNames().toSortedSet()

    /**
     * Add a classpath resource as a custom font.
     *
     * [AwtFontManager]s don't have knowledge of custom fonts added to other
     * instances.
     *
     * Don't forget to remove custom fonts when you don't need them anymore.
     *
     * @see removeCustomFontFamily
     */
    suspend fun addCustomFontResource(
        resource: String,
        loader: ClassLoader = Thread.currentThread().contextClassLoader
    ) = customTypefaceCache.addResource(resource, loader)

    /**
     * Add a [File] as a custom font.
     *
     * [AwtFontManager]s don't have knowledge of custom fonts added to other
     * instances.
     *
     * Don't forget to remove custom fonts when you don't need them anymore.
     *
     * @see removeCustomFontFamily
     */
    fun addCustomFontFile(file: File) = customTypefaceCache.addFile(file)

    /**
     * Add a [Typeface] as a custom font.
     *
     * [AwtFontManager]s don't have knowledge of custom fonts added to other
     * instances.
     *
     * Don't forget to remove custom fonts when you don't need them anymore.
     *
     * @see removeCustomFontFamily
     */
    fun addCustomFontTypeface(typeface: Typeface) =
        customTypefaceCache.addTypeface(typeface)

    /**
     * Remove a custom font by family name, if it exists.
     *
     * This will not impact other instances of [AwtFontManager]; if a custom
     * font is registered on multiple instances, it needs to be removed from
     * all of them.
     *
     * @see addCustomFontFile
     * @see addCustomFontResource
     * @see addCustomFontTypeface
     */
    fun removeCustomFontFamily(familyName: String) =
        customTypefaceCache.removeFontFamily(familyName)

    /**
     * Remove all custom fonts added to this instance.
     *
     * This will not impact other instances of [AwtFontManager]; if a custom
     * font is registered on multiple instances, it needs to be removed from
     * all of them.
     */
    fun clearCustomFonts() = customTypefaceCache.clear()

    /**
     * Indicate whether the current JVM is able to resolve font family names
     * accurately or not.
     *
     * This value will be `true` if using the JetBrains Runtime. It will be
     * `false` otherwise, indicating that this class is not able to return
     * valid values.
     *
     * If the return value is `false`, you should assume we are unable to
     * obtain the actual font family names, and font resolving might fail.
     * In particular, this is important when we need the actual family name
     * to match an AWT [Font] to a [Typeface].
     *
     * On other JVMs running on Windows and Linux, the AWT implementation is
     * not enumerating font families correctly. E.g., you may have these entries
     * for JetBrains Mono, instead of a single entry: _JetBrains Mono, JetBrains
     * Mono Bold, JetBrains Mono ExtraBold, JetBrains Mono ExtraLight, JetBrains
     * Mono Light, JetBrains Mono Medium, JetBrains Mono SemiBold, JetBrains
     * Mono Thin_.
     *
     * On the JetBrains Runtime, there are additional APIs that provide the
     * necessary information needed to list the actual font families as single
     * entries, as one would expect.
     *
     * @see toSkikoTypefaceOrNull
     */
    val isAbleToResolveFamilyNames
        get() = AwtFontUtils.isAbleToResolveFontFamilyNames
}
