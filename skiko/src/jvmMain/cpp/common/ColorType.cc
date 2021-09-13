#include <jni.h>
#include "SkImageInfo.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ColorTypeKt__1nIsAlwaysOpaque
  (JNIEnv* env, jclass jclass, jint value) {
    return SkColorTypeIsAlwaysOpaque(static_cast<SkColorType>(value));
}
