package org.jetbrains.skiko

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Library {
    private var loaded = false

    private val skikoLibraryPath = System.getProperty("skiko.library.path")
    private val cacheRoot = "${System.getProperty("user.home")}/.skiko/"

    private fun loadOrGet(cacheDir: File, path: String, resourceName: String, isLibrary: Boolean) {
        val file = File(cacheDir, resourceName)
        if (!file.exists()) {
            val tempFile = File.createTempFile("skiko", "", cacheDir)
            Library::class.java.getResourceAsStream("$path$resourceName").use { input ->
                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }
        if (isLibrary) {
            System.load(file.absolutePath)
        }
    }

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. Also, it could load additional
    // localization resource on platforms wher it is needed.
    @Synchronized
    fun load() {
        if (loaded) return

        val name = "skiko-$hostId"
        val platformName = System.mapLibraryName(name)

        if (skikoLibraryPath != null) {
            val library = File(File(skikoLibraryPath), platformName)
            System.load(library.absolutePath)
        } else {
            val resourcePath = "/"
            val hashResourceStream = Library::class.java.getResourceAsStream(
                "$resourcePath$platformName.sha256"
            ) ?: throw LibraryLoadException(
                "Cannot find $platformName.sha256, proper native dependency missing."
            )
            val hash = hashResourceStream.use {
                BufferedReader(InputStreamReader(it)).lines().toArray()[0] as String
            }
            val cacheDir = File(File(cacheRoot), hash)
            cacheDir.mkdirs()
            loadOrGet(cacheDir, resourcePath, platformName, true)
            val loadIcu = hostOs.isWindows
            if (loadIcu) {
                loadOrGet(cacheDir, resourcePath, "icudtl.dat", false)
            }
        }

        // TODO move properties to SkikoProperties
        Setup.init(
            System.getProperty("skiko.rendering.noerasebackground") != "false",
            System.getProperty("skiko.rendering.laf.global") == "true",
            System.getProperty("skiko.rendering.useScreenMenuBar") != "false"
        )

        try {
            // Init code executed after library was loaded.
            org.jetbrains.skija.impl.Library._nAfterLoad()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        loaded = true
    }


}
