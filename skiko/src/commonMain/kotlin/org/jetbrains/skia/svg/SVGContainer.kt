package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer

abstract class SVGContainer internal constructor(ptr: NativePointer) : SVGTransformableNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}