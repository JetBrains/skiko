#include "FontMgrWithFallbackWrapper.hh"

FontMgrWithFallbackWrapper::FontMgrWithFallbackWrapper(sk_sp<TypefaceFontProviderWithFallback> fallbackTypefaceFontProvider)
    : fallbackFontProvider(std::move(fallbackTypefaceFontProvider)) {
    wrappedFntMgr = SkFontMgr::RefDefault();
}

int FontMgrWithFallbackWrapper::onCountFamilies() const {
     return wrappedFntMgr->countFamilies();
}

void FontMgrWithFallbackWrapper::onGetFamilyName(int index, SkString* familyName) const {
    wrappedFntMgr->getFamilyName(index, familyName);
}

sk_sp<SkFontStyleSet> FontMgrWithFallbackWrapper::onMatchFamily(const char familyName[]) const {
    return wrappedFntMgr->matchFamily(familyName);
}

sk_sp<SkFontStyleSet> FontMgrWithFallbackWrapper::onCreateStyleSet(int ix) const {
    return wrappedFntMgr->createStyleSet(ix);
}

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onMatchFamilyStyle(const char familyName[], const SkFontStyle& style) const {
    return wrappedFntMgr->matchFamilyStyle(familyName, style);
}

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onMatchFamilyStyleCharacter(
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

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onMakeFromData(sk_sp<SkData> data, int ix) const {
    return wrappedFntMgr->makeFromData(std::move(data), ix);
}

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onMakeFromStreamIndex(std::unique_ptr<SkStreamAsset> stream, int ix) const {
    return wrappedFntMgr->makeFromStream(std::move(stream), ix);
}

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onMakeFromStreamArgs(std::unique_ptr<SkStreamAsset> stream, const SkFontArguments& args) const {
    return wrappedFntMgr->makeFromStream(std::move(stream), args);
}

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onMakeFromFile(const char path[], int ix) const {
    return wrappedFntMgr->makeFromFile(path, ix);
}

sk_sp<SkTypeface> FontMgrWithFallbackWrapper::onLegacyMakeTypeface(const char name[], SkFontStyle style) const {
    return wrappedFntMgr->legacyMakeTypeface(name, style);
}

void FontMgrWithFallbackWrapper::setFallbackFontProvider(sk_sp<TypefaceFontProviderWithFallback> fontProvider) {
    fallbackFontProvider = std::move(fontProvider);
}

/* TypefaceFontProviderWithFallback section */

size_t TypefaceFontProviderWithFallback::registerTypeface(sk_sp<SkTypeface> typeface) {
    registeredTypefaces.push_back(typeface);
    return TypefaceFontProvider::registerTypeface(std::move(typeface));
}

size_t TypefaceFontProviderWithFallback::registerTypeface(sk_sp<SkTypeface> typeface, const SkString& alias) {
    registeredTypefaces.push_back(typeface);
    return TypefaceFontProvider::registerTypeface(std::move(typeface), alias);
}

sk_sp<SkTypeface> TypefaceFontProviderWithFallback::fallbackForChar(SkUnichar character) const {
    for (const auto& typeface : registeredTypefaces) {
        if (!typeface) continue;
        auto glyph = typeface->unicharToGlyph(character);
        if (glyph != 0) {
            return typeface;
        }
    }
    return nullptr;
}