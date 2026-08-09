@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import platform.windows.BI_RGB
import platform.windows.BITMAPINFO
import platform.windows.DIB_RGB_COLORS
import platform.windows.DwmFlush
import platform.windows.GetClientRect
import platform.windows.GetDC
import platform.windows.GetDeviceCaps
import platform.windows.GetLastError
import platform.windows.GetWindowLongPtrW
import platform.windows.GetWindowRect
import platform.windows.HKEY_CURRENT_USER
import platform.windows.HWND
import platform.windows.LOGPIXELSX
import platform.windows.MONITORINFO
import platform.windows.MONITOR_DEFAULTTONEAREST
import platform.windows.MonitorFromWindow
import platform.windows.RRF_RT_REG_DWORD
import platform.windows.RECT
import platform.windows.RegGetValueW
import platform.windows.ReleaseDC
import platform.windows.SRCCOPY
import platform.windows.SWP_FRAMECHANGED
import platform.windows.SWP_NOACTIVATE
import platform.windows.SWP_NOOWNERZORDER
import platform.windows.SWP_SHOWWINDOW
import platform.windows.SetWindowLongPtrW
import platform.windows.SetWindowPos
import platform.windows.ShowWindow
import platform.windows.StretchDIBits
import platform.windows.SW_RESTORE
import platform.windows.WS_OVERLAPPEDWINDOW
import platform.windows.GWL_STYLE
import platform.windows.GetMonitorInfoW

