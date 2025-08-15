package org.jetbrains.skiko

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class LibraryLoader(
    /**
     * Short library name without platform suffix and extension. For example "skiko" or "skiko-angle-libEGL"
     */
    private val name: String,

    /**
     * Additional file to check or unpack after loading the library. For example, "icudtl.dat".
     *
     * Currently only one file is supported, but it can be extended to support multiple ones.
     */
    private val additionalFile: String? = null,

    /**
     * Additional code that is called after successfully loading
     */
    private val init: () -> Unit = {}
) {
    // A native library cannot be loaded in several classloaders, so we have to clone
    // the native library to allow Skiko loading to work properly in complex cases, i.e.,
    // several IDEA plugins.
    private fun loadLibraryOrCopy(library: File): File? {
        try {
            System.load(library.absolutePath)
            return null
        } catch (e: UnsatisfiedLinkError) {
            if (e.message?.contains("already loaded in another classloader") == true) {
                val copyDir = Files.createTempDirectory("skiko").toFile()
                val tempFile = copyDir.resolve(library.name)
                Files.copy(library.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                tempFile.deleteOnExit()
                System.load(tempFile.absolutePath)
                return copyDir
            } else {
                throw LibraryLoadException("Failed to loade library $library", cause = e)
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

    private var isLoaded = false

    /**
     * Load a native library finding it in multiple sources:
     * - from SkikoProperties.libraryPath
     * - java.home
     * - jar resources
     *
     * @throws LibraryLoadException if library wasn't loaded successfully.
     *         Calling this function again retries the loading.
     */
    @Synchronized
    fun loadOnce() {
        if (!isLoaded) {
            findAndLoadLibrary(name, additionalFile)
            init()
            isLoaded = true
        }
    }

    private fun findAndLoadLibrary(name: String, additionalFile: String? = null) {
        val platformName = System.mapLibraryName(name)

        if (hostOs == OS.Android) {
            System.loadLibrary(name)
            return
        }

        // First try: system property is set.
        val skikoLibraryPath = SkikoProperties.libraryPath
        if (skikoLibraryPath != null) {
            val library = File(File(skikoLibraryPath), platformName)
            val copyDir = loadLibraryOrCopy(library)
            if (additionalFile != null && copyDir != null)
                unpackIfNeeded(copyDir, additionalFile, true)
            return
        }

        // Second try: load it from the bin/ or lib/ directory relative to the JVM home.
        // The user might have placed the native files alongside the other JVM libraries
        // for signing purposes, so we'll find it here if so.
        val jvmFiles = File(System.getProperty("java.home"), if (hostOs.isWindows) "bin" else "lib")
        val pathInJvm = jvmFiles.resolve(platformName)
        if (pathInJvm.exists() && additionalFile?.let { (jvmFiles.resolve(it)).exists() } != false) {
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

        val dataDir = File(File(SkikoProperties.dataPath), hash)
        dataDir.mkdirs()
        val library = unpackIfNeeded(dataDir, platformName, false)
        val copyDir = loadLibraryOrCopy(library)
        if (additionalFile != null) {
            if (copyDir != null) {
                // We made a duplicate to resolve classloader conflicts.
                unpackIfNeeded(copyDir, additionalFile, true)
            } else {
                // Normal path where Skiko is loaded only once.
                unpackIfNeeded(dataDir, additionalFile, false)
            }
        }
    }
}
