package org.jetbrains.skia

import kotlin.test.Test

class RuntimeEffectTest {
    @Test
    fun canCreateShader() {
        val shaderSKSL = """
            |half4 main(float2 coord) {
            |  float t = coord.x / 128;
            |  half4 white = half4(1);
            |  half4 black = half4(0,0,0,1);
            |  return mix(white, black, t);
            |}""".trimMargin()

        val effect = RuntimeEffect.makeForShader(shaderSKSL)
        val shader = effect.makeShader(null, null, null, true)

        val shader2SKSL = """
            |uniform shader input_1;
            |half4 main(float2 coord) {
            |  return input_1.eval(coord).bgra;
            |}""".trimMargin()

        val derivedEffect = RuntimeEffect.makeForShader(shader2SKSL)
        derivedEffect.makeShader(null, arrayOf(shader), null, false)

        val colorFilterSKSL = """
            |half4 main(half4 inColor) {
            | return inColor.bgra;
            |}""".trimMargin()
        RuntimeEffect.makeForColorFilter(colorFilterSKSL)
    }
}