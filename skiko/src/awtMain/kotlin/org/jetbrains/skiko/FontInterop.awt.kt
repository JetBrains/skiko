package org.jetbrains.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Font
import java.awt.FontFormatException
import java.awt.GraphicsEnvironment
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

    private val systemFontNames: Array<String>
        get() = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames

    private val systemFontsPaths: Array<String> by lazy {
        when (hostOs) {
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

    private val systemFontFiles: List<File> by lazy {
        val extensions = arrayOf("ttf", "TTF")
        val paths = systemFontsPaths
        val files = mutableListOf<File>()
        for (i in paths.indices) {
            val fontDirectory = File(paths[i])
            if (!fontDirectory.exists()) break
            fontDirectory.walk().filter { it.isFile && it.extension in extensions }.forEach {
                files.add(it)
            }
        }
        files
    }

    private fun cacheSystemFonts() {
        val fontFiles = systemFontFiles
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

    fun findFontFile(font: Font): File? {
        return fontsMap[font.family]
    }

    val allFontsCached: Boolean
        get() = allFontsCachedImpl

    fun whenAllCachedBlocking(continuation: () -> Unit) {
        // TODO: avoid busy loop
        while (!allFontsCachedImpl) {}
        continuation()
    }

    suspend fun waitCached() {
        if (!allFontsCachedImpl) {
            waitChannel.receive()
        }
    }
}