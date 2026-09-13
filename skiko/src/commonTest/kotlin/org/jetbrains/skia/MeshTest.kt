package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.TestGlContext
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers [Mesh] and the types it is built from with a quad of two triangles, inset from the edges of
 * a 16x16 surface and read from a vertex buffer whose stride is padded past the attributes it holds.
 *
 * Meshes are rasterized by GPU-backed canvases; a raster surface accepts a mesh draw and produces no
 * pixels for it. What a drawing test can assert on every target is therefore that the draw reaches
 * Skia and leaves the surface outside the mesh intact.
 */
class MeshTest {

    @Test
    fun specificationReportsDeclaredStride() {
        makeSpecification().use { specification ->
            assertEquals(VERTEX_STRIDE, specification.stride)
        }
    }

    @Test
    fun specificationRejectsMalformedShader() {
        val exception = assertFailsWith<IllegalArgumentException> {
            makeSpecification(fragmentShader = "float2 main(const Varyings v) { return not-sksl; }")
        }
        assertTrue(!exception.message.isNullOrEmpty(), "Expected the SkSL compiler error")
    }

    @Test
    fun specificationRejectsMisalignedVertexStride() {
        val exception = assertFailsWith<IllegalArgumentException> {
            makeSpecification(vertexStride = VERTEX_STRIDE + 2)
        }
        assertTrue(!exception.message.isNullOrEmpty(), "Expected the reason the stride was rejected")
    }

