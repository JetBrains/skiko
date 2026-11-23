#include <jni.h>
#include "../interop.hh"
#include "SkCanvas.h"
#include "SkData.h"
#include "SkFontMgr.h"
#include "SkStream.h"
#include "SkSVGDOM.h"
#include "SkSVGSVG.h"
#include "modules/skresources/include/SkResources.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    // No font manager and no resource provider: <text> that needs a font, and <image>, are not drawn.
    // Callers that need either use Builder.
    sk_sp<SkSVGDOM> instance = SkSVGDOM::MakeFromStream(stream);
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nGetRoot
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    SkSVGSVG* root = instance->getRoot();
    root->ref();
    return reinterpret_cast<jlong>(root);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nGetContainerSize
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray dst) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    const SkSize& size = instance->containerSize();
    skija::Point::copyToInterop(env, {size.fWidth, size.fHeight}, dst);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nSetContainerSize
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat width, jfloat height) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    instance->setContainerSize(SkSize{width, height});
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nRender
  (JNIEnv* env, jclass jclass, jlong ptr, jlong canvasPtr) {
    SkSVGDOM* instance = reinterpret_cast<SkSVGDOM*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    instance->render(canvas);
}

static void deleteSVGDOMBuilder(SkSVGDOM::Builder* builder) {
    delete builder;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nBuilderGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteSVGDOMBuilder));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nBuilderMake
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(new SkSVGDOM::Builder());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nBuilderSetFontManager
  (JNIEnv* env, jclass jclass, jlong ptr, jlong fontMgrPtr) {
    SkSVGDOM::Builder* instance = reinterpret_cast<SkSVGDOM::Builder*>(static_cast<uintptr_t>(ptr));
    instance->setFontManager(sk_ref_sp(reinterpret_cast<SkFontMgr*>(static_cast<uintptr_t>(fontMgrPtr))));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nBuilderSetResourceProvider
  (JNIEnv* env, jclass jclass, jlong ptr, jlong resourceProviderPtr) {
    SkSVGDOM::Builder* instance = reinterpret_cast<SkSVGDOM::Builder*>(static_cast<uintptr_t>(ptr));
    instance->setResourceProvider(sk_ref_sp(reinterpret_cast<skresources::ResourceProvider*>(static_cast<uintptr_t>(resourceProviderPtr))));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGDOMKt_SVGDOM_1nBuilderBuildFromData
  (JNIEnv* env, jclass jclass, jlong ptr, jlong dataPtr) {
    SkSVGDOM::Builder* instance = reinterpret_cast<SkSVGDOM::Builder*>(static_cast<uintptr_t>(ptr));
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkMemoryStream stream(sk_ref_sp(data));
    return reinterpret_cast<jlong>(instance->make(stream).release());
}
