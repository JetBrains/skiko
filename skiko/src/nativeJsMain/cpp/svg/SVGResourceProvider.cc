#include "SkFontMgr.h"
#include "common.h"
#include "modules/skresources/include/SkResources.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_svg_SVGResourceProvider__1nMakeDataURI
  () {
    // No delegate: names that are not a data:image/ URI are not loaded. That includes file paths
    // and http(s) URLs. DataURIResourceProviderProxy::Make accepts a null provider.
    sk_sp<skresources::ResourceProvider> instance = skresources::DataURIResourceProviderProxy::Make(nullptr);
    return reinterpret_cast<KNativePointer>(instance.release());
}
