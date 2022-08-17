#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "window_util.h"

#include "SkColorSpace.h"
#include "SkSurface.h"
#include "src/core/SkAutoMalloc.h"

class SoftwareDevice
{
public:
    HWND window;
    RECT clientRect;
    SkAutoMalloc surfaceMemory;
    sk_sp<SkSurface> surface;

    ~SoftwareDevice() {}
};

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_WindowsSoftwareRedrawer_createDevice(
        JNIEnv *env, jobject redrawer, jlong contentHandle, jboolean transparency)
    {
        SoftwareDevice *device = new SoftwareDevice();
        device->window = (HWND)contentHandle;
        if (transparency)
        {
            HWND parent = GetAncestor(device->window, GA_PARENT);
            enableTransparentWindow(parent);
        }
        GetClientRect(device->window, &device->clientRect);
        return toJavaPointer(device);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_resize(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        __try
        {
            SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
            device->surface.reset();
            const size_t bmpSize = sizeof(BITMAPINFOHEADER) + width * height * sizeof(uint32_t);
            device->surfaceMemory.reset(bmpSize);
            BITMAPINFO *bmpInfo = reinterpret_cast<BITMAPINFO *>(device->surfaceMemory.get());
            ZeroMemory(bmpInfo, sizeof(BITMAPINFO));
            bmpInfo->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
            bmpInfo->bmiHeader.biWidth = width;
            bmpInfo->bmiHeader.biHeight = -height;
            bmpInfo->bmiHeader.biPlanes = 1;
            bmpInfo->bmiHeader.biBitCount = 32;
            bmpInfo->bmiHeader.biCompression = BI_RGB;
            void *pixels = bmpInfo->bmiColors;
            SkImageInfo info = SkImageInfo::Make(
                width, height, kBGRA_8888_SkColorType, kPremul_SkAlphaType,
                SkColorSpace::MakeSRGB());
            device->surface = SkSurface::MakeRasterDirect(info, pixels, sizeof(uint32_t) * width);
            GetClientRect(device->window, &device->clientRect);
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            logJavaException(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_acquireSurface(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
        return toJavaPointer(device->surface.release());
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong surfacePtr)
    {
        __try
        {
            SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
            BITMAPINFO *bmpInfo = reinterpret_cast<BITMAPINFO *>(device->surfaceMemory.get());
            HDC dc = GetDC(device->window);
            int width = device->clientRect.right - device->clientRect.left;
            int height = device->clientRect.bottom - device->clientRect.top;
            StretchDIBits(dc, 0, 0, width, height, 0, 0, width, height, bmpInfo->bmiColors, bmpInfo,
                          DIB_RGB_COLORS, SRCCOPY);
            ReleaseDC(device->window, dc);
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            logJavaException(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AbstractDirectSoftwareRedrawer_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        SoftwareDevice *device = fromJavaPointer<SoftwareDevice *>(devicePtr);
        delete device;
    }
}
