#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <X11/extensions/Xrandr.h>
#include <cstdlib>
#include <dlfcn.h>
#include <unistd.h>
#include <stdio.h>

#include "jni_helpers.h"

static void* loadXrandr() {
    static void* result = nullptr;
    if (result != nullptr) return result;

    static const char* searchPaths[] = { "/lib", "/lib/x86_64-linux-gnu", "/lib/aarch64-linux-gnu", nullptr };
    for (int i = 0; searchPaths[i] != nullptr; i++) {
        char buf[MAX_PATH];
        snprintf(buf, MAX_PATH, "%s/libXrandr.so", searchPaths[i]);
        result = dlopen(buf, RTLD_LOCAL);
        if (result) break;
    }
    printf("library is %p\n", result);
    return result;
}

static XRRScreenResources* XRRGetScreenResourcesCurrentDynamic(Display* display, Window window) {
    typedef XRRScreenResources* (*XRRGetScreenResourcesCurrent_t)(Display*, Window);
    static XRRGetScreenResourcesCurrent_t func = nullptr;
    if (!func) {
        void* lib = loadXrandr();
        if (!lib) return nullptr;
        func = (XRRGetScreenResourcesCurrent_t)dlsym(lib, "XRRGetScreenResourcesCurrent");
    }
    if (!func) return nullptr;
    return func(display, window);
}

static XRRCrtcInfo* XRRGetCrtcInfoDynamic(
    Display *display, XRRScreenResources *resources, RRCrtc crtc) {
    typedef XRRCrtcInfo* (*XRRGetCrtcInfo_t)(Display*, XRRScreenResources*, RRCrtc);
    static XRRGetCrtcInfo_t func = nullptr;
    if (!func) {
        void* lib = loadXrandr();
        if (!lib) return nullptr;
        func = (XRRGetCrtcInfo_t)dlsym(lib, "XRRGetCrtcInfo");
    }
    if (!func) return nullptr;
    return func(display, resources, crtc);
}

void XRRFreeCrtcInfoDynamic(XRRCrtcInfo* crtcInfo) {
    typedef void (*XRRFreeCrtcInfo_t)(XRRCrtcInfo*);
    static XRRFreeCrtcInfo_t func = nullptr;
    if (!func) {
        void* lib = loadXrandr();
        if (!lib) return;
        func = (XRRFreeCrtcInfo_t)dlsym(lib, "XRRFreeCrtcInfo");
    }
    if (!func) return;
    func(crtcInfo);
}

void XRRFreeScreenResourcesDynamic(XRRScreenResources *resources) {
    typedef void (*XRRFreeScreenResources_t)(XRRScreenResources*);
    static XRRFreeScreenResources_t func = nullptr;
    if (!func) {
        void* lib = loadXrandr();
        if (!lib) return;
        func = (XRRFreeScreenResources_t)dlsym(lib, "XRRFreeScreenResources");
    }
    if (!func) return;
    func(resources);
}

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
