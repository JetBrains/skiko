package org.jetbrains.skiko

import org.junit.Test
import java.net.URL
import java.net.URLClassLoader
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
}

class SeveralClassloaders {
    @Test
    fun `load skiko in several classloaders`()  {
        val threaded = false
        val jar = System.getProperty("skiko.jar.path")
        val stdlibClass = Class.forName("kotlin.jvm.internal.Intrinsics")
        val stdLibJar = stdlibClass.protectionDomain.codeSource.location
        val urls = listOf(URL("file:/${if (hostOs.isWindows) "" else "/"}$jar"), stdLibJar)
        val loaders = arrayOf(PlatformAndURLClassLoader(urls), PlatformAndURLClassLoader(urls))
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