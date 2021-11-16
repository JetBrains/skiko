#include "common.h"
#include <stdint.h>

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5(KNativePointer byteArray) {
    uint8_t *bytes = reinterpret_cast<uint8_t*>(byteArray);
    bytes[0] = 1;
    bytes[1] = 2;
    bytes[2] = 3;
    bytes[3] = 4;
    bytes[4] = 5;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5(KNativePointer floatArray) {
    float *floats = reinterpret_cast<float*>(floatArray);
    floats[0] = 0.0;
    floats[1] = 1.1;
    floats[2] = 2.2;
    floats[3] = 3.3;
    floats[4] = -4.4;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5(KNativePointer shortArray) {
    short *shorts = reinterpret_cast<short*>(shortArray);
    shorts[0] = 0;
    shorts[1] = 1;
    shorts[2] = 2;
    shorts[3] = -3;
    shorts[4] = 4;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5(KNativePointer intArray) {
    int *ints = reinterpret_cast<int*>(intArray);
    ints[0] = 0;
    ints[1] = 1;
    ints[2] = -22;
    ints[3] = 3;
    ints[4] = 4;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5(KNativePointer doubleArray) {
    double *doubles = reinterpret_cast<double*>(doubleArray);
    doubles[0] = -0.001;
    doubles[1] = 0.00222;
    doubles[2] = 2.71828;
    doubles[3] = 3.1415;
    doubles[4] = 10000000.9991;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__nStringByIndex(KInt index) {
    switch (index) {
        case 0: return new SkString("Hello");
        case 1: return new SkString("Привет");
        case 2: return new SkString("你好");
        default:
            TODO("unknown");
            return nullptr;
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nWriteArraysOfInts(KNativePointer* arrayOfIntArray) {
    // hardcoded length is ok for testing purposes
    size_t len = 3; //(*env)->GetArrayLength(env, arrayOfIntArray);

    int *mem = reinterpret_cast<int *>(malloc(3 * 4 * 4)); // 3 arrays. each array consists of 4 ints

    for (int i = 0; i < len; i++) {
        KNativePointer* array = reinterpret_cast<KNativePointer*>(arrayOfIntArray[i]);
        int *ints = reinterpret_cast<int*>(array);
        for (int j = 0; j < 4; j++) {
            mem[(i * 4) + j] = ints[j];
        }
    }

    return reinterpret_cast<KNativePointer>(mem);
}

struct SkikoTestGlContext;
SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext();
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr);
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr);
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr);

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer() {
    return reinterpret_cast<KNativePointer>(org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext);
}

#ifdef SK_GL
#ifdef __linux__

#define SKIKO_TEST_GL_INCLUDED

#include <GL/glx.h>
#include <iostream>

#include "include/gpu/gl/GrGLInterface.h"
#include "include/gpu/gl/egl/GrGLMakeEGLInterface.h"

struct SkikoTestGlContext {
    Display* display;
    GLXDrawable surface;
    GLXContext context;
};

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext() {
   const int glxContextAttribs[] {
        GLX_DRAWABLE_TYPE, GLX_PBUFFER_BIT,
        GLX_RENDER_TYPE, GLX_RGBA_BIT,
        GLX_DOUBLEBUFFER, True,
        GLX_RED_SIZE, 8,
        GLX_GREEN_SIZE, 8,
        GLX_BLUE_SIZE, 8,
        None
    };

    const int glxPBufferAttribs[] {
        GLX_PBUFFER_WIDTH, 1280,
        GLX_PBUFFER_HEIGHT, 720,
        None
    };

    Display* display = XOpenDisplay(nullptr);
    SKIKO_ASSERT(display, "Failed to connect to Xserver");

    int numConfigs = 0;
    GLXFBConfig* fbConfigs = glXChooseFBConfig(display, DefaultScreen(display), glxContextAttribs, &numConfigs);
    SKIKO_ASSERT(fbConfigs != nullptr && numConfigs > 0, "No suitable fbconfig available");

    GLXDrawable surface = glXCreatePbuffer(display, fbConfigs[0], glxPBufferAttribs);
    SKIKO_ASSERT(surface, "Failed to create surface");

    GLXContext context = glXCreateNewContext(display, fbConfigs[0], GLX_RGBA_TYPE, nullptr, True);
    SKIKO_ASSERT(context, "Failed to create context");

    XFree(fbConfigs);

    return reinterpret_cast<KInteropPointer>(new SkikoTestGlContext { display, surface, context });
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    glXMakeContextCurrent(instance->display, None, None, nullptr);
    glXDestroyPbuffer(instance->display, instance->surface);
    glXDestroyContext(instance->display, instance->context);
    XCloseDisplay(instance->display);
    delete instance;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    glXMakeContextCurrent(instance->display, instance->surface, instance->surface, instance->context);
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    glXSwapBuffers(instance->display, instance->surface);
}

#endif // __linux__
#endif // SK_GL

#ifndef SKIKO_TEST_GL_INCLUDED
SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext() {
    TODO("OpenGl context is not supported for this platform");
    return nullptr;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr) {}
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr) {}
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr) {}
#endif // SKIKO_TEST_GL_INCLUDED