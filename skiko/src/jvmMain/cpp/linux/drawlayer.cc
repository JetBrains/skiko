// drawcanvas.cpp : Defines the exported functions for the DLL application.
#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <unistd.h>
#include <stdio.h>
#include <set>

using namespace std;

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);

JavaVM *jvm = NULL;

class LayersSet
{
public:
    jobject canvasRef;
    GLXContext context;

    void update()
    {
        draw();
    }

    void dispose()
    {
        context = NULL;
    }

private:
    void draw()
    {
        if (jvm != NULL)
        {
            JNIEnv *env;
            jvm->GetEnv((void **)&env, JNI_VERSION_10);
            JAWT awt;
            JAWT_DrawingSurface *ds = NULL;
            JAWT_DrawingSurfaceInfo *dsi = NULL;

            jboolean result = JNI_FALSE;
            jint lock = 0;
            JAWT_X11DrawingSurfaceInfo *dsi_x11;

            awt.version = (jint)JAWT_VERSION_9;
            result = Skiko_GetAWT(env, &awt);

            if (result == JNI_FALSE)
            {
                fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
                return;
            }

            ds = awt.GetDrawingSurface(env, canvasRef);
            lock = ds->Lock(ds);
            dsi = ds->GetDrawingSurfaceInfo(ds);
            dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;
            Display *display = dsi_x11->display;
            Window window = dsi_x11->drawable;
            if (dsi != NULL)
            {
                jvm->AttachCurrentThread((void **)&env, NULL);

                glXMakeCurrent(display, window, context);

                jclass wndClass = env->GetObjectClass(canvasRef);
                jmethodID drawMethod = env->GetMethodID(wndClass, "draw", "()V");
                if (NULL == drawMethod)
                {
                    fprintf(stderr, "The method Window.draw() not found!\n");
                    return;
                }
                env->CallVoidMethod(canvasRef, drawMethod);
                glFinish();
                glXSwapBuffers(display, window);
            }
            ds->FreeDrawingSurfaceInfo(dsi);
            ds->Unlock(ds);
            awt.FreeDrawingSurface(ds);
        }
    }
};

set<LayersSet *> *windowsSet = NULL;
LayersSet *findByObject(JNIEnv *env, jobject object)
{
    for (auto &layer : *windowsSet)
    {
        if (env->IsSameObject(object, layer->canvasRef) == JNI_TRUE)
        {
            return layer;
        }
    }
    fprintf(stderr, "The set does not contain this window.\n");
    return NULL;
}

extern "C"
{

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_updateLayer(JNIEnv *env, jobject canvas)
    {
        if (windowsSet != NULL)
        {
            LayersSet *layer = findByObject(env, canvas);
            if (layer != NULL)
            {
                if (layer->context == NULL)
                {
                    env->DeleteGlobalRef(layer->canvasRef);
                    layer->canvasRef = NULL;
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

        if (jvm == NULL)
        {
            env->GetJavaVM(&jvm);
        }

        JAWT awt;
        JAWT_DrawingSurface *ds = NULL;
        JAWT_DrawingSurfaceInfo *dsi = NULL;

        jboolean result = JNI_FALSE;
        jint lock = 0;
        JAWT_X11DrawingSurfaceInfo *dsi_x11;

        awt.version = (jint)JAWT_VERSION_9;
        result = Skiko_GetAWT(env, &awt);

        if (result == JNI_FALSE)
        {
            fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
            return;
        }

        ds = awt.GetDrawingSurface(env, canvas);
        lock = ds->Lock(ds);
        dsi = ds->GetDrawingSurfaceInfo(ds);
        dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;
        Display *display = dsi_x11->display;
        Window window = dsi_x11->drawable;
        if (dsi != NULL)
        {
            GLint att[] = {GLX_RGBA, GLX_DOUBLEBUFFER, True, None};

            XVisualInfo *vi = glXChooseVisual(display, 0, att);
            GLXContext context = glXCreateContext(display, vi, NULL, GL_TRUE);

            LayersSet *layer = new LayersSet();
            windowsSet->insert(layer);

            jobject canvasRef = env->NewGlobalRef(canvas);

            layer->canvasRef = canvasRef;
            layer->context = context;
        }
        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_redrawLayer(JNIEnv *env, jobject canvas)
    {
        LayersSet *layer = findByObject(env, canvas);
        if (layer != NULL)
        {
            layer->update();
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_disposeLayer(JNIEnv *env, jobject canvas)
    {
        LayersSet *layer = findByObject(env, canvas);
        if (layer != NULL)
        {
            layer->dispose();
        }
    }

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentScale(JNIEnv *env, jobject canvas)
    {
        return 1.0f;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas)
    {
        JAWT awt;
        JAWT_DrawingSurface *ds = NULL;
        JAWT_DrawingSurfaceInfo *dsi = NULL;

        jboolean result = JNI_FALSE;
        jint lock = 0;
        JAWT_X11DrawingSurfaceInfo *dsi_x11;

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
        dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;

        Window window = dsi_x11->drawable;

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);

        return (jlong)window;
    }
} // extern "C"