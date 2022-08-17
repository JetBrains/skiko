#if SK_BUILD_FOR_LINUX

#include "xrandr_utils.h"
#include <dlfcn.h>

void* loadXrandr() {
    static void* result = nullptr;
    if (result != nullptr) return result;
    result = dlopen("libXrandr.so", RTLD_LAZY | RTLD_LOCAL);
    return result;
}

XRRScreenResources* XRRGetScreenResourcesCurrentDynamic(Display* display, Window window) {
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

XRRCrtcInfo* XRRGetCrtcInfoDynamic(
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

XRROutputInfo* XRRGetOutputInfoDynamic(
    Display *display, XRRScreenResources *resources, RROutput output) {
    typedef XRROutputInfo* (*XRRGetOutputInfo_t)(Display*, XRRScreenResources*, RROutput);
    static XRRGetOutputInfo_t func = nullptr;
    if (!func) {
        void* lib = loadXrandr();
        if (!lib) return nullptr;
        func = (XRRGetOutputInfo_t)dlsym(lib, "XRRGetOutputInfo");
    }
    if (!func) return nullptr;
    return func(display, resources, output);
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

void XRRFreeOutputInfoDynamic(XRROutputInfo* outputInfo) {
    typedef void (*XRRFreeOutputInfo_t)(XRROutputInfo*);
    static XRRFreeOutputInfo_t func = nullptr;
    if (!func) {
        void* lib = loadXrandr();
        if (!lib) return;
        func = (XRRFreeOutputInfo_t)dlsym(lib, "XRRFreeOutputInfo");
    }
    if (!func) return;
    func(outputInfo);
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

#endif