package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer

abstract class SVGTransformableNode internal constructor(ptr: NativePointer) : SVGNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}