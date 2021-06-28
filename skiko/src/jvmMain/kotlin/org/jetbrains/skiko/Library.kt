package org.jetbrains.skiko

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

object Library {
    private val skikoLibraryPath = System.getProperty("skiko.library.path")
    private val cacheRoot = "${System.getProperty("user.home")}/.skiko/"
    private var copyDir: File? = null

    // Same native library cannot be loaded in several classloaders, so we have to clone
    // native library to allow Skiko loading to work properly in complex cases, i.e.
    // several IDEA plugins.
    private fun loadLibraryOrCopy(library: File) {
        try {
            System.load(library.absolutePath)
        } catch (e: UnsatisfiedLinkError) {
            if (e.message?.contains("already loaded in another classloader") == true) {
                val tempFile = File.createTempFile("skiko", if (hostOs.isWindows) ".dll" else "")
                copyDir = tempFile.parentFile
                Files.copy(library.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                tempFile.deleteOnExit()
                System.load(tempFile.absolutePath)
            } else {
                throw e
            }
        }
    }

    private fun unpackIfNeeded(dest: File, resourceName: String, deleteOnExit: Boolean): File {
        val file = File(dest, resourceName)
        if (!file.exists()) {
            val tempFile = File.createTempFile("skiko", "", dest)
            if (deleteOnExit)
                file.deleteOnExit()
            Library::class.java.getResourceAsStream("/$resourceName").use { input ->
                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }
        return file
    }

    private var loaded = AtomicBoolean(false)

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. Also, it could load additional
    // localization resource on platforms wher it is needed.
    @Synchronized
    fun load() {
        if (loaded.compareAndExchange(false, true)) return
        val name = "skiko-$hostId"
        val platformName = System.mapLibraryName(name)
        val icu = if (hostOs.isWindows) "icudtl.dat" else null

        if (skikoLibraryPath != null) {
            val library = File(File(skikoLibraryPath), platformName)
            loadLibraryOrCopy(library)
            if (icu != null && copyDir != null) {
                unpackIfNeeded(copyDir!!, icu, true)
            }
        } else {
            val hashResourceStream = Library::class.java.getResourceAsStream(
                "/$platformName.sha256"
            ) ?: throw LibraryLoadException(
                "Cannot find $platformName.sha256, proper native dependency missing."
            )
            val hash = hashResourceStream.use {
                BufferedReader(InputStreamReader(it)).lines().toArray()[0] as String
            }
            val cacheDir = File(File(cacheRoot), hash)
            cacheDir.mkdirs()
            val library = unpackIfNeeded(cacheDir, platformName, false)
            loadLibraryOrCopy(library)
            if (icu != null) {
                unpackIfNeeded(cacheDir, icu, false)
            }
        }

        // TODO move properties to SkikoProperties
        Setup.init()

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