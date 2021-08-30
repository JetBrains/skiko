package org.jetbrains.skija.svg

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.Library.Companion.staticLoad

abstract class SVGContainer @ApiStatus.Internal constructor(ptr: Long) : SVGTransformableNode(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }
}