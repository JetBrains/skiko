
// This file has been auto generated.

#include "SkSVGCanvas.h"
#include "SkStream.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGCanvas__1nMake
  (KFloat left, KFloat top, KFloat right, KFloat bottom, KNativePointer wstreamPtr, KInt flags) {
    SkWStream* wstream = reinterpret_cast<SkWStream*>((wstreamPtr));
    SkRect bounds {left, top, right, bottom};
    SkCanvas* instance = SkSVGCanvas::Make(bounds, wstream, flags).release();
    return reinterpret_cast<KNativePointer>(instance);
}
