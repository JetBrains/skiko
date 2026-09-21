#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include "xrandr_utils.h"
#include "jni_helpers.h"

extern "C"
{
    JNIEXPORT jdouble JNICALL Java_org_jetbrains_skiko_DisplayKt_getLinuxDisplayRefreshRate(JNIEnv *env, jobject obj, jlong displayPtr, jlong windowPtr)
    {
        Display *display = fromJavaPointer<Display *>(displayPtr);
        Window window = fromJavaPointer<Window>(windowPtr);
        XRRScreenResources *screenResources = XRRGetScreenResourcesCurrentDynamic(display, window);

        RRMode activeModeId = 0;

        if (!screenResources) return 60.0;

        for (int i = 0; i < screenResources->ncrtc; ++i) {
            XRRCrtcInfo *info = XRRGetCrtcInfoDynamic(display, screenResources, screenResources->crtcs[i]);
            if (info->mode != None) {
                activeModeId = info->mode;
            }
            XRRFreeCrtcInfoDynamic(info);
        }

        double rate = 0;
        for (int i = 0; i < screenResources->nmode; ++i) {
            XRRModeInfo info = screenResources->modes[i];
            if (info.id == activeModeId) {
                rate = (double) info.dotClock / ((double) info.hTotal * (double) info.vTotal);
            }
        }

        XRRFreeScreenResourcesDynamic(screenResources);

        return rate;
    }
}
