package org.jetbrains.skiko

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Library {
    private val skikoLibraryPath = System.getProperty("skiko.library.path")
    private val cacheRoot = "${System.getProperty("user.home")}/.skiko/"

    // Same native library cannot be loaded in several classloaders, so we have to clone
    // native library to allow Skiko loading to work properly in complex cases, i.e.
    // several IDEA plugins.
    private fun loadLibraryOrCopy(library: File) {
        try {
            System.load(library.absolutePath)
        } catch (e: UnsatisfiedLinkError) {
            if (e.message?.contains("already loaded in another classloader") == true) {
                val tempFile = File.createTempFile("skiko", "")
                Files.copy(library.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                tempFile.deleteOnExit()
                System.load(tempFile.absolutePath)
            } else {
                throw e
            }
        }
    }

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
            loadLibraryOrCopy(file)
        }
    }

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. Also, it could load additional
    // localization resource on platforms wher it is needed.
    @Synchronized
    fun load() {
        val name = "skiko-$hostId"
        val platformName = System.mapLibraryName(name)

        if (skikoLibraryPath != null) {
            val library = File(File(skikoLibraryPath), platformName)
            loadLibraryOrCopy(library)
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
    }
}

// We have to keep this tiny class in Skiko for testing purposes.
internal class LibraryTestImpl() {
    fun run(): Long {
        val bitmap = org.jetbrains.skija.Bitmap()
        return bitmap._ptr
    }
}