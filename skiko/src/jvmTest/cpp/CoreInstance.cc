#include <jni.h>

// Exists once per copy of the core library, so its address identifies the copy.
static char coreInstance;

extern "C" JNIEXPORT const void* skikoCoreInstance() {
    return &coreInstance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_tests_CoreInstanceProbeKt__1nCoreInstance
(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(skikoCoreInstance());
}
