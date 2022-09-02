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

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTLinuxDrawingSurfaceKt_getDisplay(JNIEnv *env, jobject redrawer, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        Display *display = dsi_x11->display;
        return toJavaPointer(display);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTLinuxDrawingSurfaceKt_getWindow(JNIEnv *env, jobject redrawer, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        Window window = dsi_x11->drawable;
        return toJavaPointer(window);
    }
}
