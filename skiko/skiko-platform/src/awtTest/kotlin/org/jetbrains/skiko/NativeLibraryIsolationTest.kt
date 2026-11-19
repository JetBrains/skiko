package org.jetbrains.skiko

import java.io.File
import org.jetbrains.skiko.util.PlatformAndURLClassLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeLibraryIsolationTest {
    private val core = System.mapLibraryName("skiko-$hostId")
    private val platform = System.mapLibraryName("skiko-platform-$hostId")

    private fun isolatedLoader(): ClassLoader {
        val classPath = System.getProperty("java.class.path").split(File.pathSeparator) + System.getProperty("skiko.jar.path")
        return PlatformAndURLClassLoader(classPath.map { File(it).toURI().toURL() })
    }

    private fun ClassLoader.loadPlatformLibrary() {
        val type = Class.forName("org.jetbrains.skiko.PlatformLibrary", true, this)
        type.getMethod("load").invoke(type.getField("INSTANCE").get(null))
    }

    @Test
    fun `libraries cloned for a classloader stay together`() {
        val loaders = List(3) { isolatedLoader().apply { loadPlatformLibrary() } }
        val clones = loaders.mapNotNull {
            Class.forName("org.jetbrains.skiko.LibraryLoader", true, it)
                .getDeclaredField("copyDirectory").apply { isAccessible = true }.get(null) as File?
        }

        assertTrue(clones.size >= 2, "Only one loader can load the originals: $clones")
        assertEquals(clones.size, clones.toSet().size, "Loaders must not share a clone")
        for (directory in clones) {
            assertTrue(directory.resolve(core).isFile && directory.resolve(platform).isFile, "$directory")
        }
    }

    @Test
    fun `classloaders use different instances of the libraries`() {
        // The default font manager is a native singleton: its address differs between copies of the library.
        val singletons = List(2) { isolatedLoader().apply { loadPlatformLibrary() } }.map {
            val companion = Class.forName("org.jetbrains.skia.FontMgr", true, it).getField("Companion").get(null)
            companion.javaClass.getMethod("getDefault").invoke(companion).toString()
        }

        assertEquals(2, singletons.toSet().size, "Classloaders share a library instance: $singletons")
    }

    @Test
    fun `platform library uses the core library of its own classloader`() {
        // The first loader loads the original libraries, the others load clones.
        repeat(3) {
            val loader = isolatedLoader().apply { loadPlatformLibrary() }
            val probe = Class.forName("org.jetbrains.skiko.tests.CoreInstanceProbeKt", true, loader)
            val coreInstance = probe.getMethod("_nCoreInstance").invoke(null)
            val coreInstanceSeenByPlatform = probe.getMethod("_nCoreInstanceSeenByPlatform").invoke(null)

            assertEquals(coreInstance, coreInstanceSeenByPlatform)
        }
    }
}
