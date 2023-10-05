#include <windows.h>
#include <gl/GL.h>
#include <jawt_md.h>

namespace OpenGL32Library {
    typedef void (*PROC_glFinish) (void);
    typedef void (*PROC_glGetIntegerv) (GLenum pname, GLint *data);
    typedef const GLubyte *(*PROC_glGetString) (GLenum name);
    typedef HGLRC (WINAPI * PROC_wglCreateContext) (HDC hDc);
    typedef BOOL (WINAPI * PROC_wglDeleteContext) (HGLRC oldContext);
    typedef PROC (WINAPI * PROC_wglGetProcAddress) (LPCSTR lpszProc);
    typedef BOOL (WINAPI * PROC_wglMakeCurrent) (HDC hDc, HGLRC newContext);
    typedef HGLRC (WINAPI * PROC_wglGetCurrentContext) (void);

    bool isLoaded = false;
    HINSTANCE lib = 0;

    PROC_glFinish glFinish;
    PROC_glGetIntegerv glGetIntegerv;
    PROC_glGetString glGetString;
    PROC_wglCreateContext wglCreateContext;
    PROC_wglDeleteContext wglDeleteContext;
    PROC_wglGetProcAddress wglGetProcAddress;
    PROC_wglMakeCurrent wglMakeCurrent;
    PROC_wglGetCurrentContext wglGetCurrentContext;

    // Loads the opengl32.dll into memory
    // If loading isn't successful, skip loading the next time
    // Isn't thread safe, call it in synchronized block
    void load() {
        if (!isLoaded) {
            isLoaded = true;
            lib = LoadLibrary("opengl32.dll");
            if (lib) {
                glFinish = (PROC_glFinish) GetProcAddress(lib, "glFinish");
                glGetIntegerv = (PROC_glGetIntegerv) GetProcAddress(lib, "glGetIntegerv");
                glGetString = (PROC_glGetString) GetProcAddress(lib, "glGetString");
                wglCreateContext = (PROC_wglCreateContext) GetProcAddress(lib, "wglCreateContext");
                wglDeleteContext = (PROC_wglDeleteContext) GetProcAddress(lib, "wglDeleteContext");
                wglGetProcAddress = (PROC_wglGetProcAddress) GetProcAddress(lib, "wglGetProcAddress");
                wglMakeCurrent = (PROC_wglMakeCurrent) GetProcAddress(lib, "wglMakeCurrent");
                wglGetCurrentContext = (PROC_wglGetCurrentContext) GetProcAddress(lib, "wglGetCurrentContext");
            }
        }
    }
};

extern "C" {
    void glFinish(void) {
        OpenGL32Library::glFinish();
    }

    void glGetIntegerv(GLenum pname, GLint *data) {
        OpenGL32Library::glGetIntegerv(pname, data);
    }

    const GLubyte * glGetString(GLenum name) {
        return OpenGL32Library::glGetString(name);
    }

    HGLRC WINAPI wglCreateContext(HDC hDc) {
        return OpenGL32Library::wglCreateContext(hDc);
    }

    BOOL WINAPI wglDeleteContext(HGLRC oldContext) {
        return OpenGL32Library::wglDeleteContext(oldContext);
    }

    PROC WINAPI wglGetProcAddress(LPCSTR lpszProc) {
        return OpenGL32Library::wglGetProcAddress(lpszProc);
    }

    BOOL WINAPI wglMakeCurrent(HDC hDc, HGLRC newContext) {
        return OpenGL32Library::wglMakeCurrent(hDc, newContext);
    }

    HGLRC WINAPI wglGetCurrentContext() {
        return OpenGL32Library::wglGetCurrentContext();
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_OpenGLLibraryKt_loadOpenGLLibraryWindows(JNIEnv *env, jobject obj) {
        OpenGL32Library::load();
        return OpenGL32Library::lib != 0;
    }
}