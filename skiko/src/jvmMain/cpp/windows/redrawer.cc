#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <gl/GL.h>
#include <jawt_md.h>
#include <dwmapi.h>

extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_getDevice(JNIEnv *env, jobject redrawer, jobject layer)
    {
        JAWT awt;
        awt.version = (jint)JAWT_VERSION_9;
        if (!Skiko_GetAWT(env, &awt))
        {
            fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
            return 0;
        }

        JAWT_DrawingSurface *ds = awt.GetDrawingSurface(env, layer);
        ds->Lock(ds);
        JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
        JAWT_Win32DrawingSurfaceInfo *dsi_win = (JAWT_Win32DrawingSurfaceInfo *)dsi->platformInfo;

        HWND hwnd = dsi_win->hwnd;
        HDC device = GetDC(hwnd);

        if (dsi != NULL)
        {
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
        }

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);

        return static_cast<jlong>(reinterpret_cast<uintptr_t>(device));
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
        HDC device = reinterpret_cast<HDC>(static_cast<uintptr_t>(devicePtr));
        SwapBuffers(device);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_makeCurrent(JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr)
    {
        HDC device = reinterpret_cast<HDC>(static_cast<uintptr_t>(devicePtr));
        HGLRC context = reinterpret_cast<HGLRC>(static_cast<uintptr_t>(contextPtr));
        wglMakeCurrent(device, context);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_createContext(JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        HDC device = reinterpret_cast<HDC>(static_cast<uintptr_t>(devicePtr));
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(wglCreateContext(device)));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_deleteContext(JNIEnv *env, jobject redrawer, jlong contextPtr)
    {
        HGLRC context = reinterpret_cast<HGLRC>(static_cast<uintptr_t>(contextPtr));
        wglDeleteContext(context);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_dwmFlush(JNIEnv *env, jobject redrawer)
    {
        DwmFlush();
    }
}