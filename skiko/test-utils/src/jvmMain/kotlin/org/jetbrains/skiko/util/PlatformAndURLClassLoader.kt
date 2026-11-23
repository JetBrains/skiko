package org.jetbrains.skiko.util

import java.net.URL
import java.net.URLClassLoader

/**
 * A class loader that loads classes from [classpath] and shares none of them with other loaders,
 * as separate IDEA plugins would.
 */
class PlatformAndURLClassLoader(classpath: List<URL>) :
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
            // Defining a class twice is an error, and a class can be requested again after it was loaded.
            findLoadedClass(name)?.let { return it }
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
