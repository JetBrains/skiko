#if SK_BUILD_FOR_LINUX

#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <X11/extensions/Xrandr.h>

void* loadXrandr();
XRRScreenResources* XRRGetScreenResourcesCurrentDynamic(Display* display, Window window);
XRRCrtcInfo* XRRGetCrtcInfoDynamic(Display *display, XRRScreenResources *resources, RRCrtc crtc);
XRROutputInfo* XRRGetOutputInfoDynamic(Display *display, XRRScreenResources *resources, RROutput output);
void XRRFreeCrtcInfoDynamic(XRRCrtcInfo* crtcInfo);
void XRRFreeOutputInfoDynamic(XRROutputInfo* outputInfo);
void XRRFreeScreenResourcesDynamic(XRRScreenResources *resources);

#endif