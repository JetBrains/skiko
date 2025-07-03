#include <jni.h>
#include "../interop.hh"
#include "interop.hh"
#include "SkSVGSVG.h"
#include "SkSVGRenderContext.h"

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetX
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray jresult) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::copyToInterop(env, instance->getX(), jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetY
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray jresult) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::copyToInterop(env, instance->getY(), jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetWidth
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray jresult) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::copyToInterop(env, instance->getWidth(), jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetHeight
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray jresult) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGLength::copyToInterop(env, instance->getHeight(), jresult);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetPreserveAspectRatio
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray jresult) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    return skija::svg::SVGPreserveAspectRatio::copyToInterop(env, instance->getPreserveAspectRatio(), jresult);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetViewBox
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkTLazy<SkSVGViewBoxType> viewBox = instance->getViewBox();
    if (viewBox.isValid()) {
        skija::Rect::copyToInterop(env, *viewBox.get(), result);
        return true;
    } else {
        return false;
    }
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nGetIntrinsicSize
  (JNIEnv* env, jclass jclass, jlong ptr, float width, float height, float dpi, jfloatArray result) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLengthContext lc({width, height}, dpi);
    SkSize size = instance->intrinsicSize(lc);
    skija::Point::copyToInterop(env, {size.width(), size.height()}, result);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nSetX
  (JNIEnv* env, jclass jclass, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setX(lenght);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nSetY
  (JNIEnv* env, jclass jclass, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setY(lenght);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nSetWidth
  (JNIEnv* env, jclass jclass, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setWidth(lenght);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nSetHeight
  (JNIEnv* env, jclass jclass, jlong ptr, float value, int unit) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    SkSVGLength lenght(value, static_cast<SkSVGLength::Unit>(unit));
    instance->setHeight(lenght);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nSetPreserveAspectRatio
  (JNIEnv* env, jclass jclass, jlong ptr, jint align, jint scale) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    instance->setPreserveAspectRatio(SkSVGPreserveAspectRatio { static_cast<SkSVGPreserveAspectRatio::Align>(align),
                                                                static_cast<SkSVGPreserveAspectRatio::Scale>(scale) });
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGSVGExternalKt_SVGSVG_1nSetViewBox
  (JNIEnv* env, jclass jclass, jlong ptr, float l, float t, float r, float b) {
    SkSVGSVG* instance = reinterpret_cast<SkSVGSVG*>(static_cast<uintptr_t>(ptr));
    instance->setViewBox(SkRect::MakeLTRB(l, t, r, b));
}
