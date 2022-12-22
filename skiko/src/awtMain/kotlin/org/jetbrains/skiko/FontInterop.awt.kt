package org.jetbrains.skiko

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object AwtFontManager {
    private var fontCache = ConcurrentHashMap<String, FontFamily>()

    @Volatile
    private var allFontsCachedImpl = false
    private val waitChannel = RendezvousBroadcastChannel<Unit>()
    private var cacheJob: Job? = null

    private var cachingDoneLatch: LoggingLatch? = null

    class LoggingLatch : CountDownLatch(1) {
        override fun countDown() {
            super.countDown()
            println("Countdown! Count is now $count")
        }

        override fun await() {
            super.await()
            println("Await! Count is now $count")
        }
    }

    init {
        invalidate()
    }

    /**
     * Invalidate cache and start caching again. Maybe useful to re-read fonts
     * when changed.
     */
    @OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
    fun invalidate() {
//        cacheJob?.cancel()
        cachingDoneLatch?.countDown()
        cachingDoneLatch = LoggingLatch()

        allFontsCachedImpl = false
        GlobalScope.launch {
            println("Starting font caching...")
            val elapsed = measureTime { cacheAllFonts() }
            println("Font caching DONE. Took: $elapsed")
            allFontsCachedImpl = true
//            waitChannel.sendAll(Unit)
            cachingDoneLatch!!.countDown()
        }
    }

    private fun cacheAllFonts() {
        fontCache.clear()

        val fontManager = FontMgr.default
        println(" Got fontMgr")
        val typefacesByFamily = (0 until fontManager.familiesCount)
            .map { i -> fontManager.getFamilyName(i) }
            .let { it + "System font" }
            .associateWith { familyName -> fontManager.matchFamily(familyName).takeIf { it.count() > 0 } }
            .filterValues { it != null }
            .mapValues { (_, value) -> value as FontStyleSet }

        println(" Got typefaces")
        for ((fontFamily, typefaces) in typefacesByFamily) {
            fontCache[fontFamily] = FontFamily(typefaces)
//            yield()
        }

        println(" Font cache filled")
    }

    /**
     * Add custom resource entry as a font known to this resource manager.
     *
     * @return true, if font was found and identified, and false otherwise
     */
    fun addResourceFont(resource: String, loader: ClassLoader = Thread.currentThread().contextClassLoader) {
        ensureAllFontsCached()

        val res = loader.getResourceAsStream(resource)
            ?: ClassLoader.getSystemResourceAsStream(resource)
            ?: error("Unable to access the resources from the provided classloader")

        println("Got resources")
        val resourceBytes = res.readAllBytes()
        println("Read resource")
        val typeface = Typeface.makeFromData(Data.makeFromBytes(resourceBytes))
        val existingTypefaces = fontCache[typeface.familyName]?.availableTypefaces ?: emptySet()
        val fontFamily = FontFamily.fromTypefaces(*existingTypefaces.toTypedArray(), typeface)

        fontCache[typeface.familyName] = fontFamily
    }

    suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        ensureAllFontsCached()

        val fontFamily = fontCache[familyName] ?: return null
        return fontFamily[fontStyle]
    }

    private fun ensureAllFontsCached() {
        if (!allFontsCachedImpl) {
            println("Waiting for fonts to be cached...")
//            waitChannel.receive()
            cachingDoneLatch!!.await()
            println("Fonts are now cached")
        }
    }

    val availableFontFamilies
        get() = fontCache.keys
}
