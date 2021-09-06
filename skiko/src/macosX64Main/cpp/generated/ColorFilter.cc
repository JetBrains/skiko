
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkColorMatrixFilter.h"
#include "SkHighContrastFilter.h"
#include "SkLumaColorFilter.h"
#include "SkOverdrawColorFilter.h"
#include "SkTableColorFilter.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeComposed
  (kref __Kinstance, jlong outerPtr, jlong innerPtr) {
    SkColorFilter* outer = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(outerPtr));
    SkColorFilter* inner = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(innerPtr));
    SkColorFilter* ptr = SkColorFilters::Compose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeBlend
  (kref __Kinstance, jint colorInt, jint modeInt) {
    SkColor color = static_cast<SkColor>(colorInt);
    SkBlendMode mode = static_cast<SkBlendMode>(modeInt);
    SkColorFilter* ptr = SkColorFilters::Blend(color, mode).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeMatrix
  (kref __Kinstance, jfloatArray rowMajorArray) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_ColorFilter__1nMakeMatrix");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeMatrix
  (kref __Kinstance, jfloatArray rowMajorArray) {
    jfloat* rowMajor = env->GetFloatArrayElements(rowMajorArray, 0);
    SkColorFilter* ptr = SkColorFilters::Matrix(rowMajor).release();
    env->ReleaseFloatArrayElements(rowMajorArray, rowMajor, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix
  (kref __Kinstance, jfloatArray rowMajorArray) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix
  (kref __Kinstance, jfloatArray rowMajorArray) {
    jfloat* rowMajor = env->GetFloatArrayElements(rowMajorArray, 0);
    SkColorFilter* ptr = SkColorFilters::HSLAMatrix(rowMajor).release();
    env->ReleaseFloatArrayElements(rowMajorArray, rowMajor, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma
  (kref __Kinstance) {
    SkColorFilter* ptr = SkColorFilters::LinearToSRGBGamma().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma
  (kref __Kinstance) {
    SkColorFilter* ptr = SkColorFilters::SRGBToLinearGamma().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeLerp
  (kref __Kinstance, jfloat t, jlong dstPtr, jlong srcPtr) {
    SkColorFilter* dst = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(dstPtr));
    SkColorFilter* src = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(srcPtr));
    SkColorFilter* ptr = SkColorFilters::Lerp(t, sk_ref_sp(dst), sk_ref_sp(src)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeLighting
  (kref __Kinstance, jint mul, jint add) {
    SkColorFilter* ptr = SkColorMatrixFilter::MakeLightingFilter(mul, add).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeHighContrast
  (kref __Kinstance, jboolean grayscale, jint inverionModeInt, jfloat contrast) {
    SkHighContrastConfig config(grayscale, static_cast<SkHighContrastConfig::InvertStyle>(inverionModeInt), contrast);
    SkColorFilter* ptr = SkHighContrastFilter::Make(config).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeTable
  (kref __Kinstance, jbyteArray tableArray) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_ColorFilter__1nMakeTable");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeTable
  (kref __Kinstance, jbyteArray tableArray) {
    jbyte* table = env->GetByteArrayElements(tableArray, 0);
    SkColorFilter* ptr = SkTableColorFilter::Make(reinterpret_cast<uint8_t*>(table)).release();
    env->ReleaseByteArrayElements(tableArray, table, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeTableARGB
  (kref __Kinstance, jbyteArray arrayA, jbyteArray arrayR, jbyteArray arrayG, jbyteArray arrayB) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_ColorFilter__1nMakeTableARGB");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeTableARGB
  (kref __Kinstance, jbyteArray arrayA, jbyteArray arrayR, jbyteArray arrayG, jbyteArray arrayB) {
    jbyte* a = arrayA ? env->GetByteArrayElements(arrayA, 0) : nullptr;
    jbyte* r = arrayR ? env->GetByteArrayElements(arrayR, 0) : nullptr;
    jbyte* g = arrayG ? env->GetByteArrayElements(arrayG, 0) : nullptr;
    jbyte* b = arrayB ? env->GetByteArrayElements(arrayB, 0) : nullptr;

    SkColorFilter* ptr = SkTableColorFilter::MakeARGB(reinterpret_cast<uint8_t*>(a), reinterpret_cast<uint8_t*>(r), reinterpret_cast<uint8_t*>(g), reinterpret_cast<uint8_t*>(b)).release();
    
    if (arrayA) env->ReleaseByteArrayElements(arrayA, a, 0);
    if (arrayR) env->ReleaseByteArrayElements(arrayR, r, 0);
    if (arrayG) env->ReleaseByteArrayElements(arrayG, g, 0);
    if (arrayB) env->ReleaseByteArrayElements(arrayB, b, 0);
    
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_ColorFilter__1nMakeOverdraw
  (kref __Kinstance, jint c0, jint c1, jint c2, jint c3, jint c4, jint c5) {
    SkColor colors[6];
    colors[0] = c0;
    colors[1] = c1;
    colors[2] = c2;
    colors[3] = c3;
    colors[4] = c4;
    colors[5] = c5;
    SkColorFilter* ptr = SkOverdrawColorFilter::MakeWithSkColors(colors).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ColorFilter__1nGetLuma
  (kref __Kinstance) {
    SkColorFilter* ptr = SkLumaColorFilter::Make().release();
    return reinterpret_cast<jlong>(ptr);
}
