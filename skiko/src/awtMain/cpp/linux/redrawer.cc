#include <jawt_md.h>
#include <GL/gl.h>

#define EGL_EGL_PROTOTYPES 0

#include <EGL/egl.h>
#include <dlfcn.h>
#include <iostream>
#include "jni_helpers.h"

class EGLContextWrapper {
public:
    EGLContextWrapper(EGLDisplay _display, EGLContext _context, EGLConfig _config, EGLSurface _surface, Window _window,
                      bool _transparency, void *const egl) :
            eglDisplay(_display), eglContext(_context), eglConfig(_config), eglSurface(_surface), window(_window),
            transparency(_transparency), egl(egl) {};

    ~EGLContextWrapper() {
        const auto eglDestroyContext = getProc<PFNEGLDESTROYCONTEXTPROC>("eglDestroyContext");
        eglDestroyContext(eglDisplay, eglContext);
        dlclose(egl);
    };

    static EGLContextWrapper *create(Display *display, Window window, bool transparency) {
        const auto egl = dlopen("libEGL.so", RTLD_LAZY);
        if (!egl) {
            std::cerr << "Could not load EGL: " << dlerror() << std::endl;
            return nullptr;
        }

        const auto eglGetDisplay = (PFNEGLGETDISPLAYPROC) dlsym(egl, "eglGetDisplay");
        const auto eglInitialize = (PFNEGLINITIALIZEPROC) dlsym(egl, "eglInitialize");
        const auto eglBindAPI = (PFNEGLBINDAPIPROC) dlsym(egl, "eglBindAPI");
        const auto eglChooseConfig = (PFNEGLCHOOSECONFIGPROC) dlsym(egl, "eglChooseConfig");
        const auto eglCreateContext = (PFNEGLCREATECONTEXTPROC) dlsym(egl, "eglCreateContext");
        const auto eglCreateWindowSurface = (PFNEGLCREATEWINDOWSURFACEPROC) dlsym(egl, "eglCreateWindowSurface");

        EGLDisplay eglDisplay = eglGetDisplay(display);
        if (eglDisplay == EGL_NO_DISPLAY) {
            std::cerr << "Could not get egl display" << std::endl;
            return nullptr;
        }

        EGLint major, minor;
        if (!eglInitialize(eglDisplay, &major, &minor)) {
            std::cerr << "Could not initialize egl" << std::endl;
            return nullptr;
        }

        if (!eglBindAPI(EGL_OPENGL_ES_API)) {
            std::cerr << "Could not bind OpenGL API" << std::endl;
            return nullptr;
        }

        EGLint configConstraints[] = {
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, transparency ? 8 : 0,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_CONFIG_CAVEAT, EGL_NONE,
                EGL_NONE,
        };

        EGLConfig eglConfig;
        EGLint numConfigs;
        if (!eglChooseConfig(eglDisplay, configConstraints, &eglConfig, 1, &numConfigs)) {
            std::cerr << "Could not choose egl config" << std::endl;
            return nullptr;
        }

        static const EGLint contextAttribs[] = {
                EGL_CONTEXT_MAJOR_VERSION, 3,
                EGL_NONE
        };
        EGLContext eglContext = eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextAttribs);
        if (eglContext == EGL_NO_CONTEXT) {
            std::cerr << "Could not create egl context" << std::endl;
            return nullptr;
        }

        EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window, nullptr);
        if (eglSurface == EGL_NO_SURFACE) {
            std::cerr << "Could not create egl surface" << std::endl;
            return nullptr;
        }

        return new EGLContextWrapper(eglDisplay, eglContext, eglConfig, eglSurface, window, transparency, egl);
    };

    template<typename T>
    T getProc(const std::string &name) {
        const auto func = (T) dlsym(egl, name.c_str());
        return func;
    }

    bool makeCurrent() {
        const auto eglMakeCurrent = getProc<PFNEGLMAKECURRENTPROC>("eglMakeCurrent");
        return eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    }

    bool swapBuffers() {
        const auto eglSwapBuffers = getProc<PFNEGLSWAPBUFFERSPROC>("eglSwapBuffers");
        return eglSwapBuffers(eglDisplay, eglSurface);
    }

    bool setSwapInterval(int interval) {
        const auto eglSwapInterval = getProc<PFNEGLSWAPINTERVALPROC>("eglSwapInterval");
        return eglSwapInterval(eglDisplay, interval);
    }

    EGLDisplay getDisplay() const {
        return eglDisplay;
    }

    EGLContext getContext() const {
        return eglContext;
    }

    EGLConfig getConfig() const {
        return eglConfig;
    }

private:
    EGLDisplay eglDisplay{EGL_NO_DISPLAY};
    EGLContext eglContext{EGL_NO_CONTEXT};
    EGLConfig eglConfig{nullptr};
    EGLSurface eglSurface{EGL_NO_SURFACE};
    Window window;
    bool transparency;
    void *const egl;
};

extern "C"
{
JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_setSwapInterval(JNIEnv *env, jobject redrawer, jlong contextPtr,
                                                                        jint interval) {
    auto *context = fromJavaPointer<EGLContextWrapper *>(contextPtr);

    context->setSwapInterval(interval);
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_swapBuffers(JNIEnv *env, jobject redrawer, jlong contextPtr) {
    auto *context = fromJavaPointer<EGLContextWrapper *>(contextPtr);

    context->swapBuffers();
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_makeCurrent(JNIEnv *env, jobject redrawer, jlong contextPtr) {
    auto *context = fromJavaPointer<EGLContextWrapper *>(contextPtr);

    context->makeCurrent();
}

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_createContext(JNIEnv *env, jobject redrawer, jlong displayPtr,
                                                                      jlong windowPtr, jboolean transparency) {
    auto *display = fromJavaPointer<Display *>(displayPtr);
    auto window = fromJavaPointer<Window>(windowPtr);
    if (!display) return 0;

    auto eglContext = EGLContextWrapper::create(display, window, transparency);
    return toJavaPointer(eglContext);
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_destroyContext(JNIEnv *env, jobject redrawer,
                                                                       jlong contextPtr) {
    auto *context = fromJavaPointer<EGLContextWrapper *>(contextPtr);

    if (context) {
        delete context;
    }
}
}
