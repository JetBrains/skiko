package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.io.bufferedReader
import kotlin.io.resolve
import kotlin.use

object Library {
    private var copyDir: File? = null

    // A native library cannot be loaded in several classloaders, so we have to clone
    // the native library to allow Skiko loading to work properly in complex cases, i.e.,
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
            withSikoDataDirectoryLock {
                if (file.exists()) return file
                val tempFile = File.createTempFile("skiko", "", dest)
                if (deleteOnExit)
                    file.deleteOnExit()
                Library::class.java.getResourceAsStream("/$resourceName").use { input ->
                    Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
            }
        }
        return file
    }

    /**
     * Holds a reference to the lock which has to be acquired when loading the native library,
     * or to wait for the loading to finish.
     * The reference will resolve to `null` if loading is done and callers do not need to wait anymore.
     */
    private val loadingLock = AtomicReference(ReentrantLock())

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. It could also load additional
    // localization resources, on platforms where it is needed.
    fun load() {
        /**
         * If there is no more loading lock available, then the loading has finished
         * and we can just return as normal, assuming that the library was successfully loaded.
         */
        val lock = loadingLock.get() ?: return

        /**
         * If the lock is held by the current thread, then this indicates a recursive call to load.
         * Methods like `_nAfterLoad()` might trigger additional Class loading, where .clinit (static init methods)
         * trigger further calls to Library.staticLoad() -> Library.load() while holding the current lock.
         *
         * It is fine, in such cases, to return eagerly and assume that the Library is successfully loaded and
         * the recursion is a result of callbacks indicating the successful load.
         */
        if (lock.isHeldByCurrentThread) return

        lock.withLock {
            // We entered the critical section, but another thread might have already entered and finished
            if (loadingLock.get() !== lock) return

            // Find/unpack a usable copy of the native library.
            findAndLoad()

            // TODO move properties to SkikoProperties
            Setup.init()

            try {
                // Init code executed after library was loaded.
                org.jetbrains.skia.impl.Library._nAfterLoad()
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                loadingLock.compareAndSet(lock, null)
            }
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

        // First try: system property is set.
        val skikoLibraryPath = SkikoProperties.libraryPath
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

        val dataDir = File(File(SkikoProperties.dataPath), hash)
        dataDir.mkdirs()
        dataDir.toPath().updateLastAccessTime()

        val library = unpackIfNeeded(dataDir, platformName, false)
        loadLibraryOrCopy(library)
        if (icu != null) {
            if (copyDir != null) {
                // We made a duplicate to resolve classloader conflicts.
                unpackIfNeeded(copyDir!!, icu, true)
            } else {
                // Normal path where Skiko is loaded only once.
                unpackIfNeeded(dataDir, icu, false)
            }
        }

        launchSkikoDataDirCleanupIfNecessary()
    }
}

// We have to keep this tiny class in Skiko for testing purposes.
internal class LibraryTestImpl() {
    fun run(): Long {
        val bitmap = Bitmap()
        return bitmap._ptr
    }
}
