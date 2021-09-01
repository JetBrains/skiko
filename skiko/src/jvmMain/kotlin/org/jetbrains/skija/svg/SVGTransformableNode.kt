package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad

abstract class SVGTransformableNode internal constructor(ptr: Long) : SVGNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}