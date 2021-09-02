#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <X11/extensions/Xrandr.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>
#include "jni_helpers.h"

extern "C"
{
    JNIEXPORT jdouble JNICALL Java_org_jetbrains_skiko_DisplayKt_getLinuxDefaultDisplayRefreshRate(JNIEnv *env, jobject obj)
    {
        Display *display = XOpenDisplay(NULL);
        Window window = DefaultRootWindow(display);
        XRRScreenResources *screenResources = XRRGetScreenResources(display, window);

        RRMode activeModeId = 0;
        for (int i = 0; i < screenResources->ncrtc; ++i) {
            XRRCrtcInfo *info = XRRGetCrtcInfo(display, screenResources, screenResources->crtcs[i]);
            if (info->mode != None) {
                activeModeId = info->mode;
            }
            XRRFreeCrtcInfo(info);
        }

        double rate = 0;
        for (int i = 0; i < screenResources->nmode; ++i) {
            XRRModeInfo info = screenResources->modes[i];
            if (info.id == activeModeId) {
                rate = (double) info.dotClock / ((double) info.hTotal * (double) info.vTotal);
            }
        }

        XRRFreeScreenResources(screenResources);
        XCloseDisplay(display);

        return rate;
    }
}
