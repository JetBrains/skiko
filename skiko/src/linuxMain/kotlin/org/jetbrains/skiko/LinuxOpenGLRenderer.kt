@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.BackendTexture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.GLAssembledInterface
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.makeGLWithInterface
import org.jetbrains.skia.impl.Native.Companion.NullPointer

internal class LinuxOpenGLRenderer(
    private val component: LinuxSkiaLayerComponent,
    private val properties: SkiaLayerProperties,
) : LinuxLayerRenderer {
    override val renderApi: GraphicsApi = GraphicsApi.OPENGL

    private val openGlContext =
        run {
            component.configureGpuPriority(properties.adapterPriority)
            component.configureFrameBuffering(properties.frameBuffering)
            component.createOpenGlContext()
        }.also {
            if (it == NullPointer) throw RenderException("Could not create an OpenGL context")
        }

    private lateinit var openGlInterface: GLAssembledInterface
    private lateinit var directContext: DirectContext

    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var width = 0
    private var height = 0
    private var closed = false
    private val frameLimiter = LinuxFrameLimiter()

    override var deviceName: String? = null
        private set

    override var description: String = "Skia Ganesh/OpenGL"
        private set

    init {
        try {
            component.makeOpenGlContextCurrent(openGlContext)
            openGlInterface =
                GLAssembledInterface.createFromNativePointers(
                    ctxPtr = component.openGlResolverContext,
                    fPtr = component.openGlResolver,
                )
            directContext = DirectContext.makeGLWithInterface(openGlInterface)
            if (properties.gpuResourceCacheLimit >= 0L) {
                directContext.resourceCacheLimit = properties.gpuResourceCacheLimit
            }
            deviceName = component.openGlRendererName()
            if (!isOpenGlAdapterSupported(deviceName)) {
                throw RenderException("OpenGL adapter is not supported: $deviceName")
            }
            description =
                if (deviceName.isNullOrBlank()) "Skia Ganesh/OpenGL"
                else "Skia Ganesh/OpenGL ($deviceName)"
        } catch (failure: Throwable) {
            if (::directContext.isInitialized) directContext.close()
            if (::openGlInterface.isInitialized) openGlInterface.close()
            component.deleteOpenGlContext(openGlContext)
            throw if (failure is RenderException) failure
            else RenderException("Could not initialize the OpenGL renderer", failure)
        }
    }

    override fun render(
        width: Int,
        height: Int,
        waitForVsync: Boolean,
        block: (Canvas) -> Unit,
    ) {
        makeCurrent()
        val shouldWaitForVsync = properties.isVsyncEnabled && waitForVsync
        val platformVsyncEnabled =
            component.setOpenGlSwapInterval(if (shouldWaitForVsync) 1 else 0)
        if (
            shouldWaitForVsync &&
                !platformVsyncEnabled &&
                properties.isVsyncFramelimitFallbackEnabled
        ) {
            frameLimiter.awaitNextFrame(component.displayRefreshRate)
        }
        ensureSurface(width, height)
        val skiaSurface = checkNotNull(surface)
        skiaSurface.canvas.clear(if (component.transparency) Color.TRANSPARENT else Color.BLACK)
        block(skiaSurface.canvas)
        skiaSurface.flushAndSubmit()
        component.swapOpenGlBuffers()
    }

    fun makeCurrent() {
        check(!closed) { "Renderer is closed" }
        component.makeOpenGlContextCurrent(openGlContext)
    }

    override fun <T> withExternalOpenGl(block: () -> T): T {
        makeCurrent()
        surface?.flushAndSubmit()
        return try {
            block()
        } finally {
            directContext.resetGLAll()
        }
    }

    override fun drawTexture(textureId: Int, width: Int, height: Int, canvas: Canvas) {
        require(textureId != 0 && width > 0 && height > 0)
        makeCurrent()
        val texture =
            BackendTexture.makeGL(
                width = width,
                height = height,
                isMipmapped = false,
                textureId = textureId,
                textureTarget = GL_TEXTURE_2D,
                textureFormat = GL_RGBA8,
            )
        val image =
            try {
                Image.borrowTextureFrom(
                    context = directContext,
                    backendTexture = texture,
                    origin = SurfaceOrigin.BOTTOM_LEFT,
                    colorType = ColorType.RGBA_8888,
                    alphaType = ColorAlphaType.PREMUL,
                )
            } catch (failure: Throwable) {
                texture.close()
                throw failure
            }
        try {
            canvas.drawImageRect(image, Rect.makeWH(width.toFloat(), height.toFloat()), null)
        } finally {
            image.close()
            texture.close()
        }
    }

    override fun snapshot(width: Int, height: Int): Bitmap {
        makeCurrent()
        ensureSurface(width, height)
        val bitmap = Bitmap()
        check(bitmap.allocPixels(ImageInfo.makeN32(width, height, ColorAlphaType.PREMUL))) {
            "Could not allocate a Skia screenshot bitmap"
        }
        if (!checkNotNull(surface).readPixels(bitmap, 0, 0)) {
            bitmap.close()
            error("Could not read pixels from the Skia window surface")
        }
        return bitmap
    }

    private fun ensureSurface(newWidth: Int, newHeight: Int) {
        require(newWidth > 0 && newHeight > 0)
        if (surface != null && width == newWidth && height == newHeight) return

        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null

        width = newWidth
        height = newHeight
        renderTarget =
            BackendRenderTarget.makeGL(
                width = width,
                height = height,
                sampleCnt = 0,
                stencilBits = 8,
                fbId = 0,
                fbFormat = FramebufferFormat.GR_GL_RGBA8,
            )
        surface =
            Surface.makeFromBackendRenderTarget(
                context = directContext,
                rt = checkNotNull(renderTarget),
                origin = SurfaceOrigin.BOTTOM_LEFT,
                colorFormat = SurfaceColorFormat.RGBA_8888,
                colorSpace = ColorSpace.sRGB,
                surfaceProps = SurfaceProps(pixelGeometry = component.pixelGeometry),
            ) ?: throw RenderException("Skia could not wrap the OpenGL framebuffer")
    }

    override fun isContextLost(): Boolean =
        closed || component.isOpenGlContextLost(openGlContext)

    override fun close(contextLost: Boolean) {
        if (closed) return
        if (contextLost) {
            directContext.abandon()
        } else {
            makeCurrent()
        }
        closed = true
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
        directContext.close()
        openGlInterface.close()
        component.deleteOpenGlContext(openGlContext)
    }

    private companion object {
        const val GL_TEXTURE_2D = 0x0DE1
        const val GL_RGBA8 = 0x8058
    }
}

private fun isOpenGlAdapterSupported(deviceName: String?): Boolean {
    if (deviceName == null || SkikoProperties.allowSoftwareOpenGlAdapter) return true
    val normalized = deviceName.lowercase()
    return "llvmpipe" !in normalized && "virgl" !in normalized
}
