#ifndef FONT_MGR_WRAPPER_H
#define FONT_MGR_WRAPPER_H

// #include <iostream>
#include "SkData.h"
#include "SkStream.h"
#include "SkTypeface.h"
#include "SkFontMgr.h"
#include "TypefaceFontProvider.h"
#include "common.h"

using namespace skia::textlayout;

class ExtendedTypefaceFontProvider : public TypefaceFontProvider {
public:
    size_t registerTypeface(sk_sp<SkTypeface> typeface);
    size_t registerTypeface(sk_sp<SkTypeface> typeface, const SkString& alias);

    sk_sp<SkTypeface> fallbackForChar(SkUnichar character) const;

private:
    std::vector<sk_sp<SkTypeface>> registeredTypefaces;
};

class FontMgrWrapper : public SkFontMgr {

public:
    explicit FontMgrWrapper(sk_sp<ExtendedTypefaceFontProvider>);
    int onCountFamilies() const override;

    void onGetFamilyName(int index, SkString* familyName) const override;

    sk_sp<SkFontStyleSet> onMatchFamily(const char familyName[]) const override;

    sk_sp<SkFontStyleSet> onCreateStyleSet(int) const override;
    sk_sp<SkTypeface> onMatchFamilyStyle(const char[], const SkFontStyle&) const override;
    sk_sp<SkTypeface> onMatchFamilyStyleCharacter(const char[], const SkFontStyle&,
                                                  const char*[], int,
                                                  SkUnichar) const override;

    sk_sp<SkTypeface> onMakeFromData(sk_sp<SkData>, int) const override;
    sk_sp<SkTypeface> onMakeFromStreamIndex(std::unique_ptr<SkStreamAsset>, int) const override {
        return nullptr;
    }
    sk_sp<SkTypeface> onMakeFromStreamArgs(std::unique_ptr<SkStreamAsset>,
                                           const SkFontArguments&) const override {
       return nullptr;
   }
    sk_sp<SkTypeface> onMakeFromFile(const char[], int) const override;

    sk_sp<SkTypeface> onLegacyMakeTypeface(const char[], SkFontStyle) const override;

    void setFallbackFontProvider(sk_sp<ExtendedTypefaceFontProvider>);

private:
    sk_sp<SkFontMgr> wrappedFntMgr;
    sk_sp<ExtendedTypefaceFontProvider> fallbackFontProvider;
};


#endif