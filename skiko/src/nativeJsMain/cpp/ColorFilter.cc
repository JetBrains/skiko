
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkColorMatrixFilter.h"
#include "SkHighContrastFilter.h"
#include "SkLumaColorFilter.h"
#include "SkOverdrawColorFilter.h"
#include "SkTableColorFilter.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeComposed
  (KNativePointer outerPtr, KNativePointer innerPtr) {
    SkColorFilter* outer = reinterpret_cast<SkColorFilter*>((outerPtr));
    SkColorFilter* inner = reinterpret_cast<SkColorFilter*>((innerPtr));
    SkColorFilter* ptr = SkColorFilters::Compose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeBlend
  (KInt colorInt, KInt modeInt) {
    SkColor color = static_cast<SkColor>(colorInt);
    SkBlendMode mode = static_cast<SkBlendMode>(modeInt);
    SkColorFilter* ptr = SkColorFilters::Blend(color, mode).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeMatrix
  (KFloat* rowMajor) {
    SkColorFilter* ptr = SkColorFilters::Matrix(rowMajor).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix
  (KFloat* rowMajor) {
    SkColorFilter* ptr = SkColorFilters::HSLAMatrix(rowMajor).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma
  () {
    SkColorFilter* ptr = SkColorFilters::LinearToSRGBGamma().release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma
  () {
    SkColorFilter* ptr = SkColorFilters::SRGBToLinearGamma().release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeLerp
  (KFloat t, KNativePointer dstPtr, KNativePointer srcPtr) {
    SkColorFilter* dst = reinterpret_cast<SkColorFilter*>((dstPtr));
    SkColorFilter* src = reinterpret_cast<SkColorFilter*>((srcPtr));
    SkColorFilter* ptr = SkColorFilters::Lerp(t, sk_ref_sp(dst), sk_ref_sp(src)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeLighting
  (KInt mul, KInt add) {
    SkColorFilter* ptr = SkColorMatrixFilter::MakeLightingFilter(mul, add).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeHighContrast
  (KBoolean grayscale, KInt inverionModeInt, KFloat contrast) {
    SkHighContrastConfig config(grayscale, static_cast<SkHighContrastConfig::InvertStyle>(inverionModeInt), contrast);
    SkColorFilter* ptr = SkHighContrastFilter::Make(config).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeTable
  (KByte* table) {
    SkColorFilter* ptr = SkTableColorFilter::Make(reinterpret_cast<uint8_t*>(table)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

typedef void* KInteropPointer;


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeTableARGB
  (KByte* a, KByte* r, KByte* g, KByte* b) {
    SkColorFilter* ptr = SkTableColorFilter::MakeARGB(
        reinterpret_cast<uint8_t*>(a),
        reinterpret_cast<uint8_t*>(r),
        reinterpret_cast<uint8_t*>(g),
        reinterpret_cast<uint8_t*>(b)
    ).release();
    
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nMakeOverdraw
  (KInt c0, KInt c1, KInt c2, KInt c3, KInt c4, KInt c5) {
    SkColor colors[6];
    colors[0] = c0;
    colors[1] = c1;
    colors[2] = c2;
    colors[3] = c3;
    colors[4] = c4;
    colors[5] = c5;
    SkColorFilter* ptr = SkOverdrawColorFilter::MakeWithSkColors(colors).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ColorFilter__1nGetLuma
  () {
    SkColorFilter* ptr = SkLumaColorFilter::Make().release();
    return reinterpret_cast<KNativePointer>(ptr);
}
