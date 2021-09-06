package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad

abstract class SVGTransformableNode internal constructor(ptr: Long) : SVGNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}