#include <jni.h>
#include "third_party/externals/icu/source/common/unicode/uchar.h"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_icu_UnicodeKt_charDirection
  (JNIEnv* env, jclass jclass, jint codePoint) {
    return u_charDirection(codePoint);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_icu_UnicodeKt_nCodePointHasBinaryProperty
    (JNIEnv* env, jclass jclass, jint codePoint, jint property) {
   return u_hasBinaryProperty(codePoint, (UProperty)property);
}