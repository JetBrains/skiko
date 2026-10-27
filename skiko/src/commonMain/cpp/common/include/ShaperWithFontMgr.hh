#pragma once

#include "FontMgrDefaultFactory.hh"
#include "SkShaper.h"
#include <memory>
#include <utility>

namespace skiko {

// Owns the native shaper and the default fallback manager used by shapeLine/shapeBlob.
// Like SkShaper, this object is not intended for concurrent shaping calls.
class ShaperWithFontMgr {
public:
    static ShaperWithFontMgr* make(std::unique_ptr<SkShaper> shaper) {
        return shaper ? new ShaperWithFontMgr(std::move(shaper)) : nullptr;
    }

    SkShaper* shaper() const { return fShaper.get(); }

    sk_sp<SkFontMgr> defaultFontMgr() {
#if defined(SK_BUILD_FOR_LINUX) && !defined(SKIKO_WASM)
        // Fontconfig loads the configuration and font set when a manager is created.
        // Cache per shaper, not globally: a new shaper can observe updated system fonts.
        if (!fDefaultFontMgr) {
            fDefaultFontMgr = SkFontMgrSkikoDefault();
        }
        return fDefaultFontMgr;
#else
        return SkFontMgrSkikoDefault();
#endif
    }

private:
    explicit ShaperWithFontMgr(std::unique_ptr<SkShaper> shaper)
        : fShaper(std::move(shaper)) {}

    sk_sp<SkFontMgr> fDefaultFontMgr;
    std::unique_ptr<SkShaper> fShaper;
};

} // namespace skiko
