
// This file has been auto generated.

#include <iostream>
#include "SkMaskFilter.h"
#include "SkShader.h"
#include "SkShaderMaskFilter.h"
#include "SkTableMaskFilter.h"
#include "common.h"

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeBlur
  (KInt blurStyleInt, KFloat sigma, KBoolean respectCTM) {
    SkBlurStyle blurStyle = static_cast<SkBlurStyle>(blurStyleInt);
    SkMaskFilter* ptr = SkMaskFilter::MakeBlur(blurStyle, sigma, respectCTM).release();
    return reinterpret_cast<KInteropPointer>(ptr);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeShader
  (KInteropPointer shaderPtr) {
    SkShader* shader = reinterpret_cast<SkShader*>((shaderPtr));
    SkMaskFilter* ptr = SkShaderMaskFilter::Make(sk_ref_sp(shader)).release();
    return reinterpret_cast<KInteropPointer>(ptr);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeTable
  (KByte* table) {
    SkMaskFilter* ptr = SkTableMaskFilter::Create(reinterpret_cast<uint8_t*>(table));
    return reinterpret_cast<KInteropPointer>(ptr);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeGamma
  (KFloat gamma) {
    SkMaskFilter* ptr = SkTableMaskFilter::CreateGamma(gamma);
    return reinterpret_cast<KInteropPointer>(ptr);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeClip
  (KByte min, KByte max) {
    SkMaskFilter* ptr = SkTableMaskFilter::CreateClip(static_cast<uint8_t>(min), static_cast<uint8_t>(max));
    return reinterpret_cast<KInteropPointer>(ptr);
}
