#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>
#include "jni_helpers.h"

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeInit(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeDispose(JNIEnv *env, jobject canvas)
    {
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        Window win = dsi_x11->drawable;
        Window parent = win;
        Window root = None;
        Window *children;
        unsigned int nchildren;
        Status s;

        while (parent != root) {
            win = parent;
            s = XQueryTree(dsi_x11->display, win, &root, &parent, &children, &nchildren);

            if (s)
                XFree(children);
            else
                return 0;
        }
        return (jlong)win;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
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

    double getDpiScale() {
        Display *display = XOpenDisplay(nullptr);
        if (display != nullptr) {
            double result = getDpiScaleByDisplay(display);
            XCloseDisplay(display);
            return result;
        } else {
            return 1;
        }
    }

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_linuxGetDpiScaleNative(JNIEnv *env, jobject properties, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        return (float) getDpiScaleByDisplay(dsi_x11->display);
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemTheme_1awtKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
    {
       // Unknown.
       return 2;
    }


    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_SetupKt_linuxGetSystemDpiScale(JNIEnv *env, jobject layer)
    {
        return (float) getDpiScale();
    }

} // extern "C"