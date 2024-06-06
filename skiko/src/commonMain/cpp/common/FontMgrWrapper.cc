#include "FontMgrWrapper.hh"
#include <stdexcept>

FontMgrWrapper::FontMgrWrapper(sk_sp<ExtendedTypefaceFontProvider> fallbackTypefaceFontProvider)
    : fallbackFontProvider(std::move(fallbackTypefaceFontProvider)) {
    wrappedFntMgr = SkFontMgr::RefDefault();
}

int FontMgrWrapper::onCountFamilies() const {
    SkDebugf("onCountFamilies\n");
     return wrappedFntMgr->countFamilies();
}

void FontMgrWrapper::onGetFamilyName(int index, SkString* familyName) const {
    SkDebugf("onGetFamilyName\n");
    wrappedFntMgr->getFamilyName(index, familyName);
}

sk_sp<SkFontStyleSet> FontMgrWrapper::onMatchFamily(const char familyName[]) const {
    SkDebugf("onMatchFamily\n");
    return wrappedFntMgr->matchFamily(familyName);
}

sk_sp<SkFontStyleSet> FontMgrWrapper::onCreateStyleSet(int ix) const {
//    return wrappedFntMgr->onCreateStyleSet(ix);
    return nullptr;
}

sk_sp<SkTypeface> FontMgrWrapper::onMatchFamilyStyle(const char familyName[], const SkFontStyle& style) const {
    return wrappedFntMgr->matchFamilyStyle(familyName, style);
}

sk_sp<SkTypeface> ExtendedTypefaceFontProvider::fallbackForChar(SkUnichar character) const {
    SkDebugf("fallbackForChar\n");
    for (const auto& typeface : registeredTypefaces) {
        if (!typeface) continue;
        auto glyph = typeface->unicharToGlyph(character);
        if (glyph != 0) {
            return typeface;
        }
    }
    return nullptr;
}

sk_sp<SkTypeface> FontMgrWrapper::onMatchFamilyStyleCharacter(
    const char fontFamily[], const SkFontStyle& style,
    const char* bcp47[], int bcp47Count,
    SkUnichar character
) const {
    auto typeface = wrappedFntMgr->matchFamilyStyleCharacter(fontFamily, style, bcp47, bcp47Count, character);
    if (!typeface) {
        typeface = fallbackFontProvider->fallbackForChar(character);
    }
    return typeface;
}

sk_sp<SkTypeface> FontMgrWrapper::onMakeFromData(sk_sp<SkData> data, int ix) const {
    return wrappedFntMgr->makeFromData(data, ix);
}

sk_sp<SkTypeface> FontMgrWrapper::onMakeFromFile(const char path[], int ix) const {
    return wrappedFntMgr->makeFromFile(path, ix);
}

sk_sp<SkTypeface> FontMgrWrapper::onLegacyMakeTypeface(const char name[], SkFontStyle style) const {
    return wrappedFntMgr->legacyMakeTypeface(name,style);
}

void FontMgrWrapper::setFallbackFontProvider(sk_sp<ExtendedTypefaceFontProvider> fontProvider) {
    fallbackFontProvider = std::move(fontProvider);
}

/* ExtendedTypefaceFontProvider section */

size_t ExtendedTypefaceFontProvider::registerTypeface(sk_sp<SkTypeface> typeface) {
    SkDebugf("--registerTypeface\n");
    registeredTypefaces.push_back(typeface);
    return TypefaceFontProvider::registerTypeface(std::move(typeface));
}

size_t ExtendedTypefaceFontProvider::registerTypeface(sk_sp<SkTypeface> typeface, const SkString& alias) {
    SkDebugf("--registerTypeface with alias\n");
    registeredTypefaces.push_back(typeface);
    return TypefaceFontProvider::registerTypeface(std::move(typeface), alias);
}