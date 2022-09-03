package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.context.WindowsOpenGLContextHandler
import platform.windows.*

internal class WindowsOpenGLRedrawer(
    private val skiaLayer: SkiaLayer
) : Redrawer {

    private val contextHandler = WindowsOpenGLContextHandler(skiaLayer)

    private val dc: HDC = GetDC(skiaLayer.window) ?: throw Error("Failed to get DC")
    private val context: HGLRC

    override val renderInfo: String
        get() = contextHandler.rendererInfo()

    init {
        memScoped {
            val descriptor = alloc<PIXELFORMATDESCRIPTOR>().apply {
                nSize = sizeOf<PIXELFORMATDESCRIPTOR>().toUShort()
                nVersion = 1u
                dwFlags =
                    (PFD_DRAW_TO_WINDOW or PFD_DRAW_TO_BITMAP or PFD_SUPPORT_OPENGL or PFD_GENERIC_ACCELERATED or PFD_DOUBLEBUFFER or PFD_SWAP_LAYER_BUFFERS).toUInt()
                iPixelType = PFD_TYPE_RGBA.toUByte()
                cColorBits = 32u
                cRedBits = 8u
                cGreenBits = 8u
                cBlueBits = 8u
                cAlphaBits = 8u
                cDepthBits = 32u
                cStencilBits = 8u
            }
            val pixelFormat = ChoosePixelFormat(dc, descriptor.ptr)
            SetPixelFormat(dc, pixelFormat, descriptor.ptr)
        }
        context = wglCreateContext(dc) ?: throw Error("Failed to create context")
        wglMakeCurrent(dc, context)
        skiaLayer.onWMPaint = { nanoTime ->
            skiaLayer.update(nanoTime)
            contextHandler.draw()
            SwapBuffers(dc)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val frameDispatcher = FrameDispatcher(newSingleThreadContext("skiko-opengl-redrawer-dispatcher")) {
        redrawImmediately()
    }

    override fun dispose() {
        contextHandler.dispose()
        wglDeleteContext(context)
        ReleaseDC(skiaLayer.window, dc)
        skiaLayer.onWMPaint = {}
    }

    override fun syncSize() {
        memScoped {
            val rect = alloc<RECT>()
            GetClientRect(skiaLayer.window, rect.ptr)
            val width = rect.right - rect.left
            val height = rect.bottom - rect.top
            skiaLayer.size = width to height
        }
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        SendMessageA(skiaLayer.window, WM_PAINT, 0, 0)

    }
}