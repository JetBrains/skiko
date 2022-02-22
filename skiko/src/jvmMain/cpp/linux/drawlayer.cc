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
#include <vector>

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

static void* loadXrandr() {
    static void* result = nullptr;
    if (result != nullptr) return result;
    result = dlopen("libXrandr.so", RTLD_LAZY | RTLD_LOCAL);
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

static XRROutputInfo* XRRGetOutputInfoDynamic(
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

static void XRRFreeCrtcInfoDynamic(XRRCrtcInfo* crtcInfo) {
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

static void XRRFreeOutputInfoDynamic(XRROutputInfo* outputInfo) {
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

static void XRRFreeScreenResourcesDynamic(XRRScreenResources *resources) {
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

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_SetupKt_linuxGetSystemDpiScale(JNIEnv *env, jobject layer)
    {
        return (float) getDpiScale();
    }

    struct MonitorInfo {
        int x;
        int y;
        int wPx;
        int hPx;
        int wMm;
        int hMm;
    };

    int getIntersectSquare(int xR, int yR, int wR, int hR, int xA, int yA, int wA, int hA) {
        xR = xR - xA;
        xA = 0;
        yR = yR - yA;
        yA = 0;
        if (xR < 0) {
            wR = xR + wR;
            xR = 0;
        }
        if (yR < 0) {
            hR = yR + hR;
            yR = 0;
        }
        if (xR + wR > xA + wA) {
            wR = wA - xR;
        }
        if (yR + hR > yA + hA) {
            hR = hA - yR;
        }
        return wR * hA;
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_HardwareLayer_getCurrentDPI(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        Display *display = dsi_x11->display;
        Window window = (Window)Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(env, canvas, platformInfoPtr);
        XRRScreenResources * res = XRRGetScreenResourcesCurrentDynamic(display, window);
        XRRCrtcInfo *crtc_info;

        std::vector<MonitorInfo> monitors; 
  
        for (int i = 0; i < res->ncrtc; i++)
        {
            XRROutputInfo * output_info = XRRGetOutputInfoDynamic(display, res, res->outputs[i]);
            if (output_info->connection || output_info->crtc == NULL) {
                XRRFreeOutputInfoDynamic(output_info);
                continue;
            }
            XRRCrtcInfo * crtc_info = XRRGetCrtcInfoDynamic(display, res, output_info->crtc);

            MonitorInfo minfo;

            minfo.x = crtc_info->x;
            minfo.y = crtc_info->y;
            minfo.wPx = crtc_info->width;
            minfo.hPx = crtc_info->height;

            if (crtc_info->rotation == RR_Rotate_90 || crtc_info->rotation == RR_Rotate_270)
            {
                minfo.wMm = output_info->mm_height;
                minfo.hMm = output_info->mm_width;
            }
            else
            {
                minfo.wMm = output_info->mm_width;
                minfo.hMm = output_info->mm_height;
            }
            // If XRandr does not provide a physical size, assume the X11 default 96 DPI
            if (minfo.wMm <= 0 || minfo.hMm <= 0)
            {
                minfo.wMm  = (int) (minfo.wPx * 25.4f / 96.f);
                minfo.hMm = (int) (minfo.hPx * 25.4f / 96.f);
            }

            monitors.push_back(minfo);

            XRRFreeCrtcInfoDynamic(crtc_info);
            XRRFreeOutputInfoDynamic(output_info);
        }
        XRRFreeScreenResourcesDynamic(res);

        if (monitors.size() == 1) {
            return (jint)(monitors[0].wPx / (monitors[0].wMm / 25.4));
        }

        // Determining which monitor the current window is open on
        XWindowAttributes xwa;
        XGetWindowAttributes(display, window, &xwa);
        int index = 0;
        int maxIntersectSquare = 0;
        for (int m = 0; m < monitors.size(); m++) {
            int intersectSquare = getIntersectSquare(
                xwa.x, xwa.y, xwa.width, xwa.height,
                monitors[m].x, monitors[m].y, monitors[m].wPx, monitors[m].hPx
            );
            if (maxIntersectSquare < intersectSquare) {
                maxIntersectSquare = intersectSquare;
                index = m;
            }
        }

        return (jint)(monitors[index].wPx / (monitors[index].wMm / 25.4));
    }

} // extern "C"