package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

object Library {
    internal const val SKIKO_LIBRARY_PATH_PROPERTY = "skiko.library.path"
    internal val cacheRoot = "${System.getProperty("user.home")}/.skiko/"
    private val skikoLibraryPath = System.getProperty(SKIKO_LIBRARY_PATH_PROPERTY)
    private var copyDir: File? = null

    // Same native library cannot be loaded in several classloaders, so we have to clone
    // native library to allow Skiko loading to work properly in complex cases, i.e.
    // several IDEA plugins.
    private fun loadLibraryOrCopy(library: File) {
        try {
            System.load(library.absolutePath)
        } catch (e: UnsatisfiedLinkError) {
            if (e.message?.contains("already loaded in another classloader") == true) {
                copyDir = Files.createTempDirectory("skiko").toFile()
                val tempFile = copyDir!!.resolve(library.name)
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
    // localization resource on platforms where it is needed.
    @Synchronized
    fun load() {
        if (loaded.compareAndExchange(false, true)) return

        // Find/unpack a usable copy of the native library.
        findAndLoad()

        // TODO move properties to SkikoProperties
        Setup.init()

        try {
            // Init code executed after library was loaded.
            org.jetbrains.skia.impl.Library._nAfterLoad()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun findAndLoad() {
        val name = "skiko-$hostId"
        val platformName = System.mapLibraryName(name)
        val icu = if (hostOs.isWindows) "icudtl.dat" else null

        // First try: system property is set.
        if (skikoLibraryPath != null) {
            val library = File(File(skikoLibraryPath), platformName)
            loadLibraryOrCopy(library)
            if (icu != null && copyDir != null)
                unpackIfNeeded(copyDir!!, icu, true)
            return
        }

        // Second try: load it from the bin/ or lib/ directory relative to the JVM home.
        // The user might have placed the native files alongside the other JVM libraries
        // for signing purposes, so we'll find it here if so.
        val jvmFiles = File(System.getProperty("java.home"), if (hostOs.isWindows) "bin" else "lib")
        val pathInJvm = jvmFiles.resolve(platformName)
        if (pathInJvm.exists() && icu?.let { (jvmFiles.resolve(it)).exists() } != false) {
            loadLibraryOrCopy(pathInJvm)
            return
        }

        // Third try: look up in or extract to a local cache directory.
        // Key the cache by the hash of the library.
        val hashResourceStream = Library::class.java.getResourceAsStream(
            "/$platformName.sha256"
        ) ?: throw LibraryLoadException(
            "Cannot find $platformName.sha256, proper native dependency missing."
        )
        val hash = hashResourceStream.use { it.bufferedReader().readLine() }

        val cacheDir = File(File(cacheRoot), hash)
        cacheDir.mkdirs()
        val library = unpackIfNeeded(cacheDir, platformName, false)
        loadLibraryOrCopy(library)
        if (icu != null) {
            if (copyDir != null) {
                // We made a duplicate to resolve classloader conflicts.
                unpackIfNeeded(copyDir!!, icu, true)
            } else {
                // Normal path where Skiko is loaded only once.
                unpackIfNeeded(cacheDir, icu, false)
            }
        }
    }
}

// We have to keep this tiny class in Skiko for testing purposes.
internal class LibraryTestImpl() {
    fun run(): Long {
        val bitmap = Bitmap()
        return bitmap._ptr
    }
}
