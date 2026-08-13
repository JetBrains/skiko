package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.TestGlContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MeshTest {
    private val vertexShader = """
        Varyings main(const Attributes attributes) {
            Varyings varyings;
            varyings.position = attributes.position;
            return varyings;
        }
    """.trimIndent()

    private val fragmentShader = """
        uniform float4 meshColor;

        float2 main(const Varyings varyings, out float4 color) {
            color = meshColor;
            return varyings.position;
        }
    """.trimIndent()

    @Test
    fun drawMeshWithUniform() {
        if (!TestGlContext.isAvailable()) return

        val specification = MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(
                    MeshSpecification.AttributeType.FLOAT2,
                    0,
                    "position"
                )
            ),
            8,
            emptyArray(),
            vertexShader,
            fragmentShader
        )

        specification.use {
            Mesh(
                specification,
                VertexMode.TRIANGLES,
                floatArrayOf(
                    1f, 1f,
                    7f, 1f,
                    1f, 7f
                ),
                3,
                Rect.makeWH(8f, 8f)
            ).use { mesh ->
                mesh.setFloatUniform("meshColor", 1f, 0f, 0f, 1f)

                val bitmap = TestGlContext.run {
                    DirectContext.makeGL().useContext { context ->
                        Surface.makeRenderTarget(
                            context,
                            budgeted = false,
                            ImageInfo.makeN32Premul(8, 8)
                        ).use { surface ->
                            Paint().use { paint ->
                                surface.canvas.drawMesh(mesh, BlendMode.DST, paint)
                            }
                            Bitmap.makeFromImage(surface.makeImageSnapshot(), context)
                        }
                    }
                }
                bitmap.use {
                    assertEquals(Color.RED, bitmap.getColor(2, 2))
                    assertEquals(Color.TRANSPARENT, bitmap.getColor(7, 7))
                }
            }
        }
    }

    @Test
    fun uniformSetterValidatesNameTypeAndSize() {
        MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(
                    MeshSpecification.AttributeType.FLOAT2,
                    0,
                    "position"
                )
            ),
            8,
            emptyArray(),
            vertexShader,
            fragmentShader
        ).use { specification ->
            Mesh(
                specification,
                VertexMode.TRIANGLES,
                floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f),
                3,
                Rect.makeWH(1f, 1f)
            ).use { mesh ->
                assertFailsWith<IllegalArgumentException> {
                    mesh.setFloatUniform("missing", 1f)
                }
                assertFailsWith<IllegalArgumentException> {
                    mesh.setIntUniform("meshColor", 1, 2, 3, 4)
                }
                assertFailsWith<IllegalArgumentException> {
                    mesh.setFloatUniform("meshColor", 1f, 0f, 0f)
                }
            }
        }
    }

    @Test
    fun rejectsUnsupportedTriangleFan() {
        MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(
                    MeshSpecification.AttributeType.FLOAT2,
                    0,
                    "position"
                )
            ),
            8,
            emptyArray(),
            vertexShader,
            fragmentShader
        ).use { specification ->
            assertFailsWith<IllegalArgumentException> {
                Mesh(
                    specification,
                    VertexMode.TRIANGLE_FAN,
                    floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f),
                    3,
                    Rect.makeWH(1f, 1f)
                )
            }
        }
    }

    @Test
    fun indexedMeshAndColorUniform() {
        val colorFragmentShader = """
            layout(color) uniform float4 tint;

            float2 main(const Varyings varyings, out float4 color) {
                color = tint;
                return varyings.position;
            }
        """.trimIndent()
        MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(
                    MeshSpecification.AttributeType.FLOAT2,
                    0,
                    "position"
                )
            ),
            8,
            emptyArray(),
            vertexShader,
            colorFragmentShader,
            ColorSpace.sRGB
        ).use { specification ->
            Mesh(
                specification,
                VertexMode.TRIANGLES,
                floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f),
                3,
                shortArrayOf(0, 1, 2),
                Rect.makeWH(1f, 1f)
            ).use { mesh ->
                mesh.setColorUniform("tint", Color.RED)
                assertFailsWith<IllegalArgumentException> {
                    mesh.setColorUniform("missing", Color.RED)
                }
            }
        }
    }

    @Test
    fun childSetterValidatesChildType() {
        val childFragmentShader = """
            uniform shader content;

            float2 main(const Varyings varyings, out float4 color) {
                color = content.eval(varyings.position);
                return varyings.position;
            }
        """.trimIndent()
        MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(
                    MeshSpecification.AttributeType.FLOAT2,
                    0,
                    "position"
                )
            ),
            8,
            emptyArray(),
            vertexShader,
            childFragmentShader
        ).use { specification ->
            Mesh(
                specification,
                VertexMode.TRIANGLES,
                floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f),
                3,
                Rect.makeWH(1f, 1f)
            ).use { mesh ->
                Shader.makeColor(Color.BLUE).use { shader ->
                    mesh.child("content", shader)
                }
                Blender.makeMode(BlendMode.SRC).use { blender ->
                    assertFailsWith<IllegalArgumentException> {
                        mesh.child("content", blender)
                    }
                }
            }
        }
    }

    @Test
    fun specificationValidatesLimitsAndShaders() {
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.Attribute(
                MeshSpecification.AttributeType.FLOAT,
                -1,
                "value"
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.make(
                emptyArray(),
                8,
                emptyArray(),
                vertexShader,
                fragmentShader
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.make(
                arrayOf(
                    MeshSpecification.Attribute(
                        MeshSpecification.AttributeType.FLOAT2,
                        0,
                        "position"
                    )
                ),
                6,
                emptyArray(),
                vertexShader,
                fragmentShader
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.make(
                arrayOf(
                    MeshSpecification.Attribute(
                        MeshSpecification.AttributeType.FLOAT,
                        2,
                        "value"
                    )
                ),
                8,
                emptyArray(),
                vertexShader,
                fragmentShader
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.make(
                arrayOf(
                    MeshSpecification.Attribute(
                        MeshSpecification.AttributeType.FLOAT2,
                        0,
                        "position"
                    )
                ),
                8,
                emptyArray(),
                vertexShader,
                fragmentShader,
                ColorSpace.sRGB,
                ColorAlphaType.UNKNOWN
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.make(
                arrayOf(
                    MeshSpecification.Attribute(
                        MeshSpecification.AttributeType.FLOAT2,
                        0,
                        "position"
                    )
                ),
                MeshSpecification.MAX_STRIDE + 1,
                emptyArray(),
                vertexShader,
                fragmentShader
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MeshSpecification.make(
                arrayOf(
                    MeshSpecification.Attribute(
                        MeshSpecification.AttributeType.FLOAT2,
                        0,
                        "position"
                    )
                ),
                8,
                emptyArray(),
                "not valid SkSL",
                fragmentShader
            )
        }
    }

    @Test
    fun specificationSupportsExplicitVaryings() {
        val varyingVertexShader = """
            Varyings main(const Attributes attributes) {
                Varyings varyings;
                varyings.position = attributes.position;
                varyings.uv = attributes.position;
                return varyings;
            }
        """.trimIndent()
        val varyingFragmentShader = """
            float2 main(const Varyings varyings) {
                return varyings.uv;
            }
        """.trimIndent()
        MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(
                    MeshSpecification.AttributeType.FLOAT2,
                    0,
                    "position"
                )
            ),
            8,
            arrayOf(
                MeshSpecification.Varying(
                    MeshSpecification.VaryingType.FLOAT2,
                    "uv"
                )
            ),
            varyingVertexShader,
            varyingFragmentShader
        ).use {}
    }
}
