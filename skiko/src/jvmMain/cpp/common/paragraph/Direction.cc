#include <jni.h>
#include "third_party/externals/icu/source/common/unicode/uchar.h"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_paragraph_DirectionKt_unicodeCharDirection
  (JNIEnv* env, jclass jclass, jint codePoint) {
    return u_charDirection(codePoint);
}
