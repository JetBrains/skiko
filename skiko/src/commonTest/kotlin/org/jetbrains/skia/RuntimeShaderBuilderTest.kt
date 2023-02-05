package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test

class RuntimeShaderBuilderTest {

    private fun renderAndReturnBytes(shader: Shader? = null): ByteArray {
        return Surface.makeRasterN32Premul(20, 20).use {
            val paint = Paint().apply {
                setStroke(true)
                strokeWidth = 2f
            }

            val region = Region().apply {
                op(IRect(3, 3, 18, 18), Region.Op.UNION)
            }

            paint.shader = shader
            it.canvas.drawRegion(region, paint)

            val image = it.makeImageSnapshot()
            Bitmap.makeFromImage(image).readPixels()!!
        }
    }

    private fun shaderTest(shader: () -> Shader) = runTest {
        // We just test that rendering with the given shader is successful.
        renderAndReturnBytes(shader = shader())
    }

    @Test
    fun customShader() = shaderTest {
        val shaderSksl = """
            uniform shader content;
            uniform float greenAmount;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                c.r += 0.5;
                c.g = greenAmount;
                c.b -= 0.5;
                return c;
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(shaderSksl)
        val runtimeShaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        runtimeShaderBuilder.child("content", Shader.makeColor(Color.BLUE))
        runtimeShaderBuilder.uniform("greenAmount", 0.7f)
        runtimeShaderBuilder.makeShader()
    }

    @Test
    fun customShaderWithMatrix() = shaderTest {
        val shaderSksl = """
            uniform shader content;
            uniform vec2 baValue;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                c.rg += coord;
                c.ba = baValue;
                return c;
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(shaderSksl)
        val runtimeShaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        runtimeShaderBuilder.child("content", Shader.makeColor(Color.MAGENTA))
        runtimeShaderBuilder.uniform("baValue", 0.6f, 0.2f)
        runtimeShaderBuilder.makeShader(Matrix33.makeRotate(45f))
    }

}