    @Test
    fun buffersReportSizeInBytes() {
        MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
            assertEquals(6 * VERTEX_STRIDE, vertexBuffer.size)
        }
        MeshIndexBuffer.make(QUAD_INDICES).use { indexBuffer ->
            assertEquals(2 * QUAD_INDICES.size, indexBuffer.size)
        }
    }

    @Test
    fun drawsMesh() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                brightnessUniforms(specification, 1f).use { uniforms ->
                    Mesh.make(
                        specification = specification,
                        mode = Mesh.Mode.TRIANGLES,
                        vertexBuffer = vertexBuffer,
                        vertexCount = 6,
                        bounds = BOUNDS,
                        uniforms = uniforms
                    ).use { mesh ->
                        // An empty recording as the control for the op count below.
                        assertEquals(0, recordedOpCount { })
                        assertEquals(1, recordedOpCount { canvas ->
                            canvas.drawMesh(mesh, BlendMode.SRC, Paint())
                        })

                        val pixels = drawOnRasterSurface { canvas ->
                            canvas.drawMesh(mesh, BlendMode.SRC, Paint())
                        }
                        assertEquals(Color.WHITE, pixels.getColor(0, 0), "Drew outside the mesh bounds")
                    }
                }
            }
        }
    }

    @Test
    fun drawsIndexedMesh() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_CORNERS)).use { vertexBuffer ->
                MeshIndexBuffer.make(QUAD_INDICES).use { indexBuffer ->
                    brightnessUniforms(specification, 1f).use { uniforms ->
                        Blender.makeMode(BlendMode.SRC).use { blender ->
                            Mesh.makeIndexed(
                                specification = specification,
                                mode = Mesh.Mode.TRIANGLES,
                                vertexBuffer = vertexBuffer,
                                vertexCount = 4,
                                indexBuffer = indexBuffer,
                                indexCount = QUAD_INDICES.size,
                                bounds = BOUNDS,
                                uniforms = uniforms
                            ).use { mesh ->
                                assertEquals(1, recordedOpCount { canvas ->
                                    canvas.drawMesh(mesh, blender, Paint())
                                })

                                val pixels = drawOnRasterSurface { canvas ->
                                    canvas.drawMesh(mesh, blender, Paint())
                                }
                                assertEquals(Color.WHITE, pixels.getColor(0, 0), "Drew outside the mesh bounds")
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun uniformBuilderWritesEachUniformAtItsOwnOffset() {
        makeSpecification().use { specification ->
            MeshUniformBuilder(specification).use { builder ->
                val unwritten = builder.build().use { it.bytes }
                assertEquals(8, unwritten.size, "The specification declares two float uniforms")
                assertContentEquals(ByteArray(8), unwritten, "Unset uniforms hold zeroes")

                builder.uniform(BRIGHTNESS, 0.75f)
                val written = builder.build().use { it.bytes }
                assertEquals(8, written.size)
                assertEquals(0f, written.floatAt(0), "'$BASE' was never written")
                assertEquals(0.75f, written.floatAt(1))

                builder.uniform(BASE, 0.5f)
                builder.uniform(BRIGHTNESS, 0.25f)
                val rewritten = builder.build().use { it.bytes }
                assertEquals(0.5f, rewritten.floatAt(0))
                assertEquals(0.25f, rewritten.floatAt(1))
            }
        }
    }

    @Test
    fun uniformBuilderRejectsUnknownUniform() {
        makeSpecification().use { specification ->
            MeshUniformBuilder(specification).use { builder ->
                val fromFloats = assertFailsWith<IllegalArgumentException> {
                    builder.uniform("noSuchUniform", 1f)
                }
                assertTrue(
                    fromFloats.message!!.contains("noSuchUniform"),
                    "Expected the rejected name in: ${fromFloats.message}"
                )

                // Integer values reach Skia through an entry point of their own.
                val fromInts = assertFailsWith<IllegalArgumentException> {
                    builder.uniform("noSuchUniform", 1)
                }
                assertTrue(
                    fromInts.message!!.contains("noSuchUniform"),
                    "Expected the rejected name in: ${fromInts.message}"
                )
            }
        }
    }

    @Test
    fun uniformBuilderRejectsValueOfWrongWidth() {
        makeSpecification().use { specification ->
            MeshUniformBuilder(specification).use { builder ->
                // Two values against a one-float uniform: the name resolves, the width does not.
                val fromFloats = assertFailsWith<IllegalArgumentException> {
                    builder.uniform(BRIGHTNESS, 1f, 1f)
                }
                assertTrue(
                    fromFloats.message!!.contains("'$BRIGHTNESS' is not 2 values wide"),
                    "Expected a width mismatch rather than an unknown name in: ${fromFloats.message}"
                )

                val fromInts = assertFailsWith<IllegalArgumentException> {
                    builder.uniform(BRIGHTNESS, 1, 1)
                }
                assertTrue(
                    fromInts.message!!.contains("'$BRIGHTNESS' is not 2 values wide"),
                    "Expected a width mismatch rather than an unknown name in: ${fromInts.message}"
                )
            }
        }
    }

    @Test
    fun uniformBuilderRejectsValueOfTheWrongKind() {
        makeLayoutSpecification().use { specification ->
            MeshUniformBuilder(specification).use { builder ->
                // An int and a float are both four bytes, so only the declared type separates them.
                val intoInts = assertFailsWith<IllegalArgumentException> {
                    builder.uniform("steps", 1f, 1f)
                }
                assertTrue(
                    intoInts.message!!.contains("'steps' does not hold floating point values"),
                    "Expected a kind mismatch rather than a width mismatch in: ${intoInts.message}"
                )

                val intoFloats = assertFailsWith<IllegalArgumentException> {
                    builder.uniform("offset", 1, 1)
                }
                assertTrue(
                    intoFloats.message!!.contains("'offset' does not hold integer values"),
                    "Expected a kind mismatch rather than a width mismatch in: ${intoFloats.message}"
                )

                // A half uniform holds floating point values, so it takes floats.
                builder.uniform("brightness", 1f)
            }
        }
    }

    @Test
    fun meshRejectsMissingUniforms() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                val exception = assertFailsWith<IllegalArgumentException> {
                    Mesh.make(
                        specification = specification,
                        mode = Mesh.Mode.TRIANGLES,
                        vertexBuffer = vertexBuffer,
                        vertexCount = 6,
                        bounds = BOUNDS
                    )
                }
                assertTrue(!exception.message.isNullOrEmpty(), "Expected the size the uniforms must have")
            }
        }
    }

    @Test
    fun meshRejectsVertexCountOfZero() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                brightnessUniforms(specification, 1f).use { uniforms ->
                    val exception = assertFailsWith<IllegalArgumentException> {
                        Mesh.make(
                            specification = specification,
                            mode = Mesh.Mode.TRIANGLES,
                            vertexBuffer = vertexBuffer,
                            vertexCount = 0,
                            bounds = BOUNDS,
                            uniforms = uniforms
                        )
                    }
                    assertTrue(!exception.message.isNullOrEmpty(), "Expected the reason the mesh was rejected")
                }
            }
        }
    }

    @Test
    fun specificationReportsItsAttributes() {
        makeSpecification().use { specification ->
            assertContentEquals(ATTRIBUTES.toList(), specification.attributes)
            assertEquals(ATTRIBUTES[1], specification.findAttribute("color"))
            assertNull(specification.findAttribute("noSuchAttribute"))
        }
    }

    @Test
    fun specificationReportsTheImplicitPositionVarying() {
        makeSpecification().use { specification ->
            assertEquals(VARYINGS[0], specification.findVarying("color"))
            // The specification was not given a position varying, so Skia added one.
            assertEquals(
                MeshSpecification.Varying(MeshSpecification.Varying.Type.FLOAT2, "position"),
                specification.findVarying("position")
            )
            assertNull(specification.findVarying("noSuchVarying"))
        }
    }

    @Test
    fun specificationReportsWhereEachUniformValueBelongs() {
        makeLayoutSpecification().use { specification ->
            val uniforms = specification.uniforms
            assertEquals(uniforms.size, uniforms.map { it.name }.distinct().size, "Names repeat")
            assertEquals(uniforms.sumOf { it.sizeInBytes }, specification.uniformSize)
            for (uniform in uniforms) {
                assertEquals(uniform, specification.findUniform(uniform.name))
                assertEquals(0, uniform.offset % 4, "'${uniform.name}' is misaligned")
                assertTrue(
                    uniform.offset + uniform.sizeInBytes <= specification.uniformSize,
                    "'${uniform.name}' runs past the end of the uniform data"
                )
            }
            assertNull(specification.findUniform("noSuchUniform"))

            val offset = specification.findUniform("offset")!!
            assertEquals(MeshSpecification.Uniform.Type.FLOAT2, offset.type)
            assertEquals(8, offset.sizeInBytes)
            assertTrue(offset.isVertex, "'offset' is declared by the vertex program")

            // Half precision values are stored at full width, so 'brightness' is four bytes.
            val brightness = specification.findUniform("brightness")!!
            assertEquals(MeshSpecification.Uniform.Type.FLOAT, brightness.type)
            assertEquals(4, brightness.sizeInBytes)
            assertTrue(brightness.isHalfPrecision, "'brightness' is declared half")
            assertTrue(brightness.isFragment, "'brightness' is declared by the fragment program")

            // Values are packed tightly rather than padded out to four components.
            assertEquals(12, specification.findUniform("tintRgb")!!.sizeInBytes)
            assertEquals(36, specification.findUniform("warp")!!.sizeInBytes)

            val steps = specification.findUniform("steps")!!
            assertEquals(MeshSpecification.Uniform.Type.INT2, steps.type)
            assertEquals(8, steps.sizeInBytes)

            val weights = specification.findUniform("weights")!!
            assertTrue(weights.isArray, "'weights' is declared as an array")
            assertEquals(3, weights.count)
            assertEquals(12, weights.sizeInBytes)

            assertTrue(specification.findUniform("tint")!!.isColor, "'tint' is declared layout(color)")
        }
    }

    @Test
    fun specificationWithoutUniformsReportsNone() {
        makeMinimalSpecification().use { specification ->
            assertEquals(0, specification.uniformSize)
            assertEquals(emptyList(), specification.uniforms)
            assertEquals(emptyList(), specification.children)
        }
    }

    @Test
    fun specificationReportsTheChildrenItsProgramsDeclare() {
        makeChildSpecification().use { specification ->
            assertEquals(
                listOf(
                    MeshSpecification.Child("tex", MeshSpecification.Child.Type.SHADER, 0),
                    MeshSpecification.Child("filter", MeshSpecification.Child.Type.COLOR_FILTER, 1),
                    MeshSpecification.Child("blend", MeshSpecification.Child.Type.BLENDER, 2),
                ),
                specification.children
            )
            assertEquals(specification.children[1], specification.findChild("filter"))
            assertNull(specification.findChild("noSuchChild"))
        }
    }

    @Test
    fun specificationCarriesAColorSpaceOnlyWhenItsFragmentProgramOutputsColor() {
        makeSpecification().use { specification ->
            // The fragment program outputs a color and no color space was named, so it is sRGB.
            val colorSpace = assertNotNull(specification.colorSpace)
            colorSpace.use { assertTrue(it.isSRGB) }
        }
        makeMinimalSpecification().use { specification ->
            assertNull(specification.colorSpace, "A fragment program without color needs no color space")
        }
    }

    @Test
    fun meshReportsWhatItWasMadeFrom() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                brightnessUniforms(specification, 1f).use { uniforms ->
                    Mesh.make(
                        specification = specification,
                        mode = Mesh.Mode.TRIANGLE_STRIP,
                        vertexBuffer = vertexBuffer,
                        vertexCount = 4,
                        bounds = BOUNDS,
                        vertexOffset = VERTEX_STRIDE,
                        uniforms = uniforms
                    ).use { mesh ->
                        assertEquals(Mesh.Mode.TRIANGLE_STRIP, mesh.mode, "mode")
                        assertEquals(4, mesh.vertexCount, "vertexCount")
                        assertEquals(VERTEX_STRIDE, mesh.vertexOffset, "vertexOffset")
                        assertEquals(BOUNDS, mesh.bounds, "bounds")
                        assertNull(mesh.indexBuffer, "The mesh is not indexed")
                        assertEquals(0, mesh.indexCount, "indexCount of a mesh that is not indexed")
                        assertEquals(0, mesh.indexOffset, "indexOffset of a mesh that is not indexed")
                        mesh.specification.use { assertEquals(VERTEX_STRIDE, it.stride, "specification") }
                        mesh.vertexBuffer.use { assertEquals(vertexBuffer.size, it.size, "vertexBuffer") }
                        mesh.uniforms.use { assertContentEquals(uniforms.bytes, it.bytes, "uniforms") }
                    }
                }
            }
        }
    }

    @Test
    fun indexedMeshReportsItsIndexBuffer() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_CORNERS)).use { vertexBuffer ->
                MeshIndexBuffer.make(QUAD_INDICES).use { indexBuffer ->
                    brightnessUniforms(specification, 1f).use { uniforms ->
                        Mesh.makeIndexed(
                            specification = specification,
                            mode = Mesh.Mode.TRIANGLES,
                            vertexBuffer = vertexBuffer,
                            vertexCount = 4,
                            indexBuffer = indexBuffer,
                            indexCount = QUAD_INDICES.size,
                            bounds = BOUNDS,
                            uniforms = uniforms
                        ).use { mesh ->
                            assertEquals(QUAD_INDICES.size, mesh.indexCount, "indexCount")
                            assertEquals(0, mesh.indexOffset, "indexOffset")
                            assertNotNull(mesh.indexBuffer).use {
                                assertEquals(indexBuffer.size, it.size)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun meshBindsShaderColorFilterAndBlenderChildren() {
        makeChildSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                Shader.makeColor(Color.RED).use { shader ->
                    ColorFilter.makeBlend(Color.GREEN, BlendMode.SRC_OVER)!!.use { colorFilter ->
                        Blender.makeMode(BlendMode.SRC).use { blender ->
                            Mesh.make(
                                specification = specification,
                                mode = Mesh.Mode.TRIANGLES,
                                vertexBuffer = vertexBuffer,
                                vertexCount = 6,
                                bounds = BOUNDS,
                                children = arrayOf(shader, colorFilter, blender)
                            ).use { mesh ->
                                assertEquals(1, recordedOpCount { canvas ->
                                    canvas.drawMesh(mesh, BlendMode.SRC, Paint())
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun meshRejectsAChildOfTheWrongKind() {
        makeChildSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                Blender.makeMode(BlendMode.SRC).use { blender ->
                    val exception = assertFailsWith<IllegalArgumentException> {
                        Mesh.make(
                            specification = specification,
                            mode = Mesh.Mode.TRIANGLES,
                            vertexBuffer = vertexBuffer,
                            vertexCount = 6,
                            bounds = BOUNDS,
                            // 'tex' is declared as a shader.
                            children = arrayOf(blender, null, null)
                        )
                    }
                    assertTrue(!exception.message.isNullOrEmpty(), "Expected the reason the child was rejected")
                }
            }
        }
    }

    @Test
    fun vertexBufferFilledFromFloatsDrawsTheSameMesh() {
        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexFloats(QUAD_TRIANGLES)).use { vertexBuffer ->
                assertEquals(6 * VERTEX_STRIDE, vertexBuffer.size)
                brightnessUniforms(specification, 1f).use { uniforms ->
                    // Skia checks the buffer against the stride and the vertex count, so a mesh it
                    // accepts is one the floats were laid out correctly for.
                    Mesh.make(
                        specification = specification,
                        mode = Mesh.Mode.TRIANGLES,
                        vertexBuffer = vertexBuffer,
                        vertexCount = 6,
                        bounds = BOUNDS,
                        uniforms = uniforms
                    ).use { mesh ->
                        assertEquals(1, recordedOpCount { canvas ->
                            canvas.drawMesh(mesh, BlendMode.SRC, Paint())
                        })
                    }
                }
            }
        }
    }

    @Test
    fun rasterizesMeshOnAGpuSurface() {
        if (!TestGlContext.isAvailable()) return

        makeSpecification().use { specification ->
            MeshVertexBuffer.make(vertexData(QUAD_TRIANGLES)).use { vertexBuffer ->
                brightnessUniforms(specification, 1f).use { uniforms ->
                    Mesh.make(
                        specification = specification,
                        mode = Mesh.Mode.TRIANGLES,
                        vertexBuffer = vertexBuffer,
                        vertexCount = 6,
                        bounds = BOUNDS,
                        uniforms = uniforms
                    ).use { mesh ->
                        val pixels = TestGlContext.run {
                            DirectContext.makeGL().useContext { context ->
                                Surface.makeRenderTarget(
                                    context,
                                    budgeted = false,
                                    ImageInfo.makeN32Premul(SURFACE_SIZE, SURFACE_SIZE)
                                ).use { surface ->
                                    surface.canvas.clear(Color.WHITE)
                                    Paint().use { paint ->
                                        surface.canvas.drawMesh(mesh, BlendMode.SRC, paint)
                                    }
                                    surface.makeImageSnapshot().use { Bitmap.makeFromImage(it, context) }
                                }
                            }
                        }
                        pixels.use {
                            val inside = it.getColor(SURFACE_SIZE / 2, SURFACE_SIZE / 2)
                            assertNotEquals(Color.WHITE, inside, "The mesh left the surface unchanged")
                            assertEquals(0xFF, Color.getA(inside), "Expected an opaque mesh pixel")
                            assertEquals(Color.WHITE, it.getColor(0, 0), "Drew outside the mesh bounds")
                        }
                    }
                }
            }
        }
    }
}

private const val SURFACE_SIZE = 16

// The quad is inset far enough that the corners of the surface stay outside it.
private const val QUAD_MIN = 2f
private const val QUAD_MAX = 14f

private val BOUNDS = Rect.makeLTRB(0f, 0f, SURFACE_SIZE.toFloat(), SURFACE_SIZE.toFloat())

/** Two triangles in buffer order. */
private val QUAD_TRIANGLES = floatArrayOf(
    QUAD_MIN, QUAD_MIN, QUAD_MAX, QUAD_MIN, QUAD_MIN, QUAD_MAX,
    QUAD_MAX, QUAD_MIN, QUAD_MAX, QUAD_MAX, QUAD_MIN, QUAD_MAX,
)

/** The same quad as four corners, for the indexed mesh. */
private val QUAD_CORNERS = floatArrayOf(
    QUAD_MIN, QUAD_MIN, QUAD_MAX, QUAD_MIN, QUAD_MIN, QUAD_MAX, QUAD_MAX, QUAD_MAX,
)

private val QUAD_INDICES = shortArrayOf(0, 1, 2, 1, 3, 2)

// The fragment program declares "base" before "brightness" and uniforms are packed in declaration
// order, so "brightness" sits at a non-zero offset in the buffer the builder produces.
private const val BASE = "base"
private const val BRIGHTNESS = "brightness"

/** Eight bytes larger than the attributes it carries, so that the stride is not the packed size. */
private const val VERTEX_STRIDE = 32

private val ATTRIBUTES = arrayOf(
    MeshSpecification.Attribute(MeshSpecification.Attribute.Type.FLOAT2, 0, "pos"),
    MeshSpecification.Attribute(MeshSpecification.Attribute.Type.FLOAT4, 8, "color"),
)

private val VARYINGS = arrayOf(
    MeshSpecification.Varying(MeshSpecification.Varying.Type.FLOAT4, "color"),
)

private const val VERTEX_SHADER = """
    Varyings main(const Attributes a) {
        Varyings v;
        v.position = a.pos;
        v.color = a.color;
        return v;
    }
"""

private const val FRAGMENT_SHADER = """
    uniform float base;
    uniform float brightness;
    float2 main(const Varyings v, out float4 color) {
        color = float4(v.color.rgb * brightness + base, v.color.a);
        return v.position;
    }
"""

private fun makeSpecification(
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = FRAGMENT_SHADER,
    vertexStride: Int = VERTEX_STRIDE
): MeshSpecification = MeshSpecification.make(
    attributes = ATTRIBUTES,
    vertexStride = vertexStride,
    varyings = VARYINGS,
    vertexShader = vertexShader,
    fragmentShader = fragmentShader
    // The fragment program has a color out parameter, so Skia needs a color space: the default
    // stands in for the sRGB one it would otherwise reject the specification for missing.
)

private fun brightnessUniforms(specification: MeshSpecification, brightness: Float): Data =
    MeshUniformBuilder(specification).use { builder ->
        builder.uniform(BRIGHTNESS, brightness)
        builder.build()
    }

/**
 * Lays out [positions], a sequence of x and y pairs, as vertices of [VERTEX_STRIDE] bytes: the
 * position, an opaque red color, and padding the specification does not read.
 */
private fun vertexFloats(positions: FloatArray): FloatArray {
    val floatsPerVertex = VERTEX_STRIDE / 4
    val floats = FloatArray(positions.size / 2 * floatsPerVertex)
    for (vertex in 0 until positions.size / 2) {
        val base = vertex * floatsPerVertex
        floats[base] = positions[vertex * 2]
        floats[base + 1] = positions[vertex * 2 + 1]
        floats[base + 2] = 1f  // red
        floats[base + 5] = 1f  // alpha
    }
    return floats
}

private fun vertexData(positions: FloatArray): ByteArray = vertexFloats(positions).toBytes()

/** A specification declaring one uniform of every shape the reflection has to describe. */
private fun makeLayoutSpecification(): MeshSpecification = MeshSpecification.make(
    attributes = ATTRIBUTES,
    vertexStride = VERTEX_STRIDE,
    varyings = VARYINGS,
    vertexShader = """
        uniform float2 offset;
        Varyings main(const Attributes a) {
            Varyings v;
            v.position = a.pos + offset;
            v.color = a.color;
            return v;
        }
    """,
    fragmentShader = """
        uniform half brightness;
        uniform float3 tintRgb;
        uniform float3x3 warp;
        uniform int2 steps;
        uniform float weights[3];
        layout(color) uniform float4 tint;
        float2 main(const Varyings v, out float4 color) {
            float3 warped = warp * tintRgb;
            float weighted = weights[0] + weights[1] + weights[2] + float(steps.x + steps.y);
            color = float4(warped * brightness + weighted, 1) * tint;
            return v.position;
        }
    """
)

/** A specification declaring one child of every kind, in the order they are reported. */
private fun makeChildSpecification(): MeshSpecification = MeshSpecification.make(
    attributes = ATTRIBUTES,
    vertexStride = VERTEX_STRIDE,
    varyings = VARYINGS,
    vertexShader = VERTEX_SHADER,
    fragmentShader = """
        uniform shader tex;
        uniform colorFilter filter;
        uniform blender blend;
        float2 main(const Varyings v, out float4 color) {
            color = blend.eval(filter.eval(tex.eval(v.position)), v.color);
            return v.position;
        }
    """
)

/** A specification with no uniforms and no children, whose fragment program returns no color. */
private fun makeMinimalSpecification(): MeshSpecification = MeshSpecification.make(
    attributes = ATTRIBUTES,
    vertexStride = VERTEX_STRIDE,
    varyings = VARYINGS,
    vertexShader = VERTEX_SHADER,
    fragmentShader = "float2 main(const Varyings v) { return v.position; }"
)

private fun recordedOpCount(draw: (Canvas) -> Unit): Int = PictureRecorder().use { recorder ->
    draw(recorder.beginRecording(BOUNDS))
    recorder.finishRecordingAsPicture().use { it.approximateOpCount }
}

private fun drawOnRasterSurface(draw: (Canvas) -> Unit): Bitmap =
    Surface.makeRasterN32Premul(SURFACE_SIZE, SURFACE_SIZE).use { surface ->
        surface.canvas.clear(Color.WHITE)
        draw(surface.canvas)
        surface.makeImageSnapshot().use { Bitmap.makeFromImage(it) }
    }

/** Skia reads buffers and uniform data in the byte order of the host, which is little-endian. */
private fun FloatArray.toBytes(): ByteArray {
    val bytes = ByteArray(size * 4)
    for (i in indices) {
        val bits = this[i].toRawBits()
        bytes[i * 4] = bits.toByte()
        bytes[i * 4 + 1] = (bits ushr 8).toByte()
        bytes[i * 4 + 2] = (bits ushr 16).toByte()
        bytes[i * 4 + 3] = (bits ushr 24).toByte()
    }
    return bytes
}

private fun ByteArray.floatAt(index: Int): Float {
    var bits = 0
    for (byte in 3 downTo 0) {
        bits = (bits shl 8) or (this[index * 4 + byte].toInt() and 0xFF)
    }
    return Float.fromBits(bits)
}
