package org.jetbrains.skia

import kotlin.math.PI
import kotlin.test.Test

class ShaderTest {
    @Test
    fun canMakeLinear() {
        val start = Point(0.0f, 0.0f)
        val end = Point(16.0f, 16.0f)
        val colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN)
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colorSpace = ColorSpace.sRGBLinear
        val colorsF = colors.map { Color4f(it) }.toTypedArray()

        Shader.makeLinearGradient(start, end, colors)
        Shader.makeLinearGradient(start, end, colors, positions)
        Shader.makeLinearGradient(start, end, colors, positions, style = GradientStyle.DEFAULT)
        Shader.makeLinearGradient(start, end, colorsF, colorSpace, positions, GradientStyle.DEFAULT)
    }


    @Test
    fun canMakeRadial() {
        val center = Point(8.0f, 8.0f)
        val radius = 8.0f
        val colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN)
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colorSpace = ColorSpace.sRGBLinear
        val colorsF = colors.map { Color4f(it) }.toTypedArray()

        Shader.makeRadialGradient(center, radius, colors)
        Shader.makeRadialGradient(center, radius, colors, positions)
        Shader.makeRadialGradient(center, radius, colors, positions, style = GradientStyle.DEFAULT)
        Shader.makeRadialGradient(center, radius, colorsF, colorSpace, positions, GradientStyle.DEFAULT)
    }


    @Test
    fun canMakeConical() {
        val start = Point(8.0f, 8.0f)
        val startRadius = 2.0f
        val end = Point(16.0f, 16.0f)
        val endRadius = 8.0f
        val colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN)
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colorSpace = ColorSpace.sRGBLinear
        val colorsF = colors.map { Color4f(it) }.toTypedArray()

        Shader.makeTwoPointConicalGradient(start, startRadius, end, endRadius, colors)
        Shader.makeTwoPointConicalGradient(start, startRadius, end, endRadius, colors, positions)
        Shader.makeTwoPointConicalGradient(start, startRadius, end, endRadius, colors, positions, style = GradientStyle.DEFAULT)
        Shader.makeTwoPointConicalGradient(start, startRadius, end, endRadius, colorsF, colorSpace, positions, GradientStyle.DEFAULT)
    }

    @Test
    fun canMakeSweep() {
        val center = Point(8.0f, 8.0f)
        val startAngle = 0.0f
        val endAngle = PI.toFloat()
        val colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN)
        val positions = floatArrayOf(0.0f, 0.7f, 1.0f)
        val colorSpace = ColorSpace.sRGBLinear
        val colorsF = colors.map { Color4f(it) }.toTypedArray()

        Shader.makeSweepGradient(center, colors)
        Shader.makeSweepGradient(center, colors, positions)
        Shader.makeSweepGradient(center, colors, positions, style = GradientStyle.DEFAULT)
        Shader.makeSweepGradient(center, startAngle, endAngle, colors, positions, GradientStyle.DEFAULT)
        Shader.makeSweepGradient(center, startAngle, endAngle, colorsF, colorSpace, positions, GradientStyle.DEFAULT)
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
        Shader.makeBlend(
            mode = BlendMode.MULTIPLY,
            src = Shader.makeLinearGradient(0.0f, 0.0f, 16.0f, 16.0f, intArrayOf(Color.BLACK, Color.WHITE)),
            dst = Shader.makeRadialGradient(8.0f, 8.0f, 8.0f, intArrayOf(Color.RED, Color.BLUE))
        )
    }

    @Test
    fun canMakeNoise() {
        Shader.makeFractalNoise(0.4f, 0.5f, 10, 0.5f)
        Shader.makeFractalNoise(0.4f, 0.5f, 10, 0.5f, ISize(16, 16))

        Shader.makeTurbulence(0.4f, 0.5f, 10, 0.5f)
        Shader.makeTurbulence(0.4f, 0.5f, 10, 0.5f, ISize(16, 16))
    }
}