
// This file has been auto generated.

#include "SkSVGNode.h"
#include "common.h"

extern "C" jint org_jetbrains_skia_svg_SVGNode__1nGetTag
  (kref __Kinstance, jlong ptr) {
    SkSVGNode* instance = reinterpret_cast<SkSVGNode*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->tag());
}
