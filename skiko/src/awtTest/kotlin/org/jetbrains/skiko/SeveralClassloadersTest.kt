package org.jetbrains.skiko

import org.junit.Ignore
import org.junit.Test
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import kotlin.concurrent.thread

private class PlatformAndURLClassLoader(classpath: List<URL>) :
    ClassLoader(getPlatformClassLoader()) {
    private val childClassLoader: ChildURLClassLoader

    private class FindClassClassLoader(parent: ClassLoader?) : ClassLoader(parent) {
        @Throws(ClassNotFoundException::class)
        public override fun findClass(name: String): Class<*> {
            return super.findClass(name)
        }
    }

    private class ChildURLClassLoader(urls: Array<URL>?, private val realParent: FindClassClassLoader) :
        URLClassLoader(urls, null) {
        @Throws(ClassNotFoundException::class)
        public override fun findClass(name: String): Class<*> {
            return try {
                super.findClass(name)
            } catch (e: ClassNotFoundException) {
                realParent.loadClass(name)
            }
        }
    }

    @Synchronized
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return try {
            childClassLoader.findClass(name)
        } catch (e: ClassNotFoundException) {
            super.loadClass(name, resolve)
        }
    }

    init {
        val urls = classpath.toTypedArray()
        childClassLoader = ChildURLClassLoader(
            urls, FindClassClassLoader(
                parent
            )
        )
    }
}

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

    @Ignore("For Igor to fix")
    @Test
    fun `load skiko in several classloaders (with skiko path)`()  {
        check(skikoLibraryPath != null)
        doTest()
    }

    @Ignore("For Igor to fix")
    @Test
    fun `load skiko in several classloaders (without skiko path)`()  {
        val oldValue = skikoLibraryPath!!
        skikoLibraryPath = null
        try {
            doTest()
        } finally {
            skikoLibraryPath = oldValue
        }
    }

    private var skikoLibraryPath: String?
        get() = System.getProperty(Library.SKIKO_LIBRARY_PATH_PROPERTY)
        set(value) {
            if (value != null) {
                System.setProperty(Library.SKIKO_LIBRARY_PATH_PROPERTY, value)
            } else {
                System.clearProperty(Library.SKIKO_LIBRARY_PATH_PROPERTY)
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
