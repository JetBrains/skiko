package org.jetbrains.skia

import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skiko.Library
import kotlin.test.Test
import kotlin.test.assertEquals

fun getMakeGLAssembledInterfaceFunc(): NativePointer {
    return interopScope {
        _nGetMakeGLAssembledInterfaceFunc()
    }
}

class DirectContextTest {

    private companion object {
        init {
            Library.load()
        }
    }

    private fun makeFromInterface(): DirectContext {
        val fPtr = getMakeGLAssembledInterfaceFunc()
        val i = GLAssembledInterface.createFromNativePointers(NullPointer, fPtr)
        return DirectContext.makeGLWithInterface(i)
    }

    @Test
    fun makeFromInterfaceTest() {
        makeFromInterface()
    }

    @Test
    fun resourceCacheLimitTest() {
        val context = makeFromInterface()
        context.resourceCacheLimit = 1024
        assertEquals(1024, context.resourceCacheLimit)
    }
}

private external fun _nGetMakeGLAssembledInterfaceFunc(): NativePointer
