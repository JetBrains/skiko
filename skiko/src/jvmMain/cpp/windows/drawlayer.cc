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

class LayerHandler
{
public:
    jobject canvasGlobalRef;
    HGLRC context;
    HDC device;

    void updateLayerContent()
    {
        draw();
    }

    void disposeLayer(JNIEnv *env)
    {
        env->DeleteGlobalRef(canvasGlobalRef);
        canvasGlobalRef = NULL;
        context = NULL;
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

            static jclass wndClass = NULL;
            if (!wndClass) wndClass = env->GetObjectClass(canvasGlobalRef);
            static jmethodID drawMethod = NULL;
            if (!drawMethod) drawMethod = env->GetMethodID(wndClass, "draw", "()V");
            if (NULL == drawMethod)
            {
                fprintf(stderr, "The method Window.draw() not found!\n");
                return;
            }
            env->CallVoidMethod(canvasGlobalRef, drawMethod);

            glFinish();
            SwapBuffers(device);
        }
    }
};

set<LayerHandler *> *layerStorage = NULL;
LayerHandler *findByObject(JNIEnv *env, jobject object)
{
    if (layerStorage == NULL)
    {
        return NULL;
    }
    for (auto &layer : *layerStorage)
    {
        if (env->IsSameObject(object, layer->canvasGlobalRef) == JNI_TRUE)
        {
            return layer;
        }
    }

    return NULL;
}

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_layer_HardwareLayer_updateLayer(JNIEnv *env, jobject canvas)
    {
        if (layerStorage != NULL)
        {
            LayerHandler *layer = findByObject(env, canvas);
            if (layer != NULL)
            {
                return;
            }
        }
        else
        {
            layerStorage = new set<LayerHandler *>();
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

        ds = awt.GetDrawingSurface(env, canvas);
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

            LayerHandler *layer = new LayerHandler();
            layerStorage->insert(layer);

            jobject canvasRef = env->NewGlobalRef(canvas);

            layer->canvasGlobalRef = canvasRef;
            layer->context = context;
            layer->device = device;
        }

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_layer_HardwareLayer_redrawLayer(JNIEnv *env, jobject canvas)
    {
        LayerHandler *layer = findByObject(env, canvas);
        if (layer != NULL)
        {
            layer->updateLayerContent();
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_layer_HardwareLayer_disposeLayer(JNIEnv *env, jobject canvas)
    {
        LayerHandler *layer = findByObject(env, canvas);
        if (layer != NULL)
        {
            layerStorage->erase(layer);
            layer->disposeLayer(env);
            delete layer;
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_layer_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas)
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

        ds = awt.GetDrawingSurface(env, canvas);
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