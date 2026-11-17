@file:OptIn(InternalSkikoApi::class)

package org.jetbrains.skiko

import org.jetbrains.skia.GaneshLibrary
import org.jetbrains.skia.ImageInfo
import org.junit.Test
import java.io.File
import java.net.URLClassLoader
import kotlin.test.assertSame

class SplitLibraryClassloadersTest {
    @Test
    fun splitLibrariesKeepTheirOwnCore() {
        val urls = listOf(
            File(System.getProperty("skiko.jar.path")).toURI().toURL(),
            ImageInfo::class.java.protectionDomain.codeSource.location,
            GaneshLibrary::class.java.protectionDomain.codeSource.location,
            SplitLibraryProbe::class.java.protectionDomain.codeSource.location,
            Unit::class.java.protectionDomain.codeSource.location,
        ).distinct().toTypedArray()

        URLClassLoader(urls, ClassLoader.getPlatformClassLoader()).use { first ->
            URLClassLoader(urls, ClassLoader.getPlatformClassLoader()).use { second ->
                val probes = listOf(first, second).map { loader ->
                    val cls = loader.loadClass(SplitLibraryProbe::class.java.name)
                    cls.getDeclaredConstructor().newInstance()
                }
                // Recheck the first IDEA plugin after loading the second, too.
                (probes + probes.first()).forEach { probe ->
                    val expected = probe.javaClass.classLoader.loadClass(ImageInfo::class.java.name)
                    assertSame(expected, probe.javaClass.getMethod("loadAndGetCoreClass").invoke(probe))
                }
            }
        }
    }
}

class SplitLibraryProbe {
    fun loadAndGetCoreClass(): Class<*> {
        GaneshLibrary.load()
        LibraryLoader("skiko-platform-$hostId", cls = SplitLibraryProbe::class.java).loadOnce()
        return coreImageInfoClass()
    }

    private external fun coreImageInfoClass(): Class<*>
}
