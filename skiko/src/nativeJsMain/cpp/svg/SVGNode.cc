#include "SkSVGNode.h"
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_svg_SVGNode__1nGetTag
  (KNativePointer ptr) {
    SkSVGNode* instance = reinterpret_cast<SkSVGNode*>((ptr));
    return static_cast<KInt>(instance->tag());
}
