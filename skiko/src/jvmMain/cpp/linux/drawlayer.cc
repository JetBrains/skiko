#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_init(JNIEnv *env, jobject canvas)
    {
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_dispose(JNIEnv *env, jobject canvas)
    {
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

    double getDpiScaleByDisplay(Display *display)
    {
        char *resourceManager = XResourceManagerString(display);
        if (resourceManager != nullptr)
        {
            XrmDatabase db = XrmGetStringDatabase(resourceManager);
            if (db != nullptr)
            {
                XrmValue value;

                char *type;
                XrmGetResource(db, "Xft.dpi", "Xft.dpi", &type, &value);

                if (value.addr != nullptr)
                {
                    return atof(value.addr) / 96.0;
                }
            }
        }
        return 1;
    }

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_linuxGetDpiScaleNative(JNIEnv *env, jobject properties, jobject component)
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

        ds = awt.GetDrawingSurface(env, component);
        lock = ds->Lock(ds);
        dsi = ds->GetDrawingSurfaceInfo(ds);
        dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;

        Display *display = dsi_x11->display;

        float dpi = (float)getDpiScaleByDisplay(display);

        ds->FreeDrawingSurfaceInfo(dsi);
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);

        return dpi;
    }
} // extern "C"