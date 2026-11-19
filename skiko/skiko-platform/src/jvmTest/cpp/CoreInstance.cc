#include <jni.h>

// Defined in the core library tests
extern "C" const void* skikoCoreInstance();

// The core instance this library is linked to.
extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_tests_CoreInstanceProbeKt__1nCoreInstanceSeenByPlatform
(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(skikoCoreInstance());
}
