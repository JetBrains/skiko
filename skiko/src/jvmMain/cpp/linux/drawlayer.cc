#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>
#include "../common/jni_helpers.h"

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeInit(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_dispose(JNIEnv *env, jobject canvas)
    {
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        return (jlong) dsi_x11->drawable;
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

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_linuxGetDpiScaleNative(JNIEnv *env, jobject properties, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        return (float) getDpiScaleByDisplay(dsi_x11->display);
    }
} // extern "C"