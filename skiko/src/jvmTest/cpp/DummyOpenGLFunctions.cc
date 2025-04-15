#include <cassert>
#include <jni.h>
#include <cstdio>
#include <cstring>

static void fakeGlDummy() {
    std::printf("!!!dummy called\n");
    assert(false);
}

typedef unsigned int GLenum;
typedef int GLint;

static const char *fakeGlGetString(GLenum name) {
    constexpr GLenum GL_VERSION = 0x1F02;
    constexpr GLenum GL_EXTENSIONS = 0x1F03;
    constexpr GLenum GL_SHADING_LANGUAGE_VERSION = 0x8B8C;

    switch (name) {
        case GL_VERSION: return "OpenGL ES 2.0 Custom";
        case GL_SHADING_LANGUAGE_VERSION: return "OpenGL ES GLSL ES 2.00";
        case GL_EXTENSIONS: return "GL_EXT_framebuffer_object";
        default: return "";
    }
}

static GLenum fakeGlGetError() {
    return 0;
}

static void fakeGlGetIntegerv(GLenum pname, GLint *data) {
    *data = 0;
}

static void fakeGlFinish(){}

typedef void(*GLFuncPtr)();

static GLFuncPtr GetGLFakeFunc(void* ctx, const char* name) {
    if (ctx != nullptr) {
        return nullptr;
    }

    if (0 == std::strcmp(name, "glFinish")) { return fakeGlFinish; }
    if (0 == std::strcmp(name, "glGetError")) { return reinterpret_cast<GLFuncPtr>(fakeGlGetError); }
    if (0 == std::strcmp(name, "glGetIntegerv")) { return reinterpret_cast<GLFuncPtr>(fakeGlGetIntegerv); }
    if (0 == std::strcmp(name, "glGetString")) { return reinterpret_cast<GLFuncPtr>(fakeGlGetString); }
    if (0 == std::strcmp(name, "eglGetCurrentDisplay")) { return nullptr; }
    if (0 == std::strcmp(name, "glGetStringi")) { return nullptr; }

    return fakeGlDummy;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextTest_1jvmKt__1nGetMakeGLAssembledInterfaceFunc
(JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(GetGLFakeFunc);
}
