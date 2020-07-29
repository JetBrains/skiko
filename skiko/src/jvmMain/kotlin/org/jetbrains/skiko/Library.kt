package org.jetbrains.skiko

import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// based on https://github.com/JetBrains/skija/blob/bc6e0531021e4ff839c07196e0336d6456ed7520/src/main/java/org/jetbrains/skija/Library.java
object Library {
    var _loaded = false

    // https://github.com/adamheinrich/native-utils/blob/e6a39489662846a77504634b6fafa4995ede3b1d/src/main/java/cz/adamh/utils/NativeUtils.java
    fun load(resourcePath: String, name: String) {
        if (_loaded) return
        try {
            var file: File
            val fileName = _getPrefix() + name + "." + _getExtension()
            val url = Library::class.java.getResource(resourcePath + fileName)
            // System.out.println("Loading " + url);
            requireNotNull(url) { "Library $fileName is not found in $resourcePath" }
            if (url.protocol === "file") file = try {
                // System.out.println("Loading " + url);
                File(url.toURI())
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            } else url.openStream().use { `is` ->
                val tempDir = File(System.getProperty("java.io.tmpdir"), "skija_" + System.nanoTime())
                tempDir.mkdirs()
                tempDir.deleteOnExit()
                file = File(tempDir, fileName)
                file.deleteOnExit()
                Files.copy(`is`, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            System.load(file.absolutePath)
            _loaded = true
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun _getPrefix(): String {
        val os = System.getProperty("os.name")
        val lowerCaseOs = os.toLowerCase()
        return if (lowerCaseOs.contains("windows")) "" else "lib"
    }

    fun _getExtension(): String {
        val os = System.getProperty("os.name") ?: throw RuntimeException("Unknown operation system")
        val lowerCaseOs = os.toLowerCase()
        if (lowerCaseOs.contains("mac") || lowerCaseOs.contains("darwin")) return "dylib"
        if (lowerCaseOs.contains("windows")) return "dll"
        if (lowerCaseOs.contains("nux")) return "so"
        throw RuntimeException("Unknown operation system: $os")
    }
}