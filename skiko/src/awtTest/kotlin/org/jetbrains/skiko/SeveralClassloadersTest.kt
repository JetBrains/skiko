package org.jetbrains.skiko

import org.jetbrains.skiko.util.PlatformAndURLClassLoader
import org.junit.Test
import java.net.URL
import java.nio.file.Paths
import kotlin.concurrent.thread

private fun testSkikoLoad(loader: ClassLoader) {
    val clazz = loader.loadClass("org.jetbrains.skiko.LibraryTestImpl")
    val tester = clazz.getDeclaredConstructor().newInstance()
    val ptr = clazz.getMethod("run").invoke(tester) as Long
    assert(ptr != 0L)

    val paragraphPackage = "org.jetbrains.skia.paragraph"
    val paragraphStyle = newInstance(loader, "$paragraphPackage.ParagraphStyle")
    val fontCollection = newInstance(loader, "$paragraphPackage.FontCollection")
    newInstance(loader, "$paragraphPackage.ParagraphBuilder", paragraphStyle, fontCollection)
}

private fun newInstance(loader: ClassLoader, fqName: String, vararg args: Any): Any {
    val argsClasses = args.map { it.javaClass }.toTypedArray()
    return loader.loadClass(fqName)
        .getDeclaredConstructor(*argsClasses)
        .newInstance(*args)
}

class SeveralClassloadersTest {
    @Test
    fun `load skiko in several classloaders (with skiko path)`()  {
        check(SkikoProperties.libraryPath != null)
        doTest()
    }

    @Test
    fun `load skiko in several classloaders (without skiko path)`()  {
        val oldValue = SkikoProperties.libraryPath!!
        SkikoProperties.libraryPath = null
        try {
            doTest()
        } finally {
            SkikoProperties.libraryPath = oldValue
        }
    }

    private fun doTest() {
        val threaded = false
        val jar = System.getProperty("skiko.jar.path")
        val stdlibClass = Class.forName("kotlin.jvm.internal.Intrinsics")
        val stdLibJar = stdlibClass.protectionDomain.codeSource.location
        val coroutinesClass = Class.forName("kotlinx.coroutines.CoroutineDispatcher")
        val coroutinesJar = coroutinesClass.protectionDomain.codeSource.location
        val urls = listOf(Paths.get(jar).toUri().toURL(), stdLibJar, coroutinesJar)
        val loaders = mutableListOf<ClassLoader>()
        repeat(4) {
            loaders += PlatformAndURLClassLoader(urls)
        }
        if (threaded) {
            val threads = mutableListOf<Thread>()
            loaders.forEach { classloader ->
                threads.add(thread {
                    Thread.currentThread().contextClassLoader = classloader
                    testSkikoLoad(classloader)
                })
            }
            threads.forEach {
                it.join()
            }
        } else {
            loaders.forEach { classloader ->
                testSkikoLoad(classloader)
            }
        }
    }
}