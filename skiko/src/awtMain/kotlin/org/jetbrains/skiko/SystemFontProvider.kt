@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package org.jetbrains.skiko

import kotlinx.coroutines.*
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.FontStyleSet
import org.jetbrains.skia.Typeface
import org.jetbrains.skiko.SystemFontProvider.Companion.skia
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * A cache of font family names available in the system.
 *
 * You can get the default, global implementation from [skia].
 */
@InternalSkikoApi
interface SystemFontProvider {

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
        val skia: SystemFontProvider
            get() = SkiaSystemFontProvider
    }
}

private object SkiaSystemFontProvider : SystemFontProvider {

    private val familyNamesCache: MutableSet<FontFamilyKey> = ConcurrentHashMap.newKeySet(FontMgr.default.familiesCount)
    private val _familyNames: MutableSet<String> = ConcurrentHashMap.newKeySet(FontMgr.default.familiesCount)
    private val awtLogicalFamilyNames: MutableMap<FontFamilyKey, String> =
        ConcurrentHashMap(FontFamilyKey.Awt.awtLogicalFonts.size)

    @Volatile
    private var allFontsCachedImpl = false
    private val waitChannel = RendezvousBroadcastChannel<Unit>()
    private var cacheJob: Job? = null

    init {
        invalidate()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun invalidate() {
        cacheJob?.cancel()

        allFontsCachedImpl = false
        cacheJob = GlobalScope.launch {
            cacheAllSystemFonts()
            cacheAwtLogicalFonts()
            allFontsCachedImpl = true
            waitChannel.sendAll(Unit)
        }
    }

    private suspend fun cacheAllSystemFonts() {
        familyNamesCache.clear()

        val fontManager = FontMgr.default
        val familyNames = mutableSetOf<String>()
        (0 until fontManager.familiesCount)
            .map { i -> fontManager.getFamilyName(i) }
            .forEach { familyName ->
                familyNamesCache.add(FontFamilyKey(familyName))
                familyNames += familyName
                yield()
            }

        // Since macOS pretends the San Francisco font doesn't exist, we force-load it into
        // our cache, and store it as "System font" and as ".AppleSystemUIFont".
        // The latter is done for Swing/AWT interop reasons, since AWT loads it as
        // ".AppleSystemUIFont" (at least on the JetBrains Runtime).
        // The AwtFontManager will transparently rewrite the ".AppleSystemUIFont" alias
        // to the "System Font" one that Skia knows.
        //
        // Note that the AWT default font on macOS is not SF, but rather Helvetica Neue.
        if (hostOs == OS.MacOS) {
            fontManager.matchFamily(FontFamilyKey.Apple.SystemFont.familyName)
                .use {
                    if (it.count() > 0) {
                        familyNamesCache.add(FontFamilyKey(FontFamilyKey.Apple.SystemFont.familyName))
                        familyNames += FontFamilyKey.Apple.SystemFont.familyName

                        familyNamesCache.add(FontFamilyKey(FontFamilyKey.Apple.AppleSystemUiFont.familyName))
                        familyNames += FontFamilyKey.Apple.AppleSystemUiFont.familyName
                    }
                }
        }

        // Refresh our cache of system font family names
        // (we pre-compute it to save us time later, since there can be many fonts)
        _familyNames.clear()
        _familyNames.addAll(familyNames)
    }

    /**
     * AWT logical fonts are found in [FontFamilyKey.Awt.awtLogicalFonts];
     * these fonts are just aliases for other, physical system fonts.
     */
    private fun cacheAwtLogicalFonts() {
        try {
            for (logicalFont in FontFamilyKey.Awt.awtLogicalFonts) {
                val physicalFontFamilyName = AwtFontUtils.resolvePhysicalFontNameOrNull(logicalFont.familyName)
                    ?: continue

                awtLogicalFamilyNames += logicalFont to physicalFontFamilyName
            }
        } catch (ignored: Throwable) {
        }
    }

    /**
     * Suspend execution until the font family names caching has
     * been completed.
     */
    suspend fun ensureSystemFontsCached() {
        if (!allFontsCachedImpl) {
            waitChannel.receive()
        }
    }

    override suspend fun familyNames(): Set<String> {
        ensureSystemFontsCached()
        return _familyNames
    }

    override suspend fun contains(familyName: String): Boolean {
        val key = FontFamilyKey(familyName)
        return contains(key)
    }

    override suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        val key = FontFamilyKey(familyName)

        if (isAppleSystemFont(key)) {
            // Rewrite requests for ".AppleSystemUIFont" on macOS to "System font".
            // They are the same, hidden San Francisco font, but we need to do this
            // for AWT compatibility reasons.
            return FontMgr.default.matchFamilyStyle(FontFamilyKey.Apple.SystemFont.familyName, fontStyle)
        }

        if (isAwtLogicalFont(key)) {
            val physicalFontName = awtLogicalFamilyNames[key]
            return FontMgr.default.matchFamilyStyle(physicalFontName, fontStyle)
        }

        if (!familyNamesCache.contains(key)) return null

        return FontMgr.default.matchFamilyStyle(familyName, fontStyle)
    }

    private fun isAwtLogicalFont(key: FontFamilyKey) =
        awtLogicalFamilyNames.containsKey(key)

    override suspend fun getFontFamilyOrNull(familyName: String): FontFamily? {
        val key = FontFamilyKey(familyName)

        if (isAppleSystemFont(key)) {
            // Rewrite requests for ".AppleSystemUIFont" on macOS to "System font".
            // They are the same, hidden San Francisco font, but we need to do this
            // for AWT compatibility reasons.
            return FontMgr.default.matchFamily(FontFamilyKey.Apple.SystemFont.familyName)
                .use { it.toFontFamilyOrNull(familyName) }
        }

        if (!familyNamesCache.contains(key)) return null

        return FontMgr.default.matchFamily(familyName)
            .use { it.toFontFamilyOrNull(familyName) }
    }

    private fun FontStyleSet.toFontFamilyOrNull(familyName: String): FontFamily? {
        if (count() < 1) return null

        return FontFamily.fromTypefaces(familyName).apply {
            for (i in 0 until count()) {
                addTypeface(getTypeface(i)!!)
            }
        }
    }

    private fun isAppleSystemFont(key: FontFamilyKey): Boolean {
        if (hostOs != OS.MacOS) return false
        return key in FontFamilyKey.Apple.hiddenSystemFontNames
    }

    /**
     * Check if the given [key] exists in the system.
     *
     * This function will suspend until the caching is complete.
     *
     * @see invalidate
     * @see familyNames
     */
    suspend fun contains(key: FontFamilyKey): Boolean {
        ensureSystemFontsCached()
        return familyNamesCache.contains(key)
    }
}
