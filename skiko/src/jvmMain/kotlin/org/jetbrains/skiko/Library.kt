package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

object Library {
    private var copyDir: File? = null

    internal data class LoadResult(val baseDir: File, val fromResourceCache: Boolean, val file: File)

    // A native library cannot be loaded in several classloaders, so we have to clone
    // the native library to allow Skiko loading to work properly in complex cases, i.e.,
    // several IDEA plugins.
    internal fun loadLibraryOrCopy(library: File) {
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

    internal fun unpackIfNeeded(dest: File, resourceName: String, deleteOnExit: Boolean): File {
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

    internal fun resourceFirstLineOrNull(path: String): String? =
        Library::class.java.getResourceAsStream(path)?.use { it.bufferedReader().readLine() }

    private var loaded = AtomicBoolean(false)

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. It could also load additional
    // localization resources, on platforms where it is needed.
    @Synchronized
    fun load() {
        if (!loaded.compareAndSet(false, true)) return

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

        if (hostOs == OS.Android) {
            System.loadLibrary("skiko-$hostId")
            return
        }

        // Reuse generic finder for the main library (do not load yet)
        val result = findAndLoadExact(platformName)

        // Load the found library now
        loadLibraryOrCopy(result.file)

        // Handle ICU on Windows similarly to the original logic
        if (icu != null) {
            if (copyDir != null) {
                // We made a duplicate to resolve classloader conflicts.
                unpackIfNeeded(copyDir!!, icu, true)
            } else if (result.fromResourceCache) {
                // Normal path where Skiko is loaded only once and was extracted to cache.
                unpackIfNeeded(result.baseDir, icu, false)
            } // else: library was loaded from JVM bin/lib or explicit path; do nothing.
        }
    }

    /**
     * Fully reusable variant of the above loader for an arbitrary exact file name (with extension),
     * using the same 3-step search strategy: skiko.library.path -> JVM bin/lib -> classpath resource cache.
     */
    internal fun findAndLoadExact(resourceFileName: String): LoadResult {
        // Android path not used for ANGLE; keep consistent with desktop approach only.
        // 1) System property path
        SkikoProperties.libraryPath?.let { path ->
            val file = File(File(path), resourceFileName)
            if (file.exists()) {
                return LoadResult(file.parentFile, false, file)
            }
        }
        // 2) JVM home bin/lib
        val jvmDir = File(System.getProperty("java.home"), if (hostOs.isWindows) "bin" else "lib")
        val jvmFile = jvmDir.resolve(resourceFileName)
        if (jvmFile.exists()) {
            return LoadResult(jvmDir, false, jvmFile)
        }
        // 3) Resource cache, keyed by checksum
        val hash = resourceFirstLineOrNull("/${resourceFileName}.sha256")
            ?: throw LibraryLoadException("Cannot find ${resourceFileName}.sha256, proper native dependency missing.")
        val dataDir = File(File(SkikoProperties.dataPath), hash)
        dataDir.mkdirs()
        val file = unpackIfNeeded(dataDir, resourceFileName, false)
        return LoadResult(dataDir, true, file)
    }
}

// We have to keep this tiny class in Skiko for testing purposes.
internal class LibraryTestImpl() {
    fun run(): Long {
        val bitmap = Bitmap()
        return bitmap._ptr
    }
}
