package org.jetbrains.skiko

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

// based on https://github.com/JetBrains/skija/blob/bc6e0531021e4ff839c07196e0336d6456ed7520/src/main/java/org/jetbrains/skija/Library.java
object Library {
    private var loaded = false

    // https://github.com/adamheinrich/native-utils/blob/e6a39489662846a77504634b6fafa4995ede3b1d/src/main/java/cz/adamh/utils/NativeUtils.java
    @Synchronized
    fun load(resourcePath: String, name: String) {
        if (loaded) return

        val libFileName = "$libPrefix$name$libExtension"
        val url = Library::class.java.getResource(resourcePath + libFileName)
        // println("Loading " + url);
        val libFile = when {
            url == null -> error("Library $libFileName is not found in $resourcePath")
            url.protocol == "file" -> File(url.toURI())
            else -> url.openStream().use { input ->
                val tempRoot = File(System.getProperty("java.io.tmpdir"))
                val tempDir = tempRoot.resolve("skija_" + System.nanoTime()).apply { mkdirs() }
                File(tempDir, libFileName).also {
                    it.deleteOnExit()
                    Files.copy(input, it.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        System.load(libFile.absolutePath)
        loaded = true
    }

    private val libPrefix: String
        get() = if (currentOS == OS.Windows) "" else "lib"

    private val libExtension: String
        get() = when (currentOS) {
            OS.Mac -> ".dylib"
            OS.Linux -> ".so"
            OS.Windows -> ".dll"
        }

    private val currentOS by lazy {
        val osName = System.getProperty("os.name") ?: error("Unknown OS: 'os.name' is null")
        val os = osName.toLowerCase(Locale.ROOT)
        when {
            os.contains("mac") || os.contains("darwin") -> OS.Mac
            os.contains("win") -> OS.Windows
            os.contains("nux") -> OS.Linux
            else -> error("Unknown OS: $osName")
        }
    }

    private enum class OS {
        Linux, Mac, Windows
    }
}