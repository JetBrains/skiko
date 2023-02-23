#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <cstdlib>
#include <dlfcn.h>
#include <unistd.h>
#include <stdio.h>
#include <vector>
#include "xrandr_utils.h"
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