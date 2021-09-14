#include <jni.h>
#include "SkSVGCanvas.h"
#include "SkStream.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGCanvasKt__1nMake
  (JNIEnv* env, jclass jclass, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong wstreamPtr, jint flags) {
    SkWStream* wstream = reinterpret_cast<SkWStream*>(static_cast<uintptr_t>(wstreamPtr));
    SkRect bounds {left, top, right, bottom};
    SkCanvas* instance = SkSVGCanvas::Make(bounds, wstream, flags).release();
    return reinterpret_cast<jlong>(instance);
}