private data class WindowRect(val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * Kotlin/Native Windows layer backed by a Win32 HWND.
 *
 * The initial renderer is a BGRA software surface presented with StretchDIBits.
 * This gives native window attachment, DPI-aware sizing, invalidation, fullscreen,
 * and DWM-based frame throttling without requiring a JVM/AWT host.
 */
actual open class SkiaLayer {
    private var nativeWindow: WindowsNativeWindow? = null
    private var savedWindowStyle: Long? = null
    private var savedWindowRect: WindowRect? = null
    private var rendering = false
    private var renderPending = false

    actual var renderApi: GraphicsApi = GraphicsApi.SOFTWARE_FAST
        set(value) {
            require(value == GraphicsApi.SOFTWARE_FAST || value == GraphicsApi.SOFTWARE_COMPAT) {
                "$value is not implemented by Kotlin/Native Windows yet"
            }
            field = value
        }

    actual val contentScale: Float
        get() {
            val hwnd = nativeWindow?.hwnd() ?: return 1f
            val dc = GetDC(hwnd) ?: return 1f
            return try {
                (GetDeviceCaps(dc, LOGPIXELSX).coerceAtLeast(96) / 96f)
            } finally {
                ReleaseDC(hwnd, dc)
            }
        }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.RGB_H

    actual var fullscreen: Boolean
        get() = savedWindowStyle != null
        set(value) {
            val hwnd = nativeWindow?.hwnd()
                ?: throw IllegalStateException("SkiaLayer must be attached before changing fullscreen")
            if (value == fullscreen) return
            if (value) enterFullscreen(hwnd) else leaveFullscreen(hwnd)
        }

    actual val component: Any?
        get() = nativeWindow

    actual var renderDelegate: SkikoRenderDelegate? = null
        set(value) {
            field = value
            nativeWindow?.paintCallback = value?.let { { needRender(throttledToVsync = false) } }
        }

    actual fun attachTo(container: Any) {
        check(nativeWindow == null) { "SkiaLayer is already attached" }
        nativeWindow = when (container) {
            is WindowsNativeWindow -> container
            is Long -> WindowsNativeWindow(container)
            else -> error("container must be WindowsNativeWindow or a non-zero HWND Long")
        }
        nativeWindow?.paintCallback = renderDelegate?.let { { needRender(throttledToVsync = false) } }
        if (renderDelegate != null) needRender(throttledToVsync = false)
    }

    actual fun detach() {
        if (fullscreen) {
            nativeWindow?.hwnd()?.let(::leaveFullscreen)
        }
        nativeWindow?.paintCallback = null
        nativeWindow = null
        renderPending = false
    }

    actual fun needRender(throttledToVsync: Boolean) {
        if (renderDelegate == null) return
        val hwnd = nativeWindow?.hwnd() ?: return
        if (rendering) {
            renderPending = true
            return
        }

        rendering = true
        try {
            do {
                renderPending = false
                if (throttledToVsync) DwmFlush()
                renderAndPresent(hwnd)
            } while (renderPending)
        } finally {
            rendering = false
        }
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

    internal actual fun draw(canvas: Canvas) {
        val window = nativeWindow ?: return
        val (width, height) = clientSize(window.hwnd())
        if (width > 0 && height > 0) {
            renderDelegate?.onRender(canvas, width, height, currentNanoTime())
        }
    }

    private fun renderAndPresent(hwnd: HWND) {
        val (width, height) = clientSize(hwnd)
        if (width <= 0 || height <= 0) return

        val rowBytes = width * 4
        val pixels = ByteArray(rowBytes * height)
        pixels.usePinned { pinned ->
            val imageInfo = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.PREMUL)
            Surface.makeRasterDirect(imageInfo, pinned.addressOf(0).rawValue, rowBytes).use { surface ->
                surface.canvas.clear(Color.TRANSPARENT)
                renderDelegate?.onRender(surface.canvas, width, height, currentNanoTime())
                surface.flush()
            }

            memScoped {
                val bitmapInfo = alloc<BITMAPINFO>()
                bitmapInfo.bmiHeader.biSize = sizeOf<platform.windows.BITMAPINFOHEADER>().convert()
                bitmapInfo.bmiHeader.biWidth = width
                bitmapInfo.bmiHeader.biHeight = -height
                bitmapInfo.bmiHeader.biPlanes = 1.convert()
                bitmapInfo.bmiHeader.biBitCount = 32.convert()
                bitmapInfo.bmiHeader.biCompression = BI_RGB.convert()

                val dc = GetDC(hwnd) ?: throw RenderException("GetDC failed (Win32 error ${GetLastError()})")
                try {
                    val result = StretchDIBits(
                        dc,
                        0, 0, width, height,
                        0, 0, width, height,
                        pinned.addressOf(0),
                        bitmapInfo.ptr,
                        DIB_RGB_COLORS.convert(),
                        SRCCOPY.convert(),
                    )
                    if (result == 0) {
                        throw RenderException("StretchDIBits failed (Win32 error ${GetLastError()})")
                    }
                } finally {
                    ReleaseDC(hwnd, dc)
                }
            }
        }
    }

    private fun clientSize(hwnd: HWND): Pair<Int, Int> = memScoped {
        val rect = alloc<RECT>()
        if (GetClientRect(hwnd, rect.ptr) == 0) return@memScoped 0 to 0
        (rect.right - rect.left) to (rect.bottom - rect.top)
    }

    private fun enterFullscreen(hwnd: HWND) = memScoped {
        val rect = alloc<RECT>()
        check(GetWindowRect(hwnd, rect.ptr) != 0) { "GetWindowRect failed" }
        savedWindowRect = WindowRect(rect.left, rect.top, rect.right, rect.bottom)
        val style = GetWindowLongPtrW(hwnd, GWL_STYLE)
        savedWindowStyle = style
        SetWindowLongPtrW(hwnd, GWL_STYLE, style and WS_OVERLAPPEDWINDOW.toLong().inv())

        val monitor = MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST.convert())
        val monitorInfo = alloc<MONITORINFO>()
        monitorInfo.cbSize = sizeOf<MONITORINFO>().convert()
        check(GetMonitorInfoW(monitor, monitorInfo.ptr) != 0) { "GetMonitorInfoW failed" }
        val bounds = monitorInfo.rcMonitor
        SetWindowPos(
            hwnd,
            null,
            bounds.left,
            bounds.top,
            bounds.right - bounds.left,
            bounds.bottom - bounds.top,
            (SWP_NOOWNERZORDER or SWP_NOACTIVATE or SWP_FRAMECHANGED or SWP_SHOWWINDOW).convert(),
        )
    }

    private fun leaveFullscreen(hwnd: HWND) {
        val style = savedWindowStyle ?: return
        val rect = savedWindowRect ?: return
        SetWindowLongPtrW(hwnd, GWL_STYLE, style)
        SetWindowPos(
            hwnd,
            null,
            rect.left,
            rect.top,
            rect.right - rect.left,
            rect.bottom - rect.top,
            (SWP_NOOWNERZORDER or SWP_NOACTIVATE or SWP_FRAMECHANGED or SWP_SHOWWINDOW).convert(),
        )
        ShowWindow(hwnd, SW_RESTORE)
        savedWindowStyle = null
        savedWindowRect = null
    }
}

actual val currentSystemTheme: SystemTheme
    get() {
        val themeValue = IntArray(1)
        val themeValueSize = IntArray(1) { Int.SIZE_BYTES }
        val status = themeValue.usePinned { valuePinned ->
            themeValueSize.usePinned { sizePinned ->
                RegGetValueW(
                    HKEY_CURRENT_USER,
                    "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "AppsUseLightTheme",
                    RRF_RT_REG_DWORD.convert(),
                    null,
                    valuePinned.addressOf(0),
                    sizePinned.addressOf(0).reinterpret<UIntVar>(),
                )
            }
        }
        return if (status != 0) SystemTheme.UNKNOWN
        else if (themeValue[0] == 0) SystemTheme.DARK else SystemTheme.LIGHT
    }
