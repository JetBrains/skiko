package org.jetbrains.skia

import kotlin.math.PI
import kotlin.test.Test

class ShaderTest {
    @Test
    fun canMakeLinear() {
        val start = Point(0.0f, 0.0f)
        val end = Point(16.0f, 16.0f)
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colors = arrayOf(Color4f(Color.RED), Color4f(Color.BLUE), Color4f(Color.GREEN))
        val gradient = Gradient(Gradient.Colors(colors, positions, FilterTileMode.CLAMP, ColorSpace.sRGBLinear))

        Shader.makeLinearGradient(start, end, gradient)
        Shader.makeLinearGradient(start.x, start.y, end.x, end.y, gradient, Matrix33.IDENTITY)
    }


    @Test
    fun canMakeRadial() {
        val center = Point(8.0f, 8.0f)
        val radius = 8.0f
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colors = arrayOf(Color4f(Color.RED), Color4f(Color.BLUE), Color4f(Color.GREEN))
        val gradient = Gradient(Gradient.Colors(colors, positions, FilterTileMode.CLAMP, ColorSpace.sRGBLinear))

        Shader.makeRadialGradient(center, radius, gradient)
        Shader.makeRadialGradient(center.x, center.y, radius, gradient, Matrix33.IDENTITY)
    }


    @Test
    fun canMakeConical() {
        val start = Point(8.0f, 8.0f)
        val startRadius = 2.0f
        val end = Point(16.0f, 16.0f)
        val endRadius = 8.0f
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colors = arrayOf(Color4f(Color.RED), Color4f(Color.BLUE), Color4f(Color.GREEN))
        val gradient = Gradient(Gradient.Colors(colors, positions, FilterTileMode.CLAMP, ColorSpace.sRGBLinear))

        Shader.makeTwoPointConicalGradient(start, startRadius, end, endRadius, gradient)
        Shader.makeTwoPointConicalGradient(start.x, start.y, startRadius, end.x, end.y, endRadius, gradient, Matrix33.IDENTITY)
    }

    @Test
    fun canMakeSweep() {
        val center = Point(8.0f, 8.0f)
        val startAngle = 0.0f
        val endAngle = PI.toFloat()
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colors = arrayOf(Color4f(Color.RED), Color4f(Color.BLUE), Color4f(Color.GREEN))
        val gradient = Gradient(Gradient.Colors(colors, positions, FilterTileMode.CLAMP, ColorSpace.sRGBLinear))

        Shader.makeSweepGradient(center, gradient)
        Shader.makeSweepGradient(center.x, center.y, gradient, Matrix33.IDENTITY)
        Shader.makeSweepGradient(center, startAngle, endAngle, gradient)
        Shader.makeSweepGradient(center.x, center.y, startAngle, endAngle, gradient, Matrix33.IDENTITY)
    }

    @Test
    fun canMakeEmpty() {
        Shader.makeEmpty()
    }

    @Test
    fun canMakeColor() {
        Shader.makeColor(Color.RED)
        Shader.makeColor(Color4f(Color.BLUE), ColorSpace.sRGB)
    }

    @Test
    fun canMakeBlend() {
        val srcGradient = Gradient(
            Gradient.Colors(arrayOf(Color4f(Color.BLACK), Color4f(Color.WHITE)), tileMode = FilterTileMode.CLAMP)
        )
        val dstGradient = Gradient(
            Gradient.Colors(arrayOf(Color4f(Color.RED), Color4f(Color.BLUE)), tileMode = FilterTileMode.CLAMP)
        )
        Shader.makeBlend(
            mode = BlendMode.MULTIPLY,
            src = Shader.makeLinearGradient(0.0f, 0.0f, 16.0f, 16.0f, srcGradient),
            dst = Shader.makeRadialGradient(8.0f, 8.0f, 8.0f, dstGradient)
        )
    }

    @Test
    fun canMakeNoise() {
        Shader.makeFractalNoise(0.4f, 0.5f, 10, 0.5f)
        Shader.makeFractalNoise(0.4f, 0.5f, 10, 0.5f, ISize(16, 16))

        Shader.makeTurbulence(0.4f, 0.5f, 10, 0.5f)
        Shader.makeTurbulence(0.4f, 0.5f, 10, 0.5f, ISize(16, 16))
    }

    @Test
    fun canMakeWithLocalMatrix() {
        Shader.makeEmpty().makeWithLocalMatrix(Matrix33.IDENTITY)
    }

    @Test
    fun canMakeWithColorFilter() {
        Shader.makeEmpty().makeWithColorFilter(ColorFilter.sRGBToLinearGamma)
    }
}
