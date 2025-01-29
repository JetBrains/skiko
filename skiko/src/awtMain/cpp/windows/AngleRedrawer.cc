#ifdef SK_ANGLE

#include <Windows.h>
#include <dwmapi.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "ganesh/GrBackendSurface.h"
#include "ganesh/GrDirectContext.h"
#include "ganesh/gl/GrGLBackendSurface.h"
#include "ganesh/gl/GrGLDirectContext.h"
#include "SkSurface.h"
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include "ganesh/gl/GrGLAssembleInterface.h"
#include "ganesh/gl/GrGLDefines.h"
#include <GL/gl.h>
#include "window_util.h"

void throwAngleException(JNIEnv *env, const char * function, const char * msg) {
    char fullMsg[1024];
    snprintf(fullMsg, sizeof(fullMsg) - 1, "Native exception in [%s], message: %s", function, msg);

    static jclass cls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/redrawer/AngleRedrawerException")));
    static jmethodID init = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;)V");

    jthrowable throwable = (jthrowable) env->NewObject(cls, init, env->NewStringUTF(fullMsg));
    env->Throw(throwable);
}

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

EGLDisplay getAngleEGLDisplay(HDC hdc)
{
    PFNEGLGETPLATFORMDISPLAYEXTPROC eglGetPlatformDisplayEXT;
    eglGetPlatformDisplayEXT = (PFNEGLGETPLATFORMDISPLAYEXTPROC) eglGetProcAddress("eglGetPlatformDisplayEXT");
    // We expect ANGLE to support this extension
    if (!eglGetPlatformDisplayEXT)
    {
        return EGL_NO_DISPLAY;
    }
    static constexpr EGLint attribs[] = {
        // We currently only support D3D11 ANGLE.
        EGL_PLATFORM_ANGLE_TYPE_ANGLE, EGL_PLATFORM_ANGLE_TYPE_D3D11_ANGLE,
        EGL_NONE, EGL_NONE
    };
    return eglGetPlatformDisplayEXT(EGL_PLATFORM_ANGLE_ANGLE, hdc, attribs);
}

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_createAngleDevice(
        JNIEnv *env, jobject redrawer, jlong platformInfoPtr, jboolean transparency)
    {
        __try
        {
            JAWT_Win32DrawingSurfaceInfo *dsi_win = fromJavaPointer<JAWT_Win32DrawingSurfaceInfo *>(platformInfoPtr);
            HWND hwnd = dsi_win->hwnd;
            HDC hdc = GetDC(hwnd);

            if (transparency)
            {
                enableTransparentWindow(hwnd);
            }

            AngleDevice *angleDevice = new AngleDevice();
            angleDevice->window = hwnd;
            angleDevice->device = hdc;
            angleDevice->display = getAngleEGLDisplay(angleDevice->device);
            if (EGL_NO_DISPLAY == angleDevice->display)
            {
                throwAngleException(env, __FUNCTION__, "Could not get display!");
                return (jlong) 0;
            }

            EGLint majorVersion;
            EGLint minorVersion;
            if (!eglInitialize(angleDevice->display, &majorVersion, &minorVersion))
            {
                throwAngleException(env, __FUNCTION__, "Could not initialize display!");
                return (jlong) 0;
            }

            static constexpr int fSampleCount = 1;
            static constexpr int sampleBuffers = fSampleCount > 1 ? 1 : 0;
            static constexpr int eglSampleCnt = fSampleCount > 1 ? fSampleCount : 0;
            static constexpr EGLint configAttribs[] = {
                // We currently only support ES3.
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_SAMPLE_BUFFERS, sampleBuffers,
                EGL_SAMPLES, eglSampleCnt,
                EGL_NONE, EGL_NONE
            };

            EGLint numConfigs;
            EGLConfig surfaceConfig;
            if (!eglChooseConfig(angleDevice->display, configAttribs, &surfaceConfig, 1, &numConfigs))
            {
                throwAngleException(env, __FUNCTION__, "Could not create choose config!");
                return (jlong) 0;
            }

            // We currently only support ES3.
            static constexpr EGLint contextAttribs[] = {
                EGL_CONTEXT_MAJOR_VERSION, 3,
                EGL_CONTEXT_MINOR_VERSION, 0,
                EGL_NONE, EGL_NONE
            };
            angleDevice->context = eglCreateContext(angleDevice->display, surfaceConfig, nullptr, contextAttribs);
            if (EGL_NO_CONTEXT == angleDevice->context)
            {
                throwAngleException(env, __FUNCTION__, "Could not create context!");
                return (jlong) 0;
            }

            angleDevice->surface = eglCreateWindowSurface(angleDevice->display, surfaceConfig, angleDevice->window, nullptr);
            if (EGL_NO_SURFACE == angleDevice->surface)
            {
                throwAngleException(env, __FUNCTION__, "Could not create surface!");
                return (jlong) 0;
            }

            if (!eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context))
            {
                throwAngleException(env, __FUNCTION__, "Could not make context current!");
                return (jlong) 0;
            }

            // For vsync we will use dwmFlush instead of swapInterval, see WindowsOpenGLRedrawer.kt
            eglSwapInterval(angleDevice->display, 0);

            sk_sp<const GrGLInterface> glInterface(GrGLMakeAssembledInterface(
                nullptr,
                [](void *ctx, const char name[]) -> GrGLFuncPtr { return eglGetProcAddress(name); }));

            angleDevice->backendContext = glInterface;

            return toJavaPointer(angleDevice);
        }
        __except(EXCEPTION_EXECUTE_HANDLER)
        {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
        return (jlong) 0;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_makeCurrent(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        if (!eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context))
        {
            throwAngleException(env, __FUNCTION__, "Could not make context current!");
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_makeAngleContext(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        sk_sp<const GrGLInterface> backendContext = angleDevice->backendContext;
        return toJavaPointer(GrDirectContexts::MakeGL(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_makeAngleRenderTarget(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        __try
        {
            AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
            angleDevice->backendContext->fFunctions.fViewport(0, 0, width, height);

            GrGLint buffer;
            angleDevice->backendContext->fFunctions.fGetIntegerv(GR_GL_FRAMEBUFFER_BINDING, &buffer);

            GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(buffer), GR_GL_RGBA8 };
            GrBackendRenderTarget renderTarget = GrBackendRenderTargets::MakeGL(width,
                                                                                height,
                                                                                0,
                                                                                8,
                                                                                glInfo);
            GrBackendRenderTarget *pRenderTarget = new GrBackendRenderTarget(renderTarget);

            return toJavaPointer(pRenderTarget);
        }
        __except(EXCEPTION_EXECUTE_HANDLER)
        {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
        return (jlong) 0;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        __try
        {
            AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
            if (!eglSwapBuffers(angleDevice->display, angleDevice->surface))
            {
                throwAngleException(env, __FUNCTION__, "Could not complete eglSwapBuffers.");
            }
            angleDevice->backendContext->fFunctions.fFinish();
        }
        __except(EXCEPTION_EXECUTE_HANDLER)
        {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_dwmFlush(
        JNIEnv *env, jobject redrawer)
    {
        DwmFlush();
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        AngleDevice *angleDevice = fromJavaPointer<AngleDevice *>(devicePtr);
        eglMakeCurrent(angleDevice->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        delete angleDevice;
    }
} // end extern C

#endif
