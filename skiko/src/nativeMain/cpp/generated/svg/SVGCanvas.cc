
// This file has been auto generated.

#include "SkSVGCanvas.h"
#include "SkStream.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_svg_SVGCanvas__1nMake
  (jfloat left, jfloat top, jfloat right, jfloat bottom, jlong wstreamPtr, jint flags) {
    SkWStream* wstream = reinterpret_cast<SkWStream*>(static_cast<uintptr_t>(wstreamPtr));
    SkRect bounds {left, top, right, bottom};
    SkCanvas* instance = SkSVGCanvas::Make(bounds, wstream, flags).release();
    return reinterpret_cast<jlong>(instance);
}
