package org.jetbrains.skia

import kotlin.test.Test

class MaskFilterTest {
    @Test
    fun canCreate() {
        MaskFilter.makeBlur(FilterBlurMode.NORMAL, 0.5f)
        MaskFilter.makeClip(100, 200)
        MaskFilter.makeGamma(1.2f)
        MaskFilter.makeTable(ByteArray(256) { 127 })
        MaskFilter.makeShader(Shader.makeColor(Color.GREEN))
    }
}