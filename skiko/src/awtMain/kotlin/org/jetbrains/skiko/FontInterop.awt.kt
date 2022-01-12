package org.jetbrains.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    init {
        GlobalScope.launch(Dispatchers.IO) {
            cacheSystemFonts()
            allFontsCachedImpl = true
            waitChannel.sendAll(1)
        }
    }

    private fun systemFontsPaths(): Array<String> {
        return when (hostOs) {
            OS.Windows -> {
                val winPath = System.getenv("WINDIR")
                val localAppPath = System.getenv("LOCALAPPDATA")
                arrayOf(
                    "$winPath\\Fonts",
                    "$localAppPath\\Microsoft\\Windows\\Fonts"
                )
            }
            OS.MacOS -> {
                arrayOf(
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
                resultList.toTypedArray()
            }
            else -> {
                throw RuntimeException("Unknown OS: $hostOs")
            }
        }
    }

    private fun systemFontFiles(): List<File> {
        val extensions = arrayOf("ttf", "TTF")
        val paths = systemFontsPaths()
        val files = mutableListOf<File>()
        for (i in paths.indices) {
            val fontDirectory = File(paths[i])
            if (!fontDirectory.exists()) break
            fontDirectory.walk().filter { it.isFile && it.extension in extensions }.forEach {
                files.add(it)
            }
        }
        return files
    }

    private fun cacheSystemFonts() {
        val fontFiles = systemFontFiles()
        for (file in fontFiles) {
            try {
                if (!fontsMap.containsValue(file.absoluteFile)) {
                    val f = Font.createFont(Font.TRUETYPE_FONT, FileInputStream(file.absolutePath))
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
     * is already known to the font manager. This may change in the future.
     *
     * @param font - AWT font for which we need to know the path
     * @return - path to font, if known
     */
    fun findAvailableFontFile(font: Font): File? {
        return fontsMap[font.family]
    }

    /**
     * Show all fonts currently known to AWT font manager. As font indexing could take time,
     * may have not all elements.
     */
    fun listCurrentFontFiles(): List<File> {
        return fontsMap.values.toList()
    }

    /**
     * Show all fonts known to AWT font manager.
     */
    suspend fun listFontFiles(): List<File> {
        waitAllFontsCached()
        return fontsMap.values.toList()
    }


    /**
     * Find font file path from an AWT font.
     *  As font finding is long IO-intensive process, this operation may suspend for pretty long time..
     */
    suspend fun findFontFile(font: Font): File? {
        waitAllFontsCached()
        return fontsMap[font.family]
    }

    /**
     * If all AWT fonts were indexed.
     */
    val allFontsCached: Boolean
        get() = allFontsCachedImpl

    /**
     * Call continuation only when all AWT fonts are cached.
     */
    fun whenAllFontsCachedBlocking(continuation: () -> Unit) {
        // TODO: avoid busy loop
        while (!allFontsCachedImpl) {}
        continuation()
    }

    /**
     * Suspend until all AWT fonts were cached.
     */
    suspend fun waitAllFontsCached() {
        if (!allFontsCachedImpl) {
            waitChannel.receive()
        }
    }
}