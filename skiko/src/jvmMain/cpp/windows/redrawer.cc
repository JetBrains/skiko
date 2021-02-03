#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <gl/GL.h>
#include <jawt_md.h>
#include <dwmapi.h>
#include "../common/jni_helpers.h"

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_getDevice(JNIEnv *env, jobject redrawer, jlong platformInfoPtr)
    {
        JAWT_Win32DrawingSurfaceInfo* dsi_win = fromJavaPointer<JAWT_Win32DrawingSurfaceInfo *>(platformInfoPtr);

        HWND hwnd = dsi_win->hwnd;
        HDC device = GetDC(hwnd);

        PIXELFORMATDESCRIPTOR pixFormatDscr;
        memset(&pixFormatDscr, 0, sizeof(PIXELFORMATDESCRIPTOR));
        pixFormatDscr.nSize = sizeof(PIXELFORMATDESCRIPTOR);
        pixFormatDscr.nVersion = 1;
        pixFormatDscr.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;

        pixFormatDscr.iPixelType = PFD_TYPE_RGBA;
        pixFormatDscr.cColorBits = 32;
        int iPixelFormat = ChoosePixelFormat(device, &pixFormatDscr);
        SetPixelFormat(device, iPixelFormat, &pixFormatDscr);
        DescribePixelFormat(device, iPixelFormat, sizeof(PIXELFORMATDESCRIPTOR), &pixFormatDscr);

        return toJavaPointer(device);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_setSwapInterval(JNIEnv *env, jobject redrawer, jint interval)
    {
        typedef BOOL (WINAPI *PFNWGLSWAPINTERVALEXTPROC)(int interval);
        // according to [https://opengl.gpuinfo.org/listreports.php?extension=WGL_EXT_swap_control&option=not] (filter by OS=windows)
        // there a very few devices that doesn't support swap control
        static PFNWGLSWAPINTERVALEXTPROC wglSwapIntervalEXT = (PFNWGLSWAPINTERVALEXTPROC) wglGetProcAddress("wglSwapIntervalEXT");
        if (wglSwapIntervalEXT != NULL)
        {
            wglSwapIntervalEXT(interval);
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_swapBuffers(JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        HDC device = fromJavaPointer<HDC>(devicePtr);
        SwapBuffers(device);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_makeCurrent(JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr)
    {
        HDC device = fromJavaPointer<HDC>(devicePtr);
        HGLRC context = fromJavaPointer<HGLRC>(contextPtr);
        wglMakeCurrent(device, context);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_createContext(JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        HDC device = fromJavaPointer<HDC>(devicePtr);
        return toJavaPointer(wglCreateContext(device));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_deleteContext(JNIEnv *env, jobject redrawer, jlong contextPtr)
    {
        HGLRC context = fromJavaPointer<HGLRC>(contextPtr);
        wglDeleteContext(context);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_dwmFlush(JNIEnv *env, jobject redrawer)
    {
        DwmFlush();
    }
}