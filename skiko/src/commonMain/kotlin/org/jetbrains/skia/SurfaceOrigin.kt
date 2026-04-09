package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class SurfaceOrigin internal constructor(val ordinal: Int) {
    companion object {
        val TOP_LEFT = SurfaceOrigin(0)
        val BOTTOM_LEFT = SurfaceOrigin(1)
    }
}