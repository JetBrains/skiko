package org.jetbrains.skia.paragraph

@kotlin.jvm.JvmInline
value class Affinity internal constructor(val ordinal: Int) {
    companion object {
        val UPSTREAM = Affinity(0)
        val DOWNSTREAM = Affinity(1)
    }
}