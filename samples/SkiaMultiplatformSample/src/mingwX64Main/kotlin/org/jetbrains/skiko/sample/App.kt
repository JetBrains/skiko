package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import platform.windows.*

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

lateinit var skiaLayer: SkiaLayer

fun wndProc(hwnd: HWND?, msg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    if(msg == WM_DESTROY.toUInt()) {
        PostQuitMessage(0)
        return 0
    }
    return skiaLayer.windowProc(hwnd, msg, wParam, lParam)
}

fun main() {

    skiaLayer = SkiaLayer()

    memScoped {
        val lpszClassName = "SkiaMultiplatformSample"

        val wc = alloc<WNDCLASSEX>()
        wc.cbSize = sizeOf<WNDCLASSEX>().toUInt()
        wc.lpfnWndProc = staticCFunction(::wndProc)
        wc.style = (CS_HREDRAW or CS_VREDRAW or CS_OWNDC).toUInt()
        wc.cbClsExtra = 0
        wc.cbWndExtra = 0
        wc.hInstance = null
        wc.hIcon = null
        wc.hCursor = (LoadCursor!!)(null, IDC_ARROW)
        wc.lpszMenuName = null
        wc.lpszClassName = lpszClassName.wcstr.ptr
        wc.hIconSm = null

        if (RegisterClassEx!!(wc.ptr) == 0u.toUShort()) {
            println("could not register")
            return
        }

        val hwnd = CreateWindowExA(
            0, lpszClassName, "SkikoNative",
            WS_OVERLAPPEDWINDOW,
            CW_USEDEFAULT, CW_USEDEFAULT,
            640, 480,
            null, null, null, null
        )!!
        skiaLayer.attachTo(hwnd)
        ShowWindow(hwnd, SW_SHOW)
    }

    skiaLayer.skikoView = GenericSkikoView(skiaLayer, makeApp(skiaLayer))

    memScoped {
        val msg = alloc<MSG>()
        msg.message = 0u
        while (GetMessage!!(msg.ptr, null, 0u, 0u) > 0) {
            if(msg.message == WM_QUIT.toUInt()) {
                break
            }
            TranslateMessage(msg.ptr)
            DispatchMessageA(msg.ptr)
        }
    }

    skiaLayer.detach()
}