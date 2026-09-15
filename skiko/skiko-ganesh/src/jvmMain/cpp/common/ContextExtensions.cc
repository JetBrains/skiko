#include <jni.h>
#include "SkCanvas.h"
#include "SkSurface.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_ganesh_ContextExtensionsKt__1nGetCanvasRecordingContext
  (JNIEnv* env, jclass jclass, jlong canvasPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    return reinterpret_cast<jlong>(canvas->recordingContext());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_ganesh_ContextExtensionsKt__1nGetSurfaceRecordingContext
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->recordingContext());
}