#include <jni.h>
#include "SkDocument.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DocumentKt__1nBeginPage
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat width, jfloat height, jfloatArray jcontentArr) {
    SkDocument* instance = reinterpret_cast<SkDocument*>(static_cast<uintptr_t>(ptr));
    jfloat* contentArr;
    SkRect content;
    SkRect* contentPtr = nullptr;
    if (jcontentArr != nullptr) {
        contentArr = env->GetFloatArrayElements(jcontentArr, 0);
        content = { contentArr[0], contentArr[1], contentArr[2], contentArr[3] };
        contentPtr = &content;
    }
    SkCanvas* canvas = instance->beginPage(width, height, contentPtr);
    if (jcontentArr != nullptr)
        env->ReleaseFloatArrayElements(jcontentArr, contentArr, 0);
    return reinterpret_cast<jlong>(canvas);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DocumentKt__1nEndPage
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkDocument* instance = reinterpret_cast<SkDocument*>(static_cast<uintptr_t>(ptr));
    instance->endPage();
}
