#include <jni.h>
#include "../interop.hh"
#include "SkCanvas.h"
#include "SkData.h"
#include "SkStream.h"
#include "SkSVGDOM.h"
#include "SkSVGSVG.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMExternalKt_SVGDOM_1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    sk_sp<SkSVGDOM> instance = SkSVGDOM::MakeFromStream(stream);
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMExternalKt_SVGDOM_1nGetRoot
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    SkSVGSVG* root = instance->getRoot();
    root->ref();
    return reinterpret_cast<jlong>(root);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMExternalKt_SVGDOM_1nGetContainerSize
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray dst) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    const SkSize& size = instance->containerSize();
    skija::Point::copyToInterop(env, {size.fWidth, size.fHeight}, dst);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMExternalKt_SVGDOM_1nSetContainerSize
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat width, jfloat height) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    instance->setContainerSize(SkSize{width, height});
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMExternalKt_SVGDOM_1nRender
  (JNIEnv* env, jclass jclass, jlong ptr, jlong canvasPtr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    instance->render(canvas);
}
