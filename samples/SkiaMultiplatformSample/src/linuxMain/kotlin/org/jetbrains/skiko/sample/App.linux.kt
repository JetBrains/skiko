@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko.sample

import kgfw.Event
import kgfw.window
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.*
import org.jetbrains.skiko.GraphicsApi
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime

const val SCREEN_WIDTH = 800
const val SCREEN_HEIGHT = 450

@OptIn(ExperimentalTime::class)
fun main() {
    println("[LinuxApp] Starting main()")
    var mouseX = 0.0
    var mouseY = 0.0

    // Skia app logic: we bypass SkiaLayer/SkiaLayerRenderDelegate on Linux and render directly
    val app = object : Clocks({ GraphicsApi.OPENGL }) {}

    var context: DirectContext? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null

    window(
        name = "Skiko example",
        width = SCREEN_WIDTH,
        height = SCREEN_HEIGHT,
        onEvent = { event ->
            when (event) {
                is Event.MousePosChanged -> {
                    mouseX = event.x.toDouble()
                    mouseY = event.y.toDouble()
                }
                else -> { /* ignore */ }
            }
        }
    ) {
        try {
            if (context == null) {
                println("[LinuxApp] Creating DirectContext...")
                context = DirectContext.makeGL()
                println("[LinuxApp] DirectContext created: ${'$'}{context != null}")
            }
            if (renderTarget == null || surface == null) {
                println("[LinuxApp] Creating BackendRenderTarget and Surface...")
                // Use the default framebuffer (id = 0) provided by the window
                renderTarget = BackendRenderTarget.makeGL(
                    width = SCREEN_WIDTH,
                    height = SCREEN_HEIGHT,
                    sampleCnt = 0,
                    stencilBits = 8,
                    fbId = 0, // default framebuffer
                    fbFormat = FramebufferFormat.GR_GL_RGBA8
                )
                surface = Surface.makeFromBackendRenderTarget(
                    context = context,
                    rt = renderTarget,
                    origin = SurfaceOrigin.BOTTOM_LEFT,
                    colorFormat = SurfaceColorFormat.RGBA_8888,
                    colorSpace = ColorSpace.sRGB,
                    surfaceProps = SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
                ) ?: error("Cannot create Skia surface")
                println("[LinuxApp] Surface created: ${'$'}{surface != null}")
            }

            val canvas = surface.canvas
            canvas.clear(Color.WHITE)

            app.xpos = mouseX
            app.ypos = mouseY

            val nanoTime = now().toEpochMilliseconds() * 1_000_000L
            app.onRender(canvas, SCREEN_WIDTH, SCREEN_HEIGHT, nanoTime)

            surface.flushAndSubmit(syncCpu = true)
        } catch (t: Throwable) {
            println("[LinuxApp] ERROR: ${'$'}t")
            t.printStackTrace()
        }
    }
}
