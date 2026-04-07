#include <jni.h>
#include "../../interop.hh"
#include "gpu/graphite/Recorder.h"

static void deleteRecording(skgpu::graphite::Recording* recording) {
    delete recording;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_RecordingKt_Recording_1nGetFinalizer
        (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRecording));
}