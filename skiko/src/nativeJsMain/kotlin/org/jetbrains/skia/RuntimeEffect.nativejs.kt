package org.jetbrains.skia

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.withStringReferenceResult

internal actual fun RuntimeEffect.Companion.makeFromResultPtr(ptr: NativePointer): RuntimeEffect {
    val errorPtr = Result_nGetError(ptr)
    if (errorPtr == Native.NullPointer) {
        val effectPtr = Result_nGetPtr(ptr)
        Result_nDestroy(ptr)
        return RuntimeEffect(effectPtr)
    } else {
        // Error string is owned by Result
        val error = withStringReferenceResult { errorPtr }
        Result_nDestroy(ptr)
        throw Error(error)
    }
}
