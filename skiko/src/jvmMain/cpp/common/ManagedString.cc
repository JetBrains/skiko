#include <jni.h>
#include "interop.hh"
#include "mppinterop.h"
#include "SkString.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ManagedStringKt_ManagedString_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&skikoMpp::finalizers::deleteString));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nMake
  (JNIEnv* env, jclass jclass, jstring textStr) {
    SkString* text = new SkString(skString(env, textStr));
    return reinterpret_cast<jlong>(text);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nStringSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    return instance->size();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nStringData
  (JNIEnv* env, jclass jclass, jlong ptr, jbyteArray array, jint size) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    jbyte* bytes = env->GetByteArrayElements(array, NULL);
    if (bytes != nullptr) {
        memcpy(bytes, instance->c_str(), size);
        env->ReleaseByteArrayElements(array, bytes, 0);
    }
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nInsert
  (JNIEnv* env, jclass jclass, jlong ptr, jint offset, jstring s) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    skija::UtfIndicesConverter conv(*instance);
    instance->insert(conv.from16To8(offset), skString(env, s));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nAppend
  (JNIEnv* env, jclass jclass, jlong ptr, jstring s) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    instance->append(skString(env, s));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nRemoveSuffix
  (JNIEnv* env, jclass jclass, jlong ptr, jint from) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    skija::UtfIndicesConverter conv(*instance);
    size_t from8 = conv.from16To8(from);
    instance->remove(from8, instance->size() - from8);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_ManagedStringKt__1nRemove
  (JNIEnv* env, jclass jclass, jlong ptr, jint from, jint len) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    skija::UtfIndicesConverter conv(*instance);
    size_t from8 = conv.from16To8(from);
    size_t to8 = conv.from16To8(from + len);
    instance->remove(from8, to8 - from8);
}
