
// This file has been auto generated.

#include <iostream>
#include "SkMaskFilter.h"
#include "SkShader.h"
#include "SkShaderMaskFilter.h"
#include "SkTableMaskFilter.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_MaskFilter__1nMakeBlur
  (kref __Kinstance, jint blurStyleInt, jfloat sigma, jboolean respectCTM) {
    SkBlurStyle blurStyle = static_cast<SkBlurStyle>(blurStyleInt);
    SkMaskFilter* ptr = SkMaskFilter::MakeBlur(blurStyle, sigma, respectCTM).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_MaskFilter__1nMakeShader
  (kref __Kinstance, jlong shaderPtr) {
    SkShader* shader = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(shaderPtr));
    SkMaskFilter* ptr = SkShaderMaskFilter::Make(sk_ref_sp(shader)).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_MaskFilter__1nMakeTable
  (kref __Kinstance, jbyteArray tableArray) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_MaskFilter__1nMakeTable");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_MaskFilter__1nMakeTable
  (kref __Kinstance, jbyteArray tableArray) {
    jbyte* table = env->GetByteArrayElements(tableArray, 0);
    SkMaskFilter* ptr = SkTableMaskFilter::Create(reinterpret_cast<uint8_t*>(table));
    env->ReleaseByteArrayElements(tableArray, table, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_MaskFilter__1nMakeGamma
  (kref __Kinstance, jfloat gamma) {
    SkMaskFilter* ptr = SkTableMaskFilter::CreateGamma(gamma);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_MaskFilter__1nMakeClip
  (kref __Kinstance, jbyte min, jbyte max) {
    SkMaskFilter* ptr = SkTableMaskFilter::CreateClip(static_cast<uint8_t>(min), static_cast<uint8_t>(max));
    return reinterpret_cast<jlong>(ptr);
}
