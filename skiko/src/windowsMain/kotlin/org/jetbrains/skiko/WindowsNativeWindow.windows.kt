@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.rawValue
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.wcstr
import platform.windows.BeginPaint
import platform.windows.CS_HREDRAW
import platform.windows.CS_OWNDC
import platform.windows.CS_VREDRAW
import platform.windows.CW_USEDEFAULT
import platform.windows.CreateWindowExW
import platform.windows.DefWindowProcW
import platform.windows.DestroyWindow
import platform.windows.DispatchMessageW
import platform.windows.EndPaint
import platform.windows.GetModuleHandleW
import platform.windows.HWND
import platform.windows.HWND__
import platform.windows.IsWindow
import platform.windows.LPARAM
import platform.windows.LRESULT
import platform.windows.MSG
import platform.windows.PAINTSTRUCT
import platform.windows.PM_REMOVE
import platform.windows.PeekMessageW
import platform.windows.PostQuitMessage
import platform.windows.RegisterClassExW
import platform.windows.ShowWindow
import platform.windows.SW_SHOW
import platform.windows.TranslateMessage
import platform.windows.UINT
import platform.windows.UpdateWindow
import platform.windows.WM_CLOSE
import platform.windows.WM_DESTROY
import platform.windows.WM_DISPLAYCHANGE
import platform.windows.WM_DPICHANGED
import platform.windows.WM_ERASEBKGND
import platform.windows.WM_PAINT
import platform.windows.WM_QUIT
import platform.windows.WM_SIZE
import platform.windows.WNDCLASSEXW
import platform.windows.WPARAM
import platform.windows.WS_OVERLAPPEDWINDOW
import kotlin.native.internal.NativePtr

private const val SKIKO_WINDOW_CLASS = "SkikoKotlinNativeWindow"
private val windowsByHandle = mutableMapOf<Long, WindowsNativeWindow>()
private var windowClassRegistered = false

/**
 * A Win32 window accepted by [SkiaLayer.attachTo].
 *
 * Construct it with an existing HWND encoded as a [Long], or use [create] to
 * create a Skiko-owned top-level window with built-in paint and resize dispatch.
 * Existing HWNDs are not subclassed; their owner should call [SkiaLayer.needRender]
 * from its paint and resize handlers.
 */
class WindowsNativeWindow private constructor(
    val handle: Long,
    private val owned: Boolean,
) {
    constructor(handle: Long) : this(handle, owned = false)

    init {
        require(handle != 0L) { "A null HWND cannot host a SkiaLayer" }
    }

    internal var paintCallback: (() -> Unit)? = null

    val isValid: Boolean
        get() = IsWindow(hwnd()) != 0

    fun show() {
        check(isValid) { "The HWND is no longer valid" }
        ShowWindow(hwnd(), SW_SHOW)
        UpdateWindow(hwnd())
    }

    /** Processes all currently queued messages. Returns false after WM_QUIT. */
    fun pumpMessages(): Boolean = memScoped {
        val message = alloc<MSG>()
        while (PeekMessageW(message.ptr, null, 0u, 0u, PM_REMOVE.toUInt()) != 0) {
            if (message.message == WM_QUIT.toUInt()) return@memScoped false
            TranslateMessage(message.ptr)
            DispatchMessageW(message.ptr)
        }
        true
    }

    fun close() {
        paintCallback = null
        if (owned && isValid) DestroyWindow(hwnd())
    }

    internal fun hwnd(): HWND =
        interpretCPointer<HWND__>(NativePtr.NULL + handle) ?: error("Invalid HWND: $handle")

    companion object {
        fun create(
            title: String = "Skiko",
            width: Int = 800,
            height: Int = 600,
        ): WindowsNativeWindow {
            require(width > 0 && height > 0) { "Window dimensions must be positive" }
            registerWindowClass()
            val hwnd = CreateWindowExW(
                0u,
                SKIKO_WINDOW_CLASS,
                title,
                WS_OVERLAPPEDWINDOW.toUInt(),
                CW_USEDEFAULT,
                CW_USEDEFAULT,
                width,
                height,
                null,
                null,
                GetModuleHandleW(null),
                null,
            ) ?: error("CreateWindowExW failed")
            return WindowsNativeWindow(hwnd.rawValue.toLong(), owned = true).also {
                windowsByHandle[it.handle] = it
            }
        }
    }
}

private fun registerWindowClass() {
    if (windowClassRegistered) return
    memScoped {
        val windowClass = alloc<WNDCLASSEXW>()
        windowClass.cbSize = kotlinx.cinterop.sizeOf<WNDCLASSEXW>().convert()
        windowClass.style = (CS_HREDRAW or CS_VREDRAW or CS_OWNDC).convert()
        windowClass.lpfnWndProc = staticCFunction(::skikoWindowProc)
        windowClass.hInstance = GetModuleHandleW(null)
        windowClass.lpszClassName = SKIKO_WINDOW_CLASS.wcstr.ptr
        check(RegisterClassExW(windowClass.ptr) != 0.toUShort()) { "RegisterClassExW failed" }
    }
    windowClassRegistered = true
}

private fun skikoWindowProc(hwnd: HWND?, message: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    if (hwnd == null) return 0L
    val handle = hwnd.rawValue.toLong()
    val window = windowsByHandle[handle]
    return when (message) {
        WM_PAINT.toUInt() -> {
            memScoped {
                val paint = alloc<PAINTSTRUCT>()
                BeginPaint(hwnd, paint.ptr)
                try {
                    window?.paintCallback?.invoke()
                } finally {
                    EndPaint(hwnd, paint.ptr)
                }
            }
            0L
        }
        WM_SIZE.toUInt(), WM_DPICHANGED.toUInt(), WM_DISPLAYCHANGE.toUInt() -> {
            window?.paintCallback?.invoke()
            0L
        }
        WM_ERASEBKGND.toUInt() -> 1L
        WM_CLOSE.toUInt() -> {
            DestroyWindow(hwnd)
            0L
        }
        WM_DESTROY.toUInt() -> {
            windowsByHandle.remove(handle)?.paintCallback = null
            if (windowsByHandle.isEmpty()) PostQuitMessage(0)
            0L
        }
        else -> DefWindowProcW(hwnd, message, wParam, lParam)
    }
}
