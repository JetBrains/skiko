#include <windows.h>
#include <gl/GL.h>
#include <jawt_md.h>
#include "exceptions_handler.h"

static HINSTANCE OpenGL32Library = nullptr;

extern "C" {
    void glFinish(void) {
        typedef void (*PROC_glFinish) (void);
        static auto glFinish = (PROC_glFinish) GetProcAddress(OpenGL32Library, "glFinish");
        glFinish();
    }

    void glGetIntegerv(GLenum pname, GLint *data) {
        typedef void (*PROC_glGetIntegerv) (GLenum pname, GLint *data);
        static auto glGetIntegerv = (PROC_glGetIntegerv) GetProcAddress(OpenGL32Library, "glGetIntegerv");
        glGetIntegerv(pname, data);
    }

    const GLubyte * glGetString(GLenum name) {
        typedef const GLubyte *(*PROC_glGetString) (GLenum name);
        static auto glGetString = (PROC_glGetString) GetProcAddress(OpenGL32Library, "glGetString");
        return glGetString(name);
    }

    HGLRC WINAPI wglCreateContext(HDC hDc) {
        typedef HGLRC (WINAPI * PROC_wglCreateContext) (HDC hDc);
        static auto wglCreateContext = (PROC_wglCreateContext) GetProcAddress(OpenGL32Library, "wglCreateContext");
        return wglCreateContext(hDc);
    }

    BOOL WINAPI wglDeleteContext(HGLRC oldContext) {
        typedef BOOL (WINAPI * PROC_wglDeleteContext) (HGLRC oldContext);
        static auto wglDeleteContext = (PROC_wglDeleteContext) GetProcAddress(OpenGL32Library, "wglDeleteContext");
        return wglDeleteContext(oldContext);
    }

    PROC WINAPI wglGetProcAddress(LPCSTR lpszProc) {
        typedef PROC (WINAPI * PROC_wglGetProcAddress) (LPCSTR lpszProc);
        static auto wglGetProcAddress = (PROC_wglGetProcAddress) GetProcAddress(OpenGL32Library, "wglGetProcAddress");
        return wglGetProcAddress(lpszProc);
    }

    BOOL WINAPI wglMakeCurrent(HDC hDc, HGLRC newContext) {
        typedef BOOL (WINAPI * PROC_wglMakeCurrent) (HDC hDc, HGLRC newContext);
        static auto wglMakeCurrent = (PROC_wglMakeCurrent) GetProcAddress(OpenGL32Library, "wglMakeCurrent");
        return wglMakeCurrent(hDc, newContext);
    }

    HGLRC WINAPI wglGetCurrentContext() {
        typedef HGLRC (WINAPI * PROC_wglGetCurrentContext) (void);
        static auto wglGetCurrentContext = (PROC_wglGetCurrentContext) GetProcAddress(OpenGL32Library, "wglGetCurrentContext");
        return wglGetCurrentContext();
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLLibrary_1jvmKt_loadOpenGLLibraryWindows(JNIEnv *env, jobject obj) {
        OpenGL32Library = LoadLibrary("opengl32.dll");
        if (OpenGL32Library == nullptr) {
            auto code = GetLastError();
            throwJavaRenderExceptionByErrorCode(env, __FUNCTION__, code);
        }
    }
}