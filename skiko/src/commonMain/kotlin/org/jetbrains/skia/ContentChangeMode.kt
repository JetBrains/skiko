package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class ContentChangeMode internal constructor(val ordinal: Int) {
    companion object {
        /** Discards surface on change.  */
        val DISCARD = ContentChangeMode(0)

        /** Preserves surface on change.  */
        val RETAIN = ContentChangeMode(1)
    }
}