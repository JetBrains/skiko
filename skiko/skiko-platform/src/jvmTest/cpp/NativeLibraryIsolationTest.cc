#include <jni.h>

extern "C" jclass JNICALL Java_org_jetbrains_skiko_SplitLibraryProbe_coreClass(JNIEnv*, jobject);
#ifdef _WIN32
extern "C" jclass JNICALL Java_org_jetbrains_skiko_SplitLibraryProbe_ganeshCoreClass(JNIEnv*, jobject);
#endif

// Call through platform's native dependency, rather than the JVM's native-method lookup.
extern "C" JNIEXPORT jclass JNICALL Java_org_jetbrains_skiko_SplitLibraryProbe_coreImageInfoClass
  (JNIEnv* env, jobject receiver) {
    jclass coreClass = Java_org_jetbrains_skiko_SplitLibraryProbe_coreClass(env, receiver);
#ifdef _WIN32
    // Exercise both platform -> core and platform -> Ganesh -> core delay-load paths.
    jclass ganeshCoreClass = Java_org_jetbrains_skiko_SplitLibraryProbe_ganeshCoreClass(env, receiver);
    if (!env->IsSameObject(coreClass, ganeshCoreClass)) {
        env->ThrowNew(env->FindClass("java/lang/AssertionError"), "Ganesh resolved a different core DLL");
        return nullptr;
    }
#endif
    return coreClass;
}
