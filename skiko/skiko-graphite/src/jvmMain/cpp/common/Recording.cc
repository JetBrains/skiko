#include <jni.h>

#include "include/gpu/graphite/Recording.h"

static void deleteRecording(skgpu::graphite::Recording* recording) {
    delete recording;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_RecordingKt__1nGetRecordingFinalizer(JNIEnv*, jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRecording));
}
