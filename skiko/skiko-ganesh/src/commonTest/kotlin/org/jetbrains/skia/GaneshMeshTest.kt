@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skia

import org.jetbrains.skia.gpu.ganesh.makeFromImage
import org.jetbrains.skia.gpu.ganesh.makeRenderTarget
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.Arch
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.TestGlContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GaneshMeshTest {
    @Test
    fun rasterizesMeshOnAGpuSurface() {
        if (!TestGlContext.isAvailable()) return

        if (hostOs == OS.Linux && kotlinBackend == KotlinBackend.Native && hostArch == Arch.Arm64) {
            // TODO: fix test on Linux arm64 using EGL
            return
        }

        makeSpecification().use { specification ->
            MeshVertexBuffer.make(QUAD_VERTICES).use { vertexBuffer ->
                Mesh.make(
                    specification = specification,
                    mode = Mesh.Mode.TRIANGLES,
                    vertexBuffer = vertexBuffer,
                    vertexCount = 6,
                    bounds = Rect.makeLTRB(0f, 0f, SURFACE_SIZE.toFloat(), SURFACE_SIZE.toFloat()),
                    uniforms = null
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

private const val SURFACE_SIZE = 16

private const val QUAD_MIN = 2f
private const val QUAD_MAX = 14f

/** Two triangles covering a quad inset from the edges of the surface. */
private val QUAD_VERTICES = floatArrayOf(
    QUAD_MIN, QUAD_MIN, QUAD_MAX, QUAD_MIN, QUAD_MIN, QUAD_MAX,
    QUAD_MAX, QUAD_MIN, QUAD_MAX, QUAD_MAX, QUAD_MIN, QUAD_MAX,
)

private fun makeSpecification(): MeshSpecification = MeshSpecification.make(
    attributes = arrayOf(MeshSpecification.Attribute(MeshSpecification.Attribute.Type.FLOAT2, 0, "pos")),
    vertexStride = 8,
    varyings = emptyArray(),
    vertexShader = """
        Varyings main(const Attributes a) {
            Varyings v;
            v.position = a.pos;
            return v;
        }
    """,
    fragmentShader = """
        float2 main(const Varyings v, out float4 color) {
            color = float4(1, 0, 0, 1);
            return v.position;
        }
    """
)
