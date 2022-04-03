package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentDifferent
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageFilterTest {

    private val originalBytes: ByteArray by lazy {
        renderAndReturnBytes(imageFilter = null)
    }

    private fun renderAndReturnBytes(imageFilter: ImageFilter? = null): ByteArray {
        return Surface.makeRasterN32Premul(20, 20).use {
            val paint = Paint().apply {
                setStroke(true)
                strokeWidth = 2f
            }

            val region = Region().apply {
                op(IRect(3, 3, 18, 18), Region.Op.UNION)
            }

            paint.imageFilter = imageFilter
            it.canvas.drawRegion(region, paint)

            val image = it.makeImageSnapshot()
            Bitmap.makeFromImage(image).readPixels()!!
        }
    }

    private fun imageFilterTest(imageFilter: () -> ImageFilter) = runTest {
        val modifiedPixels = renderAndReturnBytes(imageFilter = imageFilter())
        assertEquals(originalBytes.size, modifiedPixels.size)

        // we don't check the actual content of the pixels, we only assume they're different when ImageFilter applied
        assertContentDifferent(
            array1 = originalBytes,
            array2 = modifiedPixels,
            message = "pixels with applied ImageFilter should be different"
        )
    }

    @Test
    fun alphaThreshold() = imageFilterTest {
        ImageFilter.makeAlphaThreshold(
            Region().apply {
                setRect(IRect(5, 5, 10, 10))
            },
            innerMin = 0.9f,
            outerMax = 0.2f,
            input = null,
            crop = null
        )
    }

    @Test
    fun arithmetic() = imageFilterTest {
        ImageFilter.makeArithmetic(
            k1 = 0.5f, k2 = 0.5f, k3 = 0.5f, k4 = 0.5f, enforcePMColor = true,
            bg = null, fg = null, crop = null
        )
    }

    @Test
    fun blend() = imageFilterTest {
        val region = Region().apply {
            setRect(IRect(5, 5, 10, 10))
        }
        ImageFilter.makeBlend(
            blendMode = BlendMode.COLOR,
            bg = ImageFilter.makeBlur(2f, 2f, mode = FilterTileMode.CLAMP),
            fg = ImageFilter.makeAlphaThreshold(r = region, innerMin = 0.5f, outerMax = 0.5f, input = null, crop = null),
            crop = null
        )
    }

    @Test
    fun blur() = imageFilterTest {
        ImageFilter.makeBlur(
            1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
        )
    }

    @Test
    fun colorFilter() = imageFilterTest {
        ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )
    }

    @Test
    fun compose() = imageFilterTest {
        val inner = ImageFilter.makeBlur(
            1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
        )
        val outer = ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )
        ImageFilter.makeCompose(
            inner = inner,
            outer = outer
        )
    }

    @Test
    fun displacementMap() = imageFilterTest {
        val colorFilter = ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )
        ImageFilter.makeDisplacementMap(
            x = ColorChannel.R,
            y = ColorChannel.G,
            scale = 2f,
            displacement = null,
            color = colorFilter,
            crop = null
        )
    }

    @Test
    fun dropShadow() = imageFilterTest {
        ImageFilter.makeDropShadow(
            dx = 2f, dy = 2f, sigmaX = 2f, sigmaY = 2f, color = Color.BLACK,
            input = null, crop = null
        )
    }

    @Test
    fun dropShadowOnly() = imageFilterTest {
        ImageFilter.makeDropShadowOnly(
            dx = 2f, dy = 2f, sigmaX = 1f, sigmaY = 2f,
            color = Color.BLACK, input = null, crop = null
        )
    }

    @Test
    fun makeImage() = imageFilterTest {
        val bitmap = Bitmap()
        bitmap.setImageInfo(ImageInfo.makeN32Premul(5, 5))
        bitmap.installPixels(ByteArray(100) { -1 })

        ImageFilter.makeImage(
            Image.makeFromBitmap(bitmap)
        )
    }

    @Test
    fun makeMagnifier() = imageFilterTest {
        ImageFilter.makeMagnifier(
            r = Rect(0f, 0f, 15f, 15f),
            input = null,
            crop = null,
            inset = 2f
        )
    }

    @Test
    fun makeMatrixConvolution() = imageFilterTest {
        ImageFilter.makeMatrixConvolution(
            kernelH = 2,
            kernelW = 2,
            kernel = floatArrayOf(0.5f, 1f, 0.2f, 1.2f),
            gain = 1f,
            bias = 0f,
            offsetX = 1,
            offsetY = 1,
            tileMode = FilterTileMode.CLAMP,
            convolveAlpha = true,
            input = null,
            crop = null
        )
    }

    @Test // TODO: use SamplingMode._packAs2Ints after PR(316) gets merged
    fun makeMatrixTransform() = imageFilterTest {
        ImageFilter.makeMatrixTransform(
            Matrix33.makeTranslate(2f, 2f),
            mode = SamplingMode.LINEAR,
            input = null
        )
    }

    @Test
    fun makeMerge() = imageFilterTest {
        val filter1 = ImageFilter.makeMagnifier(
            r = Rect(0f, 0f, 15f, 15f),
            input = null,
            crop = null,
            inset = 2f
        )

        val filter2 = ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )

        val filter3 = ImageFilter.makeBlur(
            1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
        )

        ImageFilter.makeMerge(
            filters = arrayOf(filter1, filter2, filter3),
            crop = IRect(2, 2, 12, 12)
        )
    }

    @Test
    fun makeOffset() = imageFilterTest {
        ImageFilter.makeOffset(dx = 2f, dy = 2f, input = null, crop = null)
    }

    @Test
    fun makePaint() = imageFilterTest {
        ImageFilter.makePaint(
            paint = Paint().setStroke(false).setColor4f(Color4f(Color.RED), colorSpace = null),
            crop = null
        )
    }

    @Test
    fun makeRuntimeShader() = imageFilterTest {
        // A simple Skia shader that bumps up the red channel of every non-transparent
        // pixel to full intensity, and leaves green and blue channels unchanged.
        val sksl = """
            uniform shader content;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                return vec4(1.0 * c.a, c.g * c.a, c.b * c.a, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformFloat2() = imageFilterTest {
        // A simple Skia shader that bumps up the red channel and bumps down the green channel
        // of every pixel. This tests the binding to SkRuntimeShaderBuilder::uniform(kFloat2)
        val sksl = """
            uniform shader content;
            uniform vec2 channels;
            
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                return vec4(c.a * (c.r + (1.0 - c.r) * channels.x),
                   c.a * (c.g * channels.y), c.a * c.b, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("channels", 0.9f, 0.5f)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformFloat3() = imageFilterTest {
        // A simple Skia shader that replaces every pixel with the specified uniform color.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kFloat3)
        val sksl = """
            uniform shader content;
            uniform vec3 color;
            
            vec4 main(vec2 coord) {
                return vec4(color, 1.0);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("color", 1.0f, 0.0f, 1.0f)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformFloat4() = imageFilterTest {
        // A simple Skia shader that replaces every pixel with the specified uniform color.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kFloat4)
        val sksl = """
            uniform shader content;
            uniform vec4 color;
            
            vec4 main(vec2 coord) {
                return color;
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("color", 1.0f, 0.0f, 1.0f, 1.0f)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformInt() = imageFilterTest {
        // A simple Skia shader that replaces the red channel of every pixel.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kInt)
        val sksl = """
            uniform shader content;
            uniform int replace;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                return vec4(c.a * float(replace) / 255.0, c.g * c.a, c.b * c.a, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("replace", 128)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformInt2() = imageFilterTest {
        // A simple Skia shader that replaces the red and green channels of every pixel.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kInt2)
        val sksl = """
            uniform shader content;
            uniform ivec2 replace;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                return vec4(c.a * float(replace.x) / 255.0, c.a * float(replace.y) / 255.0, c.b * c.a, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("replace", 128, 160)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformInt3() = imageFilterTest {
        // A simple Skia shader that replaces the red, green and blue channels of every pixel.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kInt3)
        val sksl = """
            uniform shader content;
            uniform ivec3 replace;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                return vec4(c.a * float(replace.x) / 255.0, c.a * float(replace.y) / 255.0, 
                    c.a * float(replace.z) / 255.0, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("replace", 128, 160, 192)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformInt4() = imageFilterTest {
        // A simple Skia shader that all channels of every pixel.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kInt4)
        val sksl = """
            uniform shader content;
            uniform ivec4 replace;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                float alpha = c.a * float(replace.w) / 255.0;
                return vec4(alpha * float(replace.x) / 255.0, alpha * float(replace.y) / 255.0, 
                    alpha * float(replace.z) / 255.0, alpha);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("replace", 128, 160, 192, 100)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformFloatMatrix2x2() = imageFilterTest {
        // A simple Skia shader that applies a 2x2 matrix on the red and green channels.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kFloat2x2)
        val sksl = """
            uniform shader content;
            uniform mat2 matrix2x2;
            
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                vec2 intermediate = c.rg * matrix2x2 + vec2(0.1, 0.1);
                return vec4(c.a * intermediate[0], c.a * intermediate[1], c.a * c.g, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("matrix2x2", Matrix22(0.4f, 0.3f, 0.2f, 0.6f))

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformFloatMatrix3x3() = imageFilterTest {
        // A simple Skia shader that applies a 3x3 matrix on the red, green and blue channels.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kFloat3x3)
        val sksl = """
            uniform shader content;
            uniform mat3 matrix3x3;
            
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                vec3 intermediate = c.rgb * matrix3x3 + vec3(0.1, 0.1, 0.1);
                return vec4(c.a * intermediate[0], c.a * intermediate[1], c.a * c.g, c.a);
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("matrix3x3", Matrix33(0.4f, 0.3f, 0.2f, 0.6f, 0.1f, 0.2f, 0.3f, 0.2f, 0.1f))

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderUniformFloatMatrix4x4() = imageFilterTest {
        // A simple Skia shader that applies a 4x4 matrix on all channels.
        // This tests the binding to SkRuntimeShaderBuilder::uniform(kFloat4x4)
        val sksl = """
            uniform shader content;
            uniform mat4 matrix4x4;
            
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                vec4 intermediate = c.rgba * matrix4x4 + vec4(0.1, 0.1, 0.1, 0.1);
                return intermediate;
            }
        """

        val runtimeEffect = RuntimeEffect.makeForShader(sksl)
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("matrix4x4", Matrix44(0.2f, 0.3f, 0.2f, 0.1f, 0.4f, 0.1f, 0.2f, 0.1f, 0.1f, 0.3f, 0.2f, 0.1f, 0.2f, 0.2f, 0.2f, 0.3f))

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = shaderBuilder,
            shaderName = "content",
            input = null
        )
    }

    @Test
    fun makeRuntimeShaderFromArrays() = imageFilterTest {
        // A Skia shader that has two children shaders - one that applies our custom shader logic
        // on the underlying render node content, and another that is the built in blur. This
        // shader also has a float uniform that is used to decide which one of these two children
        // shaders to apply on a given pixel, based on the X coordinate.
        // This test covers not only ImageFilter.makeRuntimeShader API, but also
        // RuntimeShaderBuilder.uniform.
        val compositeSksl = """
            uniform shader content;
            uniform shader blurred;
            uniform float cutoff;
            vec4 main(vec2 coord) {
                vec4 c = content.eval(coord);
                vec4 b = blurred.eval(coord);
                if (coord.x > cutoff) {
                    return vec4(1.0 * c.a, c.g * c.a, c.b * c.a, c.a);
                } else {
                    return b;
                }
            }
        """

        val compositeRuntimeEffect = RuntimeEffect.makeForShader(compositeSksl)
        val compositeShaderBuilder = RuntimeShaderBuilder(compositeRuntimeEffect)
        // Pass a float uniform into our shader
        compositeShaderBuilder.uniform("cutoff", 10.0f)

        // And use ImageFilter.makeBlur as the second child input to our composite shader
        val blurImageFilter = ImageFilter.makeBlur(sigmaX = 2.0f, sigmaY = 2.0f, mode = FilterTileMode.DECAL)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = compositeShaderBuilder,
            shaderNames = arrayOf("content", "blurred"),
            inputs = arrayOf(null, blurImageFilter)
        )
    }

    @Test
    fun makeTile() = imageFilterTest {
        ImageFilter.makeTile(
            src = Rect(0f, 0f, 3f, 3f),
            dst = Rect(5f, 5f, 19f, 19f),
            input = null
        )
    }

    @Test
    fun makeDilate() = imageFilterTest {
        ImageFilter.makeDilate(
            rx = 10f, ry = 10f, input = null, crop = null
        )
    }

    @Test
    fun makeErode() = imageFilterTest {
        ImageFilter.makeErode(
            rx = 5f, ry = 5f, input = null, crop = null
        )
    }

    @Test
    fun makeDistantLitDiffuse() = imageFilterTest {
        ImageFilter.makeDistantLitDiffuse(
            x = 2f, y = 2f, z = 2f, lightColor = Color.RED,
            surfaceScale = 2f, kd = 1f, input = null, crop = IRect(5, 5, 10, 10)
        )
    }

    @Test
    fun makePointLitDiffuse() = imageFilterTest {
        ImageFilter.makePointLitDiffuse(
            x = 2f, y = 2f, z = 2f, lightColor = Color.RED,
            surfaceScale = 2f, kd = 1f, input = null, crop = IRect(5, 5, 10, 10)
        )
    }

    @Test
    fun makeSpotLitDiffuse() = imageFilterTest {
        ImageFilter.makeSpotLitDiffuse(
            x0 = 0f, y0 = 0f, z0 = 0f, x1 = 10f, y1 = 10f, z1 = 10f,
            falloffExponent = 2f, cutoffAngle = 0f, lightColor = Color.RED,
            surfaceScale = 2f, kd = 1f, input = null, crop = IRect(0, 0, 15, 15)
        )
    }

    @Test
    fun makeDistantLitSpecular() = imageFilterTest {
        ImageFilter.makeDistantLitSpecular(
            x = 1f, y = 1f, z = 10f, lightColor = Color.RED,
            surfaceScale = 2f, ks = 1f, shininess = 1f, input = null,
            crop = IRect(0, 0, 15, 15)
        )
    }

    @Test
    fun makePointLitSpecular() = imageFilterTest {
        ImageFilter.makePointLitSpecular(
            x = 1f, y = 1f, z = 10f, lightColor = Color.RED,
            surfaceScale = 2f, ks = 1f, shininess = 1f, input = null,
            crop = IRect(0, 0, 15, 15)
        )
    }

    @Test
    fun makeSpotLitSpecular() = imageFilterTest {
        ImageFilter.makeSpotLitSpecular(
            x0 = 0f, y0 = 0f, z0 = 0f, x1 = 10f, y1 = 10f, z1 = 10f,
            falloffExponent = 2f, cutoffAngle = 0f, lightColor = Color.RED,
            surfaceScale = 2f, ks = 1f, input = null, crop = IRect(0, 0, 15, 15),
            shininess = 1f
        )
    }
}
