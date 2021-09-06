package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad

abstract class SVGContainer internal constructor(ptr: Long) : SVGTransformableNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}