package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

internal actual fun RuntimeEffect.Companion.makeFromResultPtr(ptr: NativePointer): RuntimeEffect
    = RuntimeEffect(ptr)
