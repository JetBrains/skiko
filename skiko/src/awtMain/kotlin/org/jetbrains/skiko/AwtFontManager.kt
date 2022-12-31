package org.jetbrains.skiko

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

object AwtFontManager {
    private val systemFontCache = ConcurrentHashMap<String, FontFamilyInfo>()
    private val customFontCache = ConcurrentHashMap<String, FontFamilyInfo>()

    @Volatile
    private var allFontsCachedImpl = false
    private val waitChannel = RendezvousBroadcastChannel<Unit>()
    private var cacheJob: Job? = null

    init {
        invalidateSystemFontsCache()
    }

    /**
     * Invalidate cache and start caching again. Maybe useful to re-read fonts
     * when changed.
     */
    @OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
    fun invalidateSystemFontsCache() {
        cacheJob?.cancel()

        allFontsCachedImpl = false
        cacheJob = GlobalScope.launch {
            cacheAllSystemFonts()
            allFontsCachedImpl = true
            waitChannel.sendAll(Unit)
        }
    }

    private suspend fun cacheAllSystemFonts() {
        systemFontCache.clear()

        val fontManager = FontMgr.default
        val typefacesByFamily = (0 until fontManager.familiesCount)
            .map { i -> fontManager.getFamilyName(i) }
            .let { it + "System font" }
            .associateWith { familyName -> fontManager.matchFamily(familyName).takeIf { it.count() > 0 } }
            .filterValues { it != null }
            .mapValues { (_, value) -> value as FontStyleSet }

        for ((fontFamily, typefaces) in typefacesByFamily) {
            systemFontCache[fontFamily.lowercase()] = FontFamilyInfo(typefaces)
            yield()
        }

        // Since macOS pretends the San Francisco font doesn't exist, we force-load it into
        // our cache, and store both as "System font" and as ".AppleSystemUIFont".
        // The latter is done for Swing/AWT interop reasons, since AWT loads it as
        // ".AppleSystemUIFont" (at least on the JetBrains Runtime).
        // Note that the AWT default font, on macOS, is not SF but rather Helvetica Neue.
        if (hostOs == OS.MacOS) {
            val systemFontStyleSet = fontManager.matchFamily("System font")
            if (systemFontStyleSet.count() > 0) {
                val fontFamilyInfo = FontFamilyInfo(systemFontStyleSet)
                systemFontCache["system font"] = fontFamilyInfo
                systemFontCache[".applesystemuifont"] = fontFamilyInfo
            }
        }
    }

    /**
     * Add a resource entry as a custom font known to this font manager.
     *
     * Note that custom fonts aren't impacted by calls to [invalidateSystemFontsCache].
     *
     * @see removeCustomFont
     */
    suspend fun addResourceFont(resource: String, loader: ClassLoader = Thread.currentThread().contextClassLoader) {
        val res = loader.getResourceAsStream(resource)
            ?: ClassLoader.getSystemResourceAsStream(resource)
            ?: error("Unable to access the resources from the provided classloader")

        val resourceBytes = withContext(Dispatchers.IO) {
            res.readAllBytes()
        }
        val typeface = Typeface.makeFromData(Data.makeFromBytes(resourceBytes))
        val existingTypefaces = customFontCache[typeface.familyName]?.availableTypefaces ?: emptySet()
        val fontFamilyInfo = FontFamilyInfo.fromTypefaces(*existingTypefaces.toTypedArray(), typeface)

        customFontCache[typeface.familyName.lowercase()] = fontFamilyInfo   // TODO check if system already has it!
    }

    /**
     * Add a file as a custom font known to this font manager.
     *
     * Note that custom fonts aren't impacted by calls to [invalidateSystemFontsCache].
     *
     * @see removeCustomFont
     */
    fun addFontFile(file: File) {
        val typeface = Typeface.makeFromFile(file.absolutePath)
        val existingTypefaces = customFontCache[typeface.familyName]?.availableTypefaces ?: emptySet()
        val fontFamilyInfo = FontFamilyInfo.fromTypefaces(*existingTypefaces.toTypedArray(), typeface)

        customFontCache[typeface.familyName.lowercase()] = fontFamilyInfo
    }

    /**
     * Remove a custom font by family name, if it exists.
     *
     * @see addFontFile
     * @see addResourceFont
     */
    fun removeCustomFont(familyName: String) {
        customFontCache -= familyName.lowercase()
    }

    /**
     * Gets a typeface from the cache, if it exists.
     *
     * Custom fonts have the precedence over system fonts.
     */
    suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        ensureAllFontsCached()

        val fontFamily = customFontCache[familyName.lowercase()]
            ?: systemFontCache[familyName.lowercase()]
            ?: return null
        return fontFamily[fontStyle]
    }

    private suspend fun ensureAllFontsCached() {
        if (!allFontsCachedImpl) {
            waitChannel.receive()
        }
    }

    val availableFontFamilies
        get() = systemFontCache.keys + customFontCache.keys
}
