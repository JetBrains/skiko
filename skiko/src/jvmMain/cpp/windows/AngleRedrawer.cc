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
#include <GL/gl.h>

class AngleDevice
{
public:
    HWND window;
    HDC device;
    EGLDisplay display = EGL_NO_DISPLAY;
    EGLContext context = EGL_NO_CONTEXT;
    EGLSurface surface = EGL_NO_SURFACE;
    sk_sp<const GrGLInterface> backendContext;
    ~AngleDevice()
    {
        backendContext.reset(nullptr);
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (EGL_NO_CONTEXT != context)
        {
            eglDestroyContext(display, context);
        }
        if (EGL_NO_SURFACE != surface)
        {
            eglDestroySurface(display, surface);
        }
        if (EGL_NO_DISPLAY != display)
        {
            eglTerminate(display);
        }
    }
};

extern "C"
{
    EGLDisplay getAngleEGLDisplay(HDC hdc)
    {
        PFNEGLGETPLATFORMDISPLAYEXTPROC eglGetPlatformDisplayEXT;
        eglGetPlatformDisplayEXT = (PFNEGLGETPLATFORMDISPLAYEXTPROC)eglGetProcAddress("eglGetPlatformDisplayEXT");
        // We expect ANGLE to support this extension
        if (!eglGetPlatformDisplayEXT)
        {
            return EGL_NO_DISPLAY;
        }
        // We currently only support D3D11 ANGLE.
        static constexpr EGLint kType = EGL_PLATFORM_ANGLE_TYPE_D3D11_ANGLE;
        static constexpr EGLint attribs[] = {EGL_PLATFORM_ANGLE_TYPE_ANGLE, kType, EGL_NONE};
        return eglGetPlatformDisplayEXT(EGL_PLATFORM_ANGLE_ANGLE, hdc, attribs);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_createAngleDevice(
        JNIEnv *env, jobject redrawer, jlong contentHandle)
    {
        AngleDevice *angleDevice = new AngleDevice();
        angleDevice->window = (HWND)contentHandle;
        angleDevice->device = GetDC(angleDevice->window);
        angleDevice->display = getAngleEGLDisplay(angleDevice->device);
        if (EGL_NO_DISPLAY == angleDevice->display)
        {
            fprintf(stderr, "Could not get display!\n");
            return 0;
        }

        EGLint majorVersion;
        EGLint minorVersion;
        if (!eglInitialize(angleDevice->display, &majorVersion, &minorVersion))
        {
            fprintf(stderr, "Could not initialize display!\n");
            return 0;
        }
        EGLint numConfigs;
        const int sampleBuffers = 1;
        const EGLint configAttribs[] = {EGL_RENDERABLE_TYPE,
                                        // We currently only support ES3.
                                        EGL_OPENGL_ES3_BIT,
                                        EGL_RED_SIZE,
                                        8,
                                        EGL_GREEN_SIZE,
                                        8,
                                        EGL_BLUE_SIZE,
                                        8,
                                        EGL_ALPHA_SIZE,
                                        8,
                                        EGL_SAMPLE_BUFFERS,
                                        sampleBuffers,
                                        EGL_SAMPLES,
                                        0,
                                        EGL_NONE};

        EGLConfig surfaceConfig;
        if (!eglChooseConfig(angleDevice->display, configAttribs, &surfaceConfig, 1, &numConfigs))
        {
            fprintf(stderr, "Could not create choose config!\n");
            return 0;
        }
        // We currently only support ES3.
        const EGLint contextAttribs[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
        angleDevice->context = eglCreateContext(angleDevice->display, surfaceConfig, nullptr, contextAttribs);
        if (EGL_NO_CONTEXT == angleDevice->context)
        {
            fprintf(stderr, "Could not create context!\n");
            return 0;
        }
        angleDevice->surface = eglCreateWindowSurface(angleDevice->display, surfaceConfig, angleDevice->window, nullptr);
        if (EGL_NO_SURFACE == angleDevice->surface)
        {
            fprintf(stderr, "Could not create surface!\n");
            return 0;
        }
        if (!eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context))
        {
            fprintf(stderr, "Could not make contxt current!\n");
            return 0;
        }

        sk_sp<const GrGLInterface> interface(GrGLMakeAssembledInterface(
            nullptr,
            [](void *ctx, const char name[]) -> GrGLFuncPtr { return eglGetProcAddress(name); }));

        angleDevice->backendContext = interface;

        return toJavaPointer(angleDevice);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_makeAngleContext(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        sk_sp<const GrGLInterface> backendContext = angleDevice->backendContext;
        return toJavaPointer(GrDirectContext::MakeGL(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_makeAngleRenderTarget(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context);
        angleDevice->backendContext->fFunctions.fViewport(0, 0, width, height);

        GrGLint buffer;
        angleDevice->backendContext->fFunctions.fGetIntegerv(GR_GL_FRAMEBUFFER_BINDING, &buffer);

        GrGLFramebufferInfo info;
        info.fFBOID = buffer;
        info.fFormat = GR_GL_RGBA8;

        GrBackendRenderTarget *renderTarget = new GrBackendRenderTarget(width,
                                                                        height,
                                                                        0,
                                                                        8,
                                                                        info);
        return toJavaPointer(renderTarget);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_resizeBuffers(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jboolean isVsyncEnabled)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context);
        eglSwapInterval(angleDevice->display, (int)isVsyncEnabled); // wait for vsync
        if (!eglSwapBuffers(angleDevice->display, angleDevice->surface))
        {
            fprintf(stderr, "Could not complete eglSwapBuffers.\n");
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawer_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        delete angleDevice;
    }
} // end extern C

#endif