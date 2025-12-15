@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.X11SkikoWindow
import org.jetbrains.skiko.currentNanoTime

internal class X11VulkanRedrawer(
    private val layer: SkiaLayer,
) : Redrawer {
    private val window: X11SkikoWindow = requireNotNull(layer.x11Window) {
        "X11VulkanRedrawer requires SkiaLayer to be attached to X11SkikoWindow"
    }

    private val presenterHandle: NativePointer = VulkanX11Presenter_create(
        displayPtr = window.display.rawValue,
        window = window.window,
    ).also {
        require(it != NullPointer) { "Failed to create Vulkan presenter" }
    }

    private var bufferWidth: Int = 0
    private var bufferHeight: Int = 0
    private var colorType: ColorType = ColorType.BGRA_8888

    private var pixels: ByteArray = ByteArray(0)
    private var pinnedPixels: Pinned<ByteArray> = pixels.pin()
    private var surface: Surface? = null

    override val renderInfo: String = "Native Vulkan (X11 + CPU upload)"

    override fun dispose() {
        surface?.close()
        surface = null

        pinnedPixels.unpin()
        pixels = ByteArray(0)
        pinnedPixels = pixels.pin()

        VulkanX11Presenter_destroy(presenterHandle)
    }

    override fun needRender(throttledToVsync: Boolean) {
        window.requestRender(throttledToVsync)
    }

    override fun syncBounds() {
        // Presenter handles swapchain recreation during present().
    }

    override fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    override fun renderImmediately() {
        val w = window.width.coerceAtLeast(1)
        val h = window.height.coerceAtLeast(1)

        val desiredColorType = when (VulkanX11Presenter_getPixelFormat(presenterHandle)) {
            0 -> ColorType.BGRA_8888
            else -> ColorType.RGBA_8888
        }

        if (w != bufferWidth || h != bufferHeight || desiredColorType != colorType || surface == null) {
            bufferWidth = w
            bufferHeight = h
            colorType = desiredColorType

            surface?.close()
            surface = null

            pinnedPixels.unpin()
            pixels = ByteArray(w * h * 4)
            pinnedPixels = pixels.pin()

            val rowBytes = w * 4
            val imageInfo = ImageInfo(
                width = w,
                height = h,
                colorType = colorType,
                alphaType = ColorAlphaType.OPAQUE,
                colorSpace = ColorSpace.sRGB,
            )
            surface = Surface.makeRasterDirect(imageInfo, pinnedPixels.addressOf(0).rawValue, rowBytes)
        }

        val surface = surface ?: return
        layer.update(currentNanoTime())

        val canvas = surface.canvas
        canvas.clear(0xFFFFFFFF.toInt())
        layer.draw(canvas)
        surface.flushAndSubmit()

        VulkanX11Presenter_present(
            presenterHandle,
            pinnedPixels.addressOf(0).rawValue,
            w,
            h,
            w * 4,
        )
            .also { result ->
                if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR && result != VK_ERROR_OUT_OF_DATE_KHR) {
                    throw RenderException("Vulkan present failed: ${vkResultToString(result)}")
                }
            }
    }
}

private const val VK_SUCCESS = 0
private const val VK_SUBOPTIMAL_KHR = 1000001003
private const val VK_ERROR_OUT_OF_DATE_KHR = -1000001004

private fun vkResultToString(code: Int): String = when (code) {
    VK_SUCCESS -> "VK_SUCCESS (0)"
    VK_SUBOPTIMAL_KHR -> "VK_SUBOPTIMAL_KHR ($VK_SUBOPTIMAL_KHR)"
    VK_ERROR_OUT_OF_DATE_KHR -> "VK_ERROR_OUT_OF_DATE_KHR ($VK_ERROR_OUT_OF_DATE_KHR)"
    -1 -> "VK_ERROR_OUT_OF_HOST_MEMORY (-1)"
    -2 -> "VK_ERROR_OUT_OF_DEVICE_MEMORY (-2)"
    -3 -> "VK_ERROR_INITIALIZATION_FAILED (-3)"
    -4 -> "VK_ERROR_DEVICE_LOST (-4)"
    -5 -> "VK_ERROR_MEMORY_MAP_FAILED (-5)"
    -6 -> "VK_ERROR_LAYER_NOT_PRESENT (-6)"
    -7 -> "VK_ERROR_EXTENSION_NOT_PRESENT (-7)"
    -8 -> "VK_ERROR_FEATURE_NOT_PRESENT (-8)"
    -9 -> "VK_ERROR_INCOMPATIBLE_DRIVER (-9)"
    -10 -> "VK_ERROR_TOO_MANY_OBJECTS (-10)"
    -11 -> "VK_ERROR_FORMAT_NOT_SUPPORTED (-11)"
    -12 -> "VK_ERROR_FRAGMENTED_POOL (-12)"
    -1000000000 -> "VK_ERROR_SURFACE_LOST_KHR (-1000000000)"
    else -> "VkResult($code)"
}

@ExternalSymbolName("skiko_vulkan_x11_create")
private external fun VulkanX11Presenter_create(displayPtr: NativePointer, window: ULong): NativePointer

@ExternalSymbolName("skiko_vulkan_x11_destroy")
private external fun VulkanX11Presenter_destroy(handle: NativePointer)

/**
 * Returns 0 for BGRA8888, 1 for RGBA8888.
 */
@ExternalSymbolName("skiko_vulkan_x11_get_pixel_format")
private external fun VulkanX11Presenter_getPixelFormat(handle: NativePointer): Int

@ExternalSymbolName("skiko_vulkan_x11_present")
private external fun VulkanX11Presenter_present(
    handle: NativePointer,
    pixelsPtr: NativePointer,
    width: Int,
    height: Int,
    rowBytes: Int,
): Int
