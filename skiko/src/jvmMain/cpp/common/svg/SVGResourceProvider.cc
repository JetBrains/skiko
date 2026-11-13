#include <jni.h>
#include "SkFontMgr.h"
#include "modules/skresources/include/SkResources.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_svg_SVGResourceProviderKt_SVGResourceProvider_1nMakeDataURI
  (JNIEnv* env, jclass jclass) {
    // No delegate: names that are not a data:image/ URI are not loaded. That includes file paths
    // and http(s) URLs. DataURIResourceProviderProxy::Make accepts a null provider.
    sk_sp<skresources::ResourceProvider> instance = skresources::DataURIResourceProviderProxy::Make(nullptr);
    return reinterpret_cast<jlong>(instance.release());
}
