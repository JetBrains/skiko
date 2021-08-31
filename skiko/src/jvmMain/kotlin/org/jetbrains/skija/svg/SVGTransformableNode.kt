package org.jetbrains.skija.svg

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.Library.Companion.staticLoad

abstract class SVGTransformableNode @ApiStatus.Internal constructor(ptr: Long) : SVGNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}