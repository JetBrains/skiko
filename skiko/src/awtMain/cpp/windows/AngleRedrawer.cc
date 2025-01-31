#ifdef SK_ANGLE

#include <Windows.h>
#include <dwmapi.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "ganesh/gl/GrGLBackendSurface.h"
#include "ganesh/gl/GrGLDirectContext.h"
#include "SkSurface.h"
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include "gl/GrGLAssembleInterface.h"
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
        eglGetPlatformDisplayEXT = (PFNEGLGETPLATFORMDISPLAYEXTPROC) eglGetProcAddress("eglGetPlatformDisplayEXT");
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

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_createAngleDevice(
        JNIEnv *env, jobject redrawer, jlong platformInfoPtr, jboolean transparency)
    {
        __try
        {
            JAWT_Win32DrawingSurfaceInfo *dsi_win = fromJavaPointer<JAWT_Win32DrawingSurfaceInfo *>(platformInfoPtr);
            HWND windowHandle = dsi_win->hwnd;

            AngleDevice *angleDevice = new AngleDevice();
            angleDevice->window = windowHandle;
            angleDevice->device = GetDC(angleDevice->window);
            angleDevice->display = getAngleEGLDisplay(angleDevice->device);
            if (EGL_NO_DISPLAY == angleDevice->display)
            {
                throwAngleException(env, __FUNCTION__, "Could not get display!");
                return 0;
            }

            if (transparency)
            {
                enableTransparentWindow(angleDevice->window);
            }

            EGLint majorVersion;
            EGLint minorVersion;
            if (!eglInitialize(angleDevice->display, &majorVersion, &minorVersion))
            {
                throwAngleException(env, __FUNCTION__, "Could not initialize display!");
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
                throwAngleException(env, __FUNCTION__, "Could not create choose config!");
                return 0;
            }
            // We currently only support ES3.
            const EGLint contextAttribs[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
            angleDevice->context = eglCreateContext(angleDevice->display, surfaceConfig, nullptr, contextAttribs);
            if (EGL_NO_CONTEXT == angleDevice->context)
            {
                throwAngleException(env, __FUNCTION__, "Could not create context!");
                return 0;
            }
            angleDevice->surface = eglCreateWindowSurface(angleDevice->display, surfaceConfig, angleDevice->window, nullptr);
            if (EGL_NO_SURFACE == angleDevice->surface)
            {
                throwAngleException(env, __FUNCTION__, "Could not create surface!");
                return 0;
            }
            if (!eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context))
            {
                throwAngleException(env, __FUNCTION__, "Could not make context current!");
                return 0;
            }

            sk_sp<const GrGLInterface> interface(GrGLMakeAssembledInterface(
                nullptr,
                [](void *ctx, const char name[]) -> GrGLFuncPtr { return eglGetProcAddress(name); }));

            angleDevice->backendContext = interface;

            return toJavaPointer(angleDevice);
        }
        __except(EXCEPTION_EXECUTE_HANDLER)
        {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
        return (jlong) 0;
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
            eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context);
            angleDevice->backendContext->fFunctions.fViewport(0, 0, width, height);

            GrGLint buffer;
            angleDevice->backendContext->fFunctions.fGetIntegerv(GR_GL_FRAMEBUFFER_BINDING, &buffer);

            GrGLFramebufferInfo info;
            info.fFBOID = buffer;
            info.fFormat = GR_GL_RGBA8;

            GrBackendRenderTarget renderTarget = GrBackendRenderTargets::MakeGL(width,
                                                                                height,
                                                                                0,
                                                                                8,
                                                                                info);
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
            eglMakeCurrent(angleDevice->display, angleDevice->surface, angleDevice->surface, angleDevice->context);
            // For vsync we will use dwmFlush instead of swapInterval, see WindowsOpenGLRedrawer.kt
            eglSwapInterval(angleDevice->display, 0);
            if (!eglSwapBuffers(angleDevice->display, angleDevice->surface))
            {
                throwAngleException(env, __FUNCTION__, "Could not complete eglSwapBuffers.");
            }
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
        delete angleDevice;
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_AngleApi_glGetString(
        JNIEnv *env, jobject object, jint value)
    {
        typedef const GLubyte *(* PFNGLGETSTRINGPROC) (GLenum name);
        static auto glGetString = (PFNGLGETSTRINGPROC) eglGetProcAddress("glGetString");
        const char *content = reinterpret_cast<const char *>(glGetString(value));
        return env->NewStringUTF(content);
    }
} // end extern C

#endif
