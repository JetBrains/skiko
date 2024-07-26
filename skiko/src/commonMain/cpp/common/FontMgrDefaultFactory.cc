#include "FontMgrDefaultFactory.hh"


#if defined(SK_BUILD_FOR_MAC) || defined(SK_BUILD_FOR_IOS)
#include "ports/SkFontMgr_mac_ct.h"
#endif

#ifdef SK_BUILD_FOR_WIN
#include "ports/SkTypeface_win.h"
#endif

#if (defined(SK_BUILD_FOR_UNIX) || defined(SK_BUILD_FOR_LINUX)) && !defined(SKIKO_WASM)
#include "ports/SkFontMgr_fontconfig.h"
#endif

#ifdef SK_BUILD_FOR_ANDROID
#include "ports/SkFontMgr_android.h"
#endif

sk_sp<SkFontMgr> SkFontMgrSkikoDefault() {

#if defined(SK_BUILD_FOR_MAC) || defined(SK_BUILD_FOR_IOS)
    return SkFontMgr_New_CoreText(nullptr);
#endif

#ifdef SK_BUILD_FOR_WIN
    return SkFontMgr_New_DirectWrite();
#endif

#if (defined(SK_BUILD_FOR_UNIX) || defined(SK_BUILD_FOR_LINUX)) && !defined(SKIKO_WASM)
    return SkFontMgr_New_FontConfig(nullptr);
#endif

#ifdef SK_BUILD_FOR_ANDROID
    return SkFontMgr_New_Android(nullptr);
#endif
}

struct SkEmbeddedResource { const uint8_t* data; size_t size; };
struct SkEmbeddedResourceHeader { const SkEmbeddedResource* entries; int count; };
extern "C" const SkEmbeddedResourceHeader SK_EMBEDDED_FONTS;