
// This file has been auto generated.

#include <iostream>
#include "SkMaskFilter.h"
#include "SkShader.h"
#include "SkShaderMaskFilter.h"
#include "SkTableMaskFilter.h"
#include "common.h"

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeBlur
  (KInteropPointer __Kinstance, KInt blurStyleInt, KFloat sigma, KBoolean respectCTM) {
    SkBlurStyle blurStyle = static_cast<SkBlurStyle>(blurStyleInt);
    SkMaskFilter* ptr = SkMaskFilter::MakeBlur(blurStyle, sigma, respectCTM).release();
    return reinterpret_cast<KInteropPointer>(ptr);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeShader
  (KInteropPointer __Kinstance, KInteropPointer shaderPtr) {
    SkShader* shader = reinterpret_cast<SkShader*>((shaderPtr));
    SkMaskFilter* ptr = SkShaderMaskFilter::Make(sk_ref_sp(shader)).release();
    return reinterpret_cast<KInteropPointer>(ptr);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeTable
  (KInteropPointer __Kinstance, jbyteArray tableArray) {
    TODO("implement org_jetbrains_skia_MaskFilter__1nMakeTable");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeTable
  (KInteropPointer __Kinstance, jbyteArray tableArray) {
    jbyte* table = env->GetByteArrayElements(tableArray, 0);
    SkMaskFilter* ptr = SkTableMaskFilter::Create(reinterpret_cast<uint8_t*>(table));
    env->ReleaseByteArrayElements(tableArray, table, 0);
    return reinterpret_cast<KInteropPointer>(ptr);
}
#endif


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeGamma
  (KInteropPointer __Kinstance, KFloat gamma) {
    SkMaskFilter* ptr = SkTableMaskFilter::CreateGamma(gamma);
    return reinterpret_cast<KInteropPointer>(ptr);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_MaskFilter__1nMakeClip
  (KInteropPointer __Kinstance, jbyte min, jbyte max) {
    SkMaskFilter* ptr = SkTableMaskFilter::CreateClip(static_cast<uint8_t>(min), static_cast<uint8_t>(max));
    return reinterpret_cast<KInteropPointer>(ptr);
}
