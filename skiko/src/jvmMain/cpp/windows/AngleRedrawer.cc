#ifdef SK_ANGLE
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "SkSurface.h"
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include "gl/GrGLAssembleInterface.h"
#include "gl/GrGLDefines.h"

class AngleDevice
{
public:
    HDC device;
    EGLDisplay display = EGL_NO_DISPLAY;
    EGLContext context = EGL_NO_CONTEXT;
    EGLSurface surface = EGL_NO_SURFACE;
    ~AngleDevice()
    {

    }
};

extern "C"
{
    EGLDisplay getAngleEGLDisplay(HDC hdc) {
        PFNEGLGETPLATFORMDISPLAYEXTPROC eglGetPlatformDisplayEXT;
        eglGetPlatformDisplayEXT = (PFNEGLGETPLATFORMDISPLAYEXTPROC)GetProcAddress(GetModuleHandle(NULL), "eglGetPlatformDisplayEXT");
        // We expect ANGLE to support this extension
        if (!eglGetPlatformDisplayEXT) {
            return EGL_NO_DISPLAY;
        }
        // We currently only support D3D11 ANGLE.
        static constexpr EGLint kType = EGL_PLATFORM_ANGLE_TYPE_D3D11_ANGLE;
        static constexpr EGLint attribs[] = {EGL_PLATFORM_ANGLE_TYPE_ANGLE, kType, EGL_NONE};
        return eglGetPlatformDisplayEXT(EGL_PLATFORM_ANGLE_ANGLE, hdc, attribs);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_createAngleDevice(
        JNIEnv *env, jobject redrawer, jlong windowHandle)
    {
        AngleDevice *angleDevice = new AngleDevice();
        angleDevice->device = GetDC((HWND) windowHandle);
        angleDevice->display = getAngleEGLDisplay(angleDevice->device);
        if (EGL_NO_DISPLAY == angleDevice->display) {
            return 0;
        }

        return toJavaPointer(angleDevice);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_makeAngleContext(
        JNIEnv* env, jobject redrawer, jlong devicePtr)
    {
        return 0;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_makeAngleRenderTarget(
        JNIEnv * env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        return 0;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_resizeBuffers(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {

    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jlong surfacePtr, jboolean isVsyncEnabled)
    {

    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice*>(devicePtr);
        delete angleDevice;
    }
} // end extern C

#endif