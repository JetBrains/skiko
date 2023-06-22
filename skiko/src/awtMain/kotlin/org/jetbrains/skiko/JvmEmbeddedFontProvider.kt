package org.jetbrains.skiko

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skiko.AwtFontUtils.fontFamilyName
import org.jetbrains.skiko.AwtFontUtils.fontFileName
import org.jetbrains.skiko.context.isRunningOnJetBrainsRuntime
import java.awt.GraphicsEnvironment
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal object JvmEmbeddedFontProvider : FontProvider {

    private val embeddedFamilyMap: MutableMap<String, String> = ConcurrentHashMap()
    private val embeddedFamilies: MutableMap<FontFamilyKey, FontFamily> = ConcurrentHashMap()
    private val embeddedFontFiles: MutableMap<Typeface, String> = ConcurrentHashMap()
    private val _familyNames: MutableSet<String> = ConcurrentHashMap.newKeySet()

    @Volatile
    private var allFontsCachedImpl = false
    private val waitChannel = RendezvousBroadcastChannel<Unit>()
    private var cacheJob: Job? = null

    private val FontManagerFactoryClass = Class.forName("sun.font.FontManagerFactory")
    private val SunFontManagerClass = Class.forName("sun.font.SunFontManager")

    // FontManagerFactory methods
    private val FontManagerFactory_getInstanceMethod =
        ReflectionUtil.getDeclaredMethodOrNull(FontManagerFactoryClass, "getInstance")

    // SunFontManagerFactory fields
    private val SunFontManagerFactory_jreBundledFontFiles =
        ReflectionUtil.findAssignableField(SunFontManagerClass, HashSet::class.java, "jreBundledFontFiles")
    private val SunFontManagerFactory_jreFamilyMap =
        ReflectionUtil.findAssignableField(SunFontManagerClass, HashMap::class.java, "jreFamilyMap")

    private val javaHomePath
        get() = Path(System.getProperty("java.home"))
    private val jbrEmbeddedFontsPath
        get() = javaHomePath.resolve("lib").resolve("fonts")

    init {
        invalidate()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun invalidate() {
        cacheJob?.cancel()

        allFontsCachedImpl = false
        cacheJob = GlobalScope.launch {
            cacheEmbeddedFonts()
            allFontsCachedImpl = true
            waitChannel.sendAll(Unit)
        }
    }

    private fun cacheEmbeddedFonts() {
        embeddedFamilies.clear()
        embeddedFontFiles.clear()
        _familyNames.clear()

        if (canUseJetBrainsRuntimeFeatures) {
            cacheJetBrainsRuntimeEmbeddedFonts()
            cacheJetBrainsRuntimeEmbeddedFamilyMap()
        } else {
            tryCachingNonJbrEmbeddedFonts()
        }
    }

    private fun cacheJetBrainsRuntimeEmbeddedFonts() {
        val field =
            checkNotNull(SunFontManagerFactory_jreBundledFontFiles) { "JetBrains Runtime SunFontManager fields not accessible" }

        try {
            field.isAccessible = true

            val fontManager = sunFontManager()

            @Suppress("UNCHECKED_CAST")
            val embeddedFontFileNames = field.get(fontManager) as HashSet<String>
            val embeddedFontPaths = embeddedFontFileNames.map { jbrEmbeddedFontsPath.resolve(it) }
                .sortedBy { it.absolutePathString() }
                .distinctBy { it.absolutePathString() }

            embeddedFontPaths.asSequence()
                .map { path ->
                    val absolutePath = path.absolutePathString()
                    val typeface = Typeface.makeFromFile(absolutePath)
                    TypefaceWithPath(typeface, absolutePath)
                }
                .distinctBy { it.path }
                .groupBy { it.typeface.familyName }
                .forEach { (familyName, typefacesWithPath) ->
                    val typefaces = typefacesWithPath
                        .distinctBy { it.typeface.fontStyle }
                        .onEach { embeddedFontFiles += it.typeface to it.path }
                        .map { it.typeface }

                    val fontFamily = FontFamily.fromTypefaces(
                        familyName = familyName,
                        source = FontFamily.FontFamilySource.JvmEmbedded,
                        typefaces = typefaces.toTypedArray()
                    )

                    val key = FontFamilyKey(familyName)
                    embeddedFamilies += key to fontFamily
                    _familyNames += familyName
                }
        } finally {
            field.isAccessible = false
        }
    }

    private fun cacheJetBrainsRuntimeEmbeddedFamilyMap() {
        val field =
            checkNotNull(SunFontManagerFactory_jreFamilyMap) { "JetBrains Runtime SunFontManager fields not accessible" }

        try {
            field.isAccessible = true

            val fontManager = sunFontManager()

            @Suppress("UNCHECKED_CAST")
            val map = field.get(fontManager) as HashMap<String, String>
            for ((rawFamilyName, readableFamilyName) in map) {
                embeddedFamilyMap += rawFamilyName to readableFamilyName
            }
        } finally {
            field.isAccessible = false
        }
    }

    private fun tryCachingNonJbrEmbeddedFonts() {
        GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.asSequence()
            .map { font -> font.fontFileName to font }
            .filter { (fileName, _) ->
                // Only take physical fonts that live in the JVM folder
                fileName != null && javaHomePath.isParentOf(Path(fileName))
            }
            .groupBy { (_, font) -> font.fontFamilyName ?: font.family }
            .forEach { (familyName, pathAndFonts) ->
                val typefacesWithPath =
                    pathAndFonts.mapNotNull { (path, _) -> path }
                        .map { path -> TypefaceWithPath(Typeface.makeFromFile(path), path) }

                val typefaces = typefacesWithPath
                    .onEach { embeddedFontFiles += it.typeface to it.path }
                    .map { it.typeface }

                val fontFamily = FontFamily.fromTypefaces(
                    familyName = familyName,
                    source = FontFamily.FontFamilySource.JvmEmbedded,
                    typefaces = typefaces.toTypedArray()
                )
                embeddedFamilies += FontFamilyKey(familyName) to fontFamily
                _familyNames += familyName
            }
    }

    /**
     * Suspend execution until the font family names caching has
     * been completed.
     */
    private suspend fun ensureEmbeddedFontsCached() {
        if (!allFontsCachedImpl) {
            waitChannel.receive()
        }
    }

    override suspend fun familyNames(): Set<String> {
        ensureEmbeddedFontsCached()
        return _familyNames
    }

    suspend fun embeddedFontFilePaths(): Set<String> {
        ensureEmbeddedFontsCached()
        return embeddedFontFiles.values.toSet()
    }

    suspend fun embeddedFontFamilyMap(): Map<String, String> {
        ensureEmbeddedFontsCached()
        return embeddedFamilyMap
    }

    override suspend fun contains(familyName: String): Boolean {
        ensureEmbeddedFontsCached()
        return _familyNames.contains(familyName)
    }

    override suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        val fontFamily = getFontFamilyOrNull(familyName) ?: return null

        return fontFamily[fontStyle]
            ?: FontFamily.closestStyle(fontFamily.availableStyles, fontStyle)?.let(fontFamily::get)
    }

    override suspend fun getFontFamilyOrNull(familyName: String): FontFamily? {
        ensureEmbeddedFontsCached()

        val key = FontFamilyKey(familyName)
        return embeddedFamilies[key]
    }

    val canUseJetBrainsRuntimeFeatures: Boolean
        get() = isRunningOnJetBrainsRuntime() && SunFontManagerFactory_jreBundledFontFiles != null

    private fun sunFontManager() =
        checkNotNull(FontManagerFactory_getInstanceMethod) { "FontManagerFactory#getInstanceMethod() not available" }
            .invoke(null)
            .also { check(SunFontManagerClass.isAssignableFrom(it.javaClass)) { "FontManager is not an instance of SunFontManager" } }

    internal data class TypefaceWithPath(val typeface: Typeface, val path: String)
}
