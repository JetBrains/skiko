package org.jetbrains.skia

import kotlin.test.Test

class RuntimeEffectTest {
    @Test
    fun canCreate() {
        val shaderSKSL = """
            |half4 main(float2 coord) {
            |  float t = coord.x / 128;
            |  half4 white = half4(1);
            |  half4 black = half4(0,0,0,1);
            |  return mix(white, black, t);
            |}""".trimMargin()

        RuntimeEffect.makeForShader(shaderSKSL)
    }
}