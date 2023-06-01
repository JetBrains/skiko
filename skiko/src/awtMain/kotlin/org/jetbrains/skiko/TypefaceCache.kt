package org.jetbrains.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skiko.TypefaceCache.Companion.inMemory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * A generic typeface cache. An instance of the default in-memory
 * implementation can be created with the [inMemory] factory function.
 */
@InternalSkikoApi
interface TypefaceCache {

    /**
     * Add a classpath resource to this cache.
     *
     * [TypefaceCache]s don't have knowledge of entries added to other
     * instances.
     *
     * Don't forget to remove entries when you don't need them anymore.
     *
     * @see removeFontFamily
     */
    suspend fun addResource(
        resource: String,
        loader: ClassLoader = Thread.currentThread().contextClassLoader
    )

    /**
     * Add a [File] to this cache.
     *
     * [TypefaceCache]s don't have knowledge of entries added to other
     * instances.
     *
     * Don't forget to remove entries when you don't need them anymore.
     *
     * @see removeFontFamily
     */
    fun addFile(file: File)

    /**
     * Add a [Typeface] to this cache.
     * [TypefaceCache]s don't have knowledge of entries added to other
     * instances.
     *
     * Don't forget to remove entries when you don't need them anymore.
     *
     * @see removeFontFamily
     */
    fun addTypeface(typeface: Typeface)

    /**
     * Remove a custom font by family name, if it exists.
     *
     * This will not impact other instances of [TypefaceCache]; if a custom
     * font is registered on multiple instances, it needs to be removed from
     * all of them.
     *
     * @see addFile
     * @see addResource
     * @see addTypeface
     */
    fun removeFontFamily(familyName: String)

    /**
     * Remove all entries added to this instance.
     *
     * This will not impact other instances of [TypefaceCache]; if a custom
     * font is registered on multiple instances, it needs to be removed from
     * all of them.
     */
    fun clear()

    /**
     * Get a [Typeface] by family name and style, if it exists.
     *
     * Font family names are matched case-insensitively.
     */
    fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface?

    /**
     * Get a [FontFamily] by family name, if it exists.
     *
     * Font family names are matched case-insensitively.
     */
    fun getFontFamilyOrNull(familyName: String): FontFamily?

    /**
     * List the font families for all registered entries.
     */
    fun familyNames(): Set<String>

    val size: Int

    fun isEmpty() = size == 0

    companion object {

        /**
         * Create an instance of the default in-memory implementation.
         * The implementation is thread safe.
         */
        fun inMemory(): TypefaceCache = InMemoryTypefaceCache()
    }
}

private class InMemoryTypefaceCache private constructor(
    private val fontFamiliesCache: ConcurrentHashMap<FontFamilyKey, FontFamily>
) : TypefaceCache {

    constructor() : this(ConcurrentHashMap())

    private val familyNamesCache: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override val size: Int
        get() = fontFamiliesCache.size

    override suspend fun addResource(
        resource: String,
        loader: ClassLoader
    ) {
        val res = loader.getResourceAsStream(resource)
            ?: ClassLoader.getSystemResourceAsStream(resource)
            ?: error("Unable to access the resources from the provided classloader")

        val resourceBytes = withContext(Dispatchers.IO) {
            res.readAllBytes()
        }
        val typeface = Typeface.makeFromData(Data.makeFromBytes(resourceBytes))
        addTypeface(typeface)
    }

    override fun addFile(file: File) {
        val typeface = Typeface.makeFromFile(file.absolutePath)
        addTypeface(typeface)
    }

    override fun addTypeface(typeface: Typeface) {
        val key = FontFamilyKey(typeface.familyName)
        val fontFamily = FontFamily.fromTypefaces(typeface.familyName, FontFamily.FontFamilySource.Custom, typeface)
        fontFamiliesCache.getOrPut(key) { fontFamily }
        familyNamesCache += typeface.familyName
    }

    override fun removeFontFamily(familyName: String) {
        fontFamiliesCache -= FontFamilyKey(familyName)
        familyNamesCache -= familyName
    }

    override fun clear() {
        fontFamiliesCache.clear()
        familyNamesCache.clear()
    }

    override fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        val family = getFontFamilyOrNull(familyName)
            ?: return null

        return family[fontStyle]
    }

    override fun getFontFamilyOrNull(familyName: String) =
        fontFamiliesCache[FontFamilyKey(familyName)]

    override fun familyNames(): Set<String> = familyNamesCache
}
