package org.jetbrains.skiko

import kotlinx.coroutines.*
import java.awt.Font
import java.awt.FontFormatException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

object AwtFontManager {
    private var fontsMap = ConcurrentHashMap<String, File>()
    @Volatile
    private var allFontsCachedImpl = false
    private val waitChannel = RendezvousBroadcastChannel<Int>()
    private var customFontPaths = mutableListOf<String>()
    private var cacheJob: Job? = null

    init {
        invalidate()
    }

    private fun systemFontsPaths(): List<String> {
        return when (hostOs) {
            OS.Windows -> {
                val winPath = System.getenv("WINDIR")
                val localAppPath = System.getenv("LOCALAPPDATA")
                listOf(
                    "$winPath\\Fonts",
                    "$localAppPath\\Microsoft\\Windows\\Fonts"
                )
            }
            OS.MacOS -> {
                listOf(
                    System.getProperty("user.home") + File.separator + "Library/Fonts",
                    "/Library/Fonts",
                    "/System/Library/Fonts"
                )
            }
            OS.Linux -> {
                val pathsToCheck = arrayOf(
                    System.getProperty("user.home") + File.separator + ".fonts",
                    "/usr/share/fonts/truetype",
                    "/usr/share/fonts/TTF"
                )
                val resultList = ArrayList<String>()
                for (i in pathsToCheck.indices.reversed()) {
                    val path = pathsToCheck[i]
                    val tmp = File(path)
                    if (tmp.exists() && tmp.isDirectory && tmp.canRead()) {
                        resultList.add(path)
                    }
                }
                resultList
            }
            else -> {
                throw RuntimeException("Unknown OS: $hostOs")
            }
        }
    }

    private fun findFontFiles(paths: List<String>): List<File> {
        val files = mutableListOf<File>()
        paths.forEach { path ->
            val fontDirectory = File(path)
            if (fontDirectory.exists()) {
                fontDirectory.walk().filter { it.isFile && it.extension.lowercase() == "ttf" }.forEach {
                    files.add(it)
                }
            }
        }
        return files
    }

    private suspend fun cacheAllFonts() {
        val fontFiles = findFontFiles(customFontPaths) + findFontFiles(systemFontsPaths())
        for (file in fontFiles) {
            try {
                if (!fontsMap.containsValue(file.absoluteFile)) {
                    val f = FileInputStream(file.absolutePath).use {
                        Font.createFont(Font.TRUETYPE_FONT, it)
                    }
                    yield()
                    val name = f.family
                    fontsMap[name] = file.absoluteFile
                }
            } catch (e: FontFormatException) {
            } catch (e: IOException) {
            }
        }
    }

    /**
     * Find font file path from an AWT font.
     * As font finding is long IO-intensive process, this operation checks if given font
     * is already known to the font manager.
     *
     * If you want stable and predictable result it's better use [findFontFile] or check [allFontsCached].
     *
     * @param font - AWT font for which we need to know the path
     * @return path to font, if known
     */
    fun findAvailableFontFile(font: Font): File? {
        return fontsMap[font.family]
    }

    /**
     * Show all fonts currently known to AWT font manager. As font indexing could take time,
     * may have not all elements.
     *
     * If you want stable and predictable result it's better use [listFontFiles] or check [allFontsCached].
     *
     * @return list of currently known fonts
     */
    fun listAvailableFontFiles(): List<File> {
        return fontsMap.values.toList()
    }

    /**
     * Show all fonts known to AWT font manager.
     * @return list of known fonts
     */
    suspend fun listFontFiles(): List<File> {
        waitAllFontsCached()
        return fontsMap.values.toList()
    }


    /**
     * Find font file path from an AWT font.
     * As font finding is long IO-intensive process, this operation may suspend for pretty long time.
     * @param font - which AWT font to look for
     * @return path to the font file or null, if not found
     */
    suspend fun findFontFile(font: Font): File? {
        waitAllFontsCached()
        return fontsMap[font.family]
    }

    /**
     * Find font file path from the family name.
     * As font finding is long IO-intensive process, this operation may suspend for pretty long time.
     * @param family - which AWT font to look for
     * @return path to the font file or null, if not found
     */
    suspend fun findFontFamilyFile(family: String): File? {
        waitAllFontsCached()
        return fontsMap[family]
    }

    /**
     * Invalidate cache and start caching again. Maybe useful to re-read fonts
     * when changed.
     */
    fun invalidate() {
        cacheJob?.let {
            it.cancel()
        }
        allFontsCachedImpl = false
        cacheJob = GlobalScope.launch(Dispatchers.IO) {
            cacheAllFonts()
            allFontsCachedImpl = true
            waitChannel.sendAll(1)
        }
    }

    /**
     * Add custom directory to font search paths. Call [invalidate]
     * for operation to take effect.
     */
    fun addCustomPath(path: String) {
        customFontPaths += path
    }

    /**
     * If all AWT fonts were cached. Check this property before using non-suspend version
     * of font conversion APIs.
     */
    val allFontsCached: Boolean
        get() = allFontsCachedImpl

    /**
     * Call continuation only when all AWT fonts are cached.
     * Please avoid this API and prefer suspend operations.
     */
    fun whenAllFontsCachedBlocking(continuation: () -> Unit) {
        // TODO: avoid busy loop
        while (!allFontsCachedImpl) {}
        continuation()
    }

    /**
     * Suspend until all AWT fonts were cached.
     */
    private suspend fun waitAllFontsCached() {
        if (!allFontsCachedImpl) {
            waitChannel.receive()
        }
    }
}