package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad

abstract class SVGContainer internal constructor(ptr: Long) : SVGTransformableNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}