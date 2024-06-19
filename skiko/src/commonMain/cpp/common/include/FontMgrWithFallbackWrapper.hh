#ifndef FONT_MGR_WRAPPER_H
#define FONT_MGR_WRAPPER_H

#include "SkData.h"
#include "SkStream.h"
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "TypefaceFontProvider.h"

using namespace skia::textlayout;

// extends the default TypefaceFontProvider:
// - all registered Typefaces are collected in registeredTypefaces
// - then they are used to find a fallback Typeface in `fallbackForChar`
class TypefaceFontProviderWithFallback : public TypefaceFontProvider {
public:
    size_t registerTypeface(sk_sp<SkTypeface> typeface);
    size_t registerTypeface(sk_sp<SkTypeface> typeface, const SkString& alias);

    sk_sp<SkTypeface> fallbackForChar(SkUnichar character) const;

private:
    std::vector<sk_sp<SkTypeface>> registeredTypefaces;
};

// FontMgrWithFallbackWrapper implementation in most methods simply delegates to wrappedFntMgr.
// The only modified implementation is onMatchFamilyStyleCharacter, which attempts to find
// any Typeface (fallback) for a given character, if all other default fallbacks didn't find anything.
// To achieve this it relies on fallbackFontProvider set via `setFallbackFontProvider`.
class FontMgrWithFallbackWrapper : public SkFontMgr {
public:
    explicit FontMgrWithFallbackWrapper(sk_sp<TypefaceFontProviderWithFallback>);
    int onCountFamilies() const override;
    void onGetFamilyName(int index, SkString* familyName) const override;
    sk_sp<SkFontStyleSet> onMatchFamily(const char familyName[]) const override;
    sk_sp<SkFontStyleSet> onCreateStyleSet(int) const override;
    sk_sp<SkTypeface> onMatchFamilyStyle(const char[], const SkFontStyle&) const override;
    sk_sp<SkTypeface> onMatchFamilyStyleCharacter(const char[], const SkFontStyle&,
                                                  const char*[], int,
                                                  SkUnichar) const override;
    sk_sp<SkTypeface> onMakeFromData(sk_sp<SkData>, int) const override;
    sk_sp<SkTypeface> onMakeFromStreamIndex(std::unique_ptr<SkStreamAsset>, int) const override;
    sk_sp<SkTypeface> onMakeFromStreamArgs(std::unique_ptr<SkStreamAsset>,
                                           const SkFontArguments&) const override;
    sk_sp<SkTypeface> onMakeFromFile(const char[], int) const override;

    sk_sp<SkTypeface> onLegacyMakeTypeface(const char[], SkFontStyle) const override;

    void setFallbackFontProvider(sk_sp<TypefaceFontProviderWithFallback>);

private:
    sk_sp<SkFontMgr> wrappedFntMgr;
    sk_sp<TypefaceFontProviderWithFallback> fallbackFontProvider;
};


#endif