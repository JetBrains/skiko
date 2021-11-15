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

#include <EGL/egl.h>

struct SkikoTestGlContext {
    EGLDisplay display;
    EGLSurface surface;
    EGLContext context;
};

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext() {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    eglInitialize(display, nullptr, nullptr);

    EGLConfig config;
    EGLint configCount;
    const EGLint attributes[] {
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_NONE
    };
    eglChooseConfig(display, attributes, &config, 1, &configCount);
    std::cerr << "EGL config count " << configCount << std::endl;
    EGLContext context = eglCreateContext(display, config, EGL_NO_CONTEXT, nullptr);
    if (context == EGL_NO_CONTEXT) {
        TODO("Failed to create context");
    }

    EGLint surfaceAttributes[] {
        EGL_WIDTH, 1280,
        EGL_HEIGHT, 720,
        EGL_NONE
    };
    EGLSurface surface = eglCreatePbufferSurface(display, context, surfaceAttributes);
    if (surface == EGL_NO_SURFACE) {
        TODO("Failed to create surface");
    }

    SkikoTestGlContext* result = new SkikoTestGlContext { display, surface, context };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    eglMakeCurrent(instance->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroySurface(instance->display, instance->surface);
    eglDestroyContext(instance->display, instance->context);
    delete instance;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    eglMakeCurrent(instance->display, instance->surface, instance->surface, instance->context);
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    eglSwapBuffers(instance->display, instance->surface);
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