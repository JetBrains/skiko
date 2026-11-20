#include <jni.h>
#include "SkikoUnicode.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_icu_UnicodeKt__1nCharDirection
  (JNIEnv* env, jclass jclass, jint codePoint) {
    return skiko::charDirection(codePoint);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_icu_UnicodeKt__1nCodePointHasBinaryProperty
    (JNIEnv* env, jclass jclass, jint codePoint, jint property) {
    return skiko::codePointHasBinaryProperty(codePoint, property);
}