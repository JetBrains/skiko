package org.jetbrains.skiko

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// based on https://github.com/JetBrains/skija/blob/bc6e0531021e4ff839c07196e0336d6456ed7520/src/main/java/org/jetbrains/skija/Library.java
object Library {
    private var loaded = false

    // https://github.com/adamheinrich/native-utils/blob/e6a39489662846a77504634b6fafa4995ede3b1d/src/main/java/cz/adamh/utils/NativeUtils.java
    @Synchronized
    fun load(resourcePath: String, name: String) {
        if (loaded) return

        val libFileName = System.mapLibraryName(name)
        val url = Library::class.java.getResource(resourcePath + libFileName)

        val tempRoot = File(System.getProperty("java.io.tmpdir"))
        val tempDir = tempRoot.resolve("skiko_" + System.nanoTime()).apply { mkdirs() }
        val libFile = when {
            url == null -> error("Library $libFileName is not found in $resourcePath")
            url.protocol == "file" -> File(url.toURI())
            else -> url.openStream().use { input ->
                File(tempDir, libFileName).also {
                    it.deleteOnExit()
                    Files.copy(input, it.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        val loadIcu = System.getProperty("os.name").toLowerCase().startsWith("win")
        val icuData = "icudtl.dat"
        if (loadIcu) {
            val icuUrl = Library::class.java.getResource(resourcePath + icuData)
            icuUrl.openStream().use { input ->
                File(tempDir, icuData).also {
                    it.deleteOnExit()
                    Files.copy(input, it.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        System.load(libFile.absolutePath)
        loaded = true

        // we have to set this property to avoid render flickering.
        System.setProperty("sun.awt.noerasebackground", "true")
    }
}