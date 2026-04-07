#include <jni.h>
#include "../../interop.hh"
#include "include/gpu/graphite/Context.h"
#include "gpu/graphite/Recorder.h"

static void deleteRecorder(skgpu::graphite::Recorder* rt) {
    delete rt;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_RecorderKt_Recorder_1nGetFinalizer
        (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRecorder));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_Recorder_1nSnap
        (JNIEnv* env, jclass jclass, jlong recorderPtr) {
    skgpu::graphite::Recorder *recorder = reinterpret_cast<skgpu::graphite::Recorder*>(recorderPtr);

    std::unique_ptr<skgpu::graphite::Recording> recording = recorder->snap();

    return reinterpret_cast<jlong>(recording.release());
}