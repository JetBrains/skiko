@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package SkiaJvmSample

import sun.awt.AWTAccessor
import java.awt.Rectangle
import java.awt.Window
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class MethodInvocator(aClass: Class<*>, val myMethod: Method, vararg parameterTypes: Class<*>?) {
    operator fun invoke(`object`: Any?, vararg arguments: Any?): Any {
        return try {
            myMethod.invoke(`object`, *arguments)
        }
        catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
        catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }
}

/**
 * Provides extensions for OpenJDK API, implemented in JetBrains JDK.
 * For OpenJDK defaults to some meaningful results where applicable or otherwise throws runtime exception.
 *
 * WARNING: For internal usage only.
 *
 * @author tav
 */
object JdkEx {
    // CUSTOM DECOR SUPPORT {{
    val JAVA_VENDOR = System.getProperty("java.vm.vendor", "Unknown")
    val isCustomDecorationSupported: Boolean
        get() = true

    fun setHasCustomDecoration(window: Window) {
        println("Supported: $isCustomDecorationSupported")
        if (!isCustomDecorationSupported) return
        MyCustomDecorMethods.SET_HAS_CUSTOM_DECORATION.invoke(window)
    }

    fun setCustomDecorationHitTestSpots(window: Window, spots: List<Rectangle>) {
        if (!isCustomDecorationSupported) return
        MyCustomDecorMethods.SET_CUSTOM_DECORATION_HITTEST_SPOTS.invoke(AWTAccessor.getComponentAccessor().getPeer(window)!!, spots)
    }

    fun setCustomDecorationTitleBarHeight(window: Window, height: Int) {
        if (!isCustomDecorationSupported) return
        MyCustomDecorMethods.SET_CUSTOM_DECORATION_TITLEBAR_HEIGHT.invoke(AWTAccessor.getComponentAccessor().getPeer(window)!!, height)
    }

    // lazy init
    private object MyCustomDecorMethods {
        val SET_HAS_CUSTOM_DECORATION = MyMethod.create(Window::class.java, "setHasCustomDecoration")
        val SET_CUSTOM_DECORATION_HITTEST_SPOTS = MyMethod.create("sun.awt.windows.WWindowPeer", "setCustomDecorationHitTestSpots",
                MutableList::class.java)
        val SET_CUSTOM_DECORATION_TITLEBAR_HEIGHT = MyMethod.create("sun.awt.windows.WWindowPeer", "setCustomDecorationTitleBarHeight",
                Int::class.javaPrimitiveType)
    }

    // }} CUSTOM DECOR SUPPORT
    private class MyMethod private constructor(val myMethod: Method) {

        operator fun invoke(`object`: Any, vararg arguments: Any?): Any? {
            return myMethod.invoke(`object`, *arguments)
        }

        companion object {
            fun create(className: String, methodName: String, vararg parameterTypes: Class<*>?): MyMethod {
                return create(Class.forName(className), methodName, *parameterTypes)
            }

            fun create(cls: Class<*>, methodName: String, vararg parameterTypes: Class<*>?): MyMethod {
                val m = cls.getDeclaredMethod(methodName, *parameterTypes)
                m.isAccessible = true
                return MyMethod(m)
            }
        }
    }
}