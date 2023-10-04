#include <windows.h>
#include <gl/GL.h>
#include <jawt_md.h>

typedef void (*PROC_glFinish) (void);
typedef void (*PROC_glGetIntegerv) (GLenum pname, GLint *data);
typedef const GLubyte *(*PROC_glGetString) (GLenum name);
typedef HGLRC (WINAPI * PROC_wglCreateContext) (HDC hDc);
typedef BOOL (WINAPI * PROC_wglDeleteContext) (HGLRC oldContext);
typedef PROC (WINAPI * PROC_wglGetProcAddress) (LPCSTR lpszProc);
typedef BOOL (WINAPI * PROC_wglMakeCurrent) (HDC hDc, HGLRC newContext);
typedef HGLRC (WINAPI * PROC_wglGetCurrentContext) (void);

class OpenGL32Library {
public:
    HINSTANCE lib;

    PROC_glFinish glFinish;
    PROC_glGetIntegerv glGetIntegerv;
    PROC_glGetString glGetString;
    PROC_wglCreateContext wglCreateContext;
    PROC_wglDeleteContext wglDeleteContext;
    PROC_wglGetProcAddress wglGetProcAddress;
    PROC_wglMakeCurrent wglMakeCurrent;
    PROC_wglGetCurrentContext wglGetCurrentContext;

    OpenGL32Library(HINSTANCE lib) {
        this->lib = lib;
        glFinish = (PROC_glFinish) GetProcAddress(lib, "glFinish");
        glGetIntegerv = (PROC_glGetIntegerv) GetProcAddress(lib, "glGetIntegerv");
        glGetString = (PROC_glGetString) GetProcAddress(lib, "glGetString");
        wglCreateContext = (PROC_wglCreateContext) GetProcAddress(lib, "wglCreateContext");
        wglDeleteContext = (PROC_wglDeleteContext) GetProcAddress(lib, "wglDeleteContext");
        wglGetProcAddress = (PROC_wglGetProcAddress) GetProcAddress(lib, "wglGetProcAddress");
        wglMakeCurrent = (PROC_wglMakeCurrent) GetProcAddress(lib, "wglMakeCurrent");
        wglGetCurrentContext = (PROC_wglGetCurrentContext) GetProcAddress(lib, "wglGetCurrentContext");
    }
};

OpenGL32Library* openGL32Library = nullptr;

extern "C"
{
    void glFinish(void) {
        openGL32Library->glFinish();
    }

    void glGetIntegerv(GLenum pname, GLint *data) {
        openGL32Library->glGetIntegerv(pname, data);
    }

    const GLubyte * glGetString(GLenum name) {
        return openGL32Library->glGetString(name);
    }

    HGLRC WINAPI wglCreateContext(HDC hDc) {
        return openGL32Library->wglCreateContext(hDc);
    }

    BOOL WINAPI wglDeleteContext(HGLRC oldContext) {
        return openGL32Library->wglDeleteContext(oldContext);
    }

    PROC WINAPI wglGetProcAddress(LPCSTR lpszProc) {
        return openGL32Library->wglGetProcAddress(lpszProc);
    }

    BOOL WINAPI wglMakeCurrent(HDC hDc, HGLRC newContext) {
        return openGL32Library->wglMakeCurrent(hDc, newContext);
    }

    HGLRC WINAPI wglGetCurrentContext() {
        return openGL32Library->wglGetCurrentContext();
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_OpenGLLibraryKt_loadOpenGLLibraryWindows(JNIEnv *env, jobject obj) {
        if (openGL32Library == 0) {
            HINSTANCE lib = LoadLibrary("opengl32.dll");
            if (lib) {
                openGL32Library = new OpenGL32Library(lib);
            }
        }
        return openGL32Library != 0;
    }
}