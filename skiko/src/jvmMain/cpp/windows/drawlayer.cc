// drawcanvas.cpp : Defines the exported functions for the DLL application.

#include <SDKDDKVer.h>
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <gl/GL.h>
#include <jawt_md.h>
#include <set>
#include <Shellscalingapi.h>
#include <stdio.h>
#include <Wingdi.h>

using namespace std;

JavaVM *jvm = NULL;
extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);

class LayersSet
{
public:
    jobject windowRef;
    HGLRC context;
    HWND handler;
    HDC device;

    void update()
    {
        draw();
    }

    void dispose()
    {
        context = NULL;
        handler = NULL;
        device = NULL;
    }

private:
    void draw()
    {
        if (jvm != NULL)
        {
            JNIEnv *env;
            jvm->GetEnv((void **)&env, JNI_VERSION_10);
            wglMakeCurrent(device, context);

            jclass wndClass = env->GetObjectClass(windowRef);
            jmethodID drawMethod = env->GetMethodID(wndClass, "draw", "()V");
            if (NULL == drawMethod)
            {
                fprintf(stderr, "The method Window.draw() not found!\n");
                return;
            }
            env->CallVoidMethod(windowRef, drawMethod);

            glFinish();
            SwapBuffers(device);
        }
    }
};

set<LayersSet *> *windowsSet = NULL;
LayersSet *findByObject(JNIEnv *env, jobject object)
{
    for (auto &layer : *windowsSet)
    {
        if (env->IsSameObject(object, layer->windowRef) == JNI_TRUE)
        {
            return layer;
        }
    }

    return NULL;
}

extern "C"
{

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_updateLayer(JNIEnv *env, jobject window)
    {
        if (windowsSet != NULL)
        {
            LayersSet *layer = findByObject(env, window);
            if (layer != NULL)
            {
                if (layer->context == NULL)
                {
                    env->DeleteGlobalRef(layer->windowRef);
                    layer->windowRef = NULL;
                    windowsSet->erase(layer);
                    delete layer;
                }
                return;
            }
        }
        else
        {
            windowsSet = new set<LayersSet *>();
        }

        JAWT awt;
        JAWT_DrawingSurface *ds = NULL;
        JAWT_DrawingSurfaceInfo *dsi = NULL;
        PIXELFORMATDESCRIPTOR pixFormatDscr;
        HGLRC context = NULL;

        jboolean result = JNI_FALSE;
        jint lock = 0;
        JAWT_Win32DrawingSurfaceInfo *dsi_win;

        awt.version = (jint)JAWT_VERSION_9;
        result = Skiko_GetAWT(env, &awt);

        if (result == JNI_FALSE)
        {
            fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
            return;
        }

        if (jvm == NULL)
        {
            env->GetJavaVM(&jvm);
        }

        ds = awt.GetDrawingSurface(env, window);
        lock = ds->Lock(ds);
        dsi = ds->GetDrawingSurfaceInfo(ds);
        dsi_win = (JAWT_Win32DrawingSurfaceInfo *)dsi->platformInfo;

        HWND hwnd = dsi_win->hwnd;
        HDC device = GetDC(hwnd);

        if (dsi != NULL)
        {
            memset(&pixFormatDscr, 0, sizeof(PIXELFORMATDESCRIPTOR));
            pixFormatDscr.nSize = sizeof(PIXELFORMATDESCRIPTOR);
            pixFormatDscr.nVersion = 1;
            pixFormatDscr.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;

            pixFormatDscr.iPixelType = PFD_TYPE_RGBA;
            pixFormatDscr.cColorBits = 32;
            int iPixelFormat = ChoosePixelFormat(device, &pixFormatDscr);
            SetPixelFormat(device, iPixelFormat, &pixFormatDscr);
            DescribePixelFormat(device, iPixelFormat, sizeof(PIXELFORMATDESCRIPTOR), &pixFormatDscr);
            context = wglCreateContext(device);

            LayersSet *layer = new LayersSet();
            windowsSet->insert(layer);

            jobject windowRef = env->NewGlobalRef(window);

            layer->windowRef = windowRef;
            layer->context = context;
            layer->handler = hwnd;
            layer->device = device;
        }

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_redrawLayer(JNIEnv *env, jobject window)
    {
        LayersSet *layer = findByObject(env, window);
        if (layer != NULL)
        {
            layer->update();
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_disposeLayer(JNIEnv *env, jobject window)
    {
        LayersSet *layer = findByObject(env, window);
        if (layer != NULL)
        {
            layer->dispose();
        }
    }

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentScale(JNIEnv *env, jobject window)
    {
        LayersSet *layer = findByObject(env, window);
        if (layer != NULL)
        {
            // get scale dpi factor of current monitor
            HWND parent = GetParent(layer->handler);
            if (parent != NULL)
            {
                UINT dpi = GetDpiForWindow(parent);
                float result = dpi / 96.0;
                return result;
            }
        }
        return 1.0f;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject window)
    {
        JAWT awt;
        JAWT_DrawingSurface *ds = NULL;
        JAWT_DrawingSurfaceInfo *dsi = NULL;
        PIXELFORMATDESCRIPTOR pixFormatDscr;
        HGLRC context = NULL;

        jboolean result = JNI_FALSE;
        jint lock = 0;
        JAWT_Win32DrawingSurfaceInfo *dsi_win;

        awt.version = (jint)JAWT_VERSION_9;
        result = Skiko_GetAWT(env, &awt);

        if (result == JNI_FALSE)
        {
            fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
            return -1;
        }

        if (jvm == NULL)
        {
            env->GetJavaVM(&jvm);
        }

        ds = awt.GetDrawingSurface(env, window);
        lock = ds->Lock(ds);
        dsi = ds->GetDrawingSurfaceInfo(ds);
        dsi_win = (JAWT_Win32DrawingSurfaceInfo *)dsi->platformInfo;

        HWND hwnd = dsi_win->hwnd;

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);

        return (jlong)hwnd;
    }
} // extern "C"