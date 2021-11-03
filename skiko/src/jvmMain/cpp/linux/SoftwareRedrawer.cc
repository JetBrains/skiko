#include <jawt_md.h>
#include <X11/Xresource.h>
#include <stdint.h>
#include "jni_helpers.h"

#include "SkSurface.h"
#include "src/core/SkAutoMalloc.h"

class SoftwareDevice
{
public:
    Display* display;
    Window window;
    GC gc;
    sk_sp<SkSurface> surface;
    unsigned int depth = 0;
    SkColorType colorSpace = kUnknown_SkColorType;

    void initDevice() {
        Window wnd;
        int x, y;
        unsigned int w, h, border;
        XGetGeometry(display, window, &wnd, &x, &y, &w, &h, &border, &depth);

        switch(depth) {
            case 16:
                colorSpace = kRGB_565_SkColorType;
                break;
            case 24: case 32:
                colorSpace = kBGRA_8888_SkColorType;
                break;
            default:
                colorSpace = kUnknown_SkColorType;
        }
    }

    ~SoftwareDevice() {
        if (display != NULL && gc != NULL)
        {
            XFreeGC(display, gc);
        }
    }
};

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxSoftwareRedrawer_createDevice(
        JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr, jint width, jint height)
    {
        Display *display = fromJavaPointer<Display *>(displayPtr);
        Window window = fromJavaPointer<Window>(windowPtr);
        SoftwareDevice *device = new SoftwareDevice();
        device->display = display;
        device->window = window;
        device->gc = XCreateGC(device->display, device->window, 0, nullptr);
        device->initDevice();
        if (device->colorSpace == kUnknown_SkColorType)
        {
            return 0L;
        }

        device->surface.reset();
        SkImageInfo info = SkImageInfo::Make(
            width, height, device->colorSpace, kPremul_SkAlphaType,
            SkColorSpace::MakeSRGB());
        device->surface = SkSurface::MakeRaster(info);

        return toJavaPointer(device);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_resize(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
        device->surface.reset();
        SkImageInfo info = SkImageInfo::Make(
            width, height, device->colorSpace, kPremul_SkAlphaType,
            SkColorSpace::MakeSRGB());
        device->surface = SkSurface::MakeRaster(info);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_acquireSurface(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
        device->surface.get()->ref();
        return toJavaPointer(device->surface.get());
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
        SkPixmap pm;
        if (!device->surface->peekPixels(&pm)) {
            return;
        }
        int bitsPerPixel = pm.info().bytesPerPixel() * 8;
        XImage image;
        memset(&image, 0, sizeof(image));
        image.width = pm.width();
        image.height = pm.height();
        image.format = ZPixmap;
        image.data = (char*) pm.addr();
        image.byte_order = LSBFirst;
        image.bitmap_unit = bitsPerPixel;
        image.bitmap_bit_order = LSBFirst;
        image.bitmap_pad = bitsPerPixel;
        image.depth = device->depth;
        image.bytes_per_line = pm.rowBytes() - pm.width() * pm.info().bytesPerPixel();
        image.bits_per_pixel = bitsPerPixel;
        if (!XInitImage(&image)) {
            return;
        }
        XPutImage(device->display, device->window, device->gc, &image, 0, 0, 0, 0, pm.width(), pm.height());
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
        delete device;
    }
}
