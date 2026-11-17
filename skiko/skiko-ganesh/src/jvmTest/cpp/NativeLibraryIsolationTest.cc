#include <jni.h>

extern "C" jclass JNICALL Java_org_jetbrains_skiko_SplitLibraryProbe_coreClass(JNIEnv*, jobject);

extern "C" JNIEXPORT jclass JNICALL Java_org_jetbrains_skiko_SplitLibraryProbe_ganeshCoreClass
  (JNIEnv* env, jobject receiver) {
    return Java_org_jetbrains_skiko_SplitLibraryProbe_coreClass(env, receiver);
}
