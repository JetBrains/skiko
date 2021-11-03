
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkTypeface.h"
#include "common.h"


SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetFontStyle
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return skija::FontStyle::toKotlin(instance->fontStyle());
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Typeface__1nIsFixedPitch
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->isFixedPitch();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetVariationsCount
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    return instance->getVariationDesignPosition(nullptr, 0);
}

SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Typeface__1nGetVariations
  (KNativePointer ptr, KInt* res, KInt count) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    if (count > 0) {
        std::vector<SkFontArguments::VariationPosition::Coordinate> coords(count);
        instance->getVariationDesignPosition(coords.data(), count);
        for (int i=0; i < count; ++i) {
             int r[2] = {static_cast<int>(coords[i].axis), rawBits(coords[i].value)};
             memcpy(res + 2 * i, r, sizeof r);
        }
    }
}


SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetVariationAxesCount
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    return instance->getVariationDesignParameters(nullptr, 0);
}


SKIKO_EXPORT void org_jetbrains_skia_Typeface__1nGetVariationAxes
  (KNativePointer ptr, int* axis, int count) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    if (count > 0) {
        std::vector<SkFontParameters::Variation::Axis> params(count);
        instance->getVariationDesignParameters(params.data(), count);
        for (int i = 0,  j = 0; i < count; ++i) {
            int p[5] = { static_cast<int>(params[i].tag), rawBits(params[i].min), rawBits(params[i].def), rawBits(params[i].max), params[i].isHidden()};
            memcpy(axis + 5 * i, p, sizeof p);

        }
    }
}
     
SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetUniqueId
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->uniqueID();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nEquals
  (KNativePointer ptr, KNativePointer otherPtr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    SkTypeface* other = reinterpret_cast<SkTypeface*>((otherPtr));
    return SkTypeface::Equal(instance, other);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeDefault
  (){
    return reinterpret_cast<KNativePointer>(SkTypeface::MakeDefault().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeFromName
  (KInteropPointer nameStr, KInt styleValue) {
    TODO("implement org_jetbrains_skia_Typeface__1nMakeFromName");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeFromName
  (KInteropPointer nameStr, KInt styleValue) {
    SkString name = skString(env, nameStr);
    SkFontStyle style = skija::FontStyle::fromJava(styleValue);
    sk_sp<SkTypeface> instance = SkTypeface::MakeFromName(name.c_str(), style);
    SkTypeface* ptr = instance.release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeFromFile
  (KInteropPointer pathStr, KInt index) {
    SkString path = skString(pathStr);
    sk_sp<SkTypeface> instance = SkTypeface::MakeFromFile(path.c_str(), index);
    SkTypeface* ptr = instance.release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeFromData
  (KNativePointer dataPtr, KInt index) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    sk_sp<SkTypeface> instance = SkTypeface::MakeFromData(sk_ref_sp(data), index);
    SkTypeface* ptr = instance.release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeClone
  (KNativePointer typefacePtr, KInteropPointerArray variations, KInt collectionIndex) {
    TODO("implement org_jetbrains_skia_Typeface__1nMakeClone");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nMakeClone
  (KNativePointer typefacePtr, KInteropPointerArray variations, KInt collectionIndex) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>((typefacePtr));
    int variationCount = env->GetArrayLength(variations);
    std::vector<SkFontArguments::VariationPosition::Coordinate> coordinates(variationCount);
    for (int i=0; i < variationCount; ++i) {
        KInteropPointer jvar = env->GetObjectArrayElement(variations, i);
        coordinates[i] = {
            static_cast<SkFourByteTag>(env->GetIntField(jvar, skija::FontVariation::tag)),
            env->GetFloatField(jvar, skija::FontVariation::value)
        };
        env->DeleteLocalRef(jvar);
    }
    SkFontArguments arg = SkFontArguments()
                            .setCollectionIndex(collectionIndex)
                            .setVariationDesignPosition({coordinates.data(), variationCount});
    SkTypeface* clone = typeface->makeClone(arg).release();
    return reinterpret_cast<KNativePointer>(clone);
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_Typeface__1nGetUTF32Glyphs
  (KNativePointer ptr, KInt* uni, KInt count, KShort* res) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    instance->unicharsToGlyphs(reinterpret_cast<SkUnichar*>(uni), count, reinterpret_cast<SkGlyphID*>(res));
}
     
SKIKO_EXPORT KShort org_jetbrains_skia_Typeface__1nGetUTF32Glyph
  (KNativePointer ptr, KInt uni) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->unicharToGlyph(uni);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetGlyphsCount
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->countGlyphs();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetTablesCount
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->countTables();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetTableTagsCount
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    return instance->countTables();
}

SKIKO_EXPORT void org_jetbrains_skia_Typeface__1nGetTableTags
  (KNativePointer ptr, KInt* res, KInt count) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    std::vector<int> tags(count);
    instance->getTableTags(reinterpret_cast<SkFontTableTag*>(tags.data()));
    memcpy(res, tags.data(), tags.size() * sizeof(KInt));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetTableSize
  (KNativePointer ptr, KInt tag) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->getTableSize(tag);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Typeface__1nGetTableData
  (KNativePointer ptr, KInt tag) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    SkData* data = instance->copyTableData(tag).release();
    return reinterpret_cast<KNativePointer>(data);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Typeface__1nGetUnitsPerEm
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return instance->getUnitsPerEm();
}


SKIKO_EXPORT KInt* org_jetbrains_skia_Typeface__1nGetKerningPairAdjustments
  (KNativePointer ptr, KShort* glyphsArr) {
    TODO("implement org_jetbrains_skia_Typeface__1nGetKerningPairAdjustments");
}
     
#if 0 
SKIKO_EXPORT KInt* org_jetbrains_skia_Typeface__1nGetKerningPairAdjustments
  (KNativePointer ptr, KShort* glyphsArr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    int count = glyphsArr == nullptr ? 0 : env->GetArrayLength(glyphsArr);
    if (count > 0) {
        std::vector<KInt> adjustments(count);
        KShort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
        bool res = instance->getKerningPairAdjustments(
          reinterpret_cast<SkGlyphID*>(glyphs), count,
            reinterpret_cast<int32_t*>(adjustments.data()));
        env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
        return res ? javaIntArray(env, adjustments) : nullptr;
    } else {
        bool res = instance->getKerningPairAdjustments(nullptr, 0, nullptr);
        return res ? javaIntArray(env, std::vector<KInt>(0)) : nullptr;
    }
}
#endif



SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Typeface__1nGetFamilyNames
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Typeface__1nGetFamilyNames");
}
     
#if 0 
SKIKO_EXPORT KInteropPointerArray org_jetbrains_skia_Typeface__1nGetFamilyNames
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    SkTypeface::LocalizedStrings* iter = instance->createFamilyNameIterator();
    std::vector<SkTypeface::LocalizedString> names;
    SkTypeface::LocalizedString name;
    while (iter->next(&name)) {
        names.push_back(name);
    }
    iter->unref();
    KInteropPointerArray res = env->NewObjectArray((jsize) names.size(), skija::FontFamilyName::cls, nullptr);
    for (int i = 0; i < names.size(); ++i) {
        skija::AutoLocal<KInteropPointer> nameStr(env, javaString(env, names[i].fString));
        skija::AutoLocal<KInteropPointer> langStr(env, javaString(env, names[i].fLanguage));
        skija::AutoLocal<KInteropPointer> obj(env, env->NewObject(skija::FontFamilyName::cls, skija::FontFamilyName::ctor, nameStr.get(), langStr.get()));
        env->SetObjectArrayElement(res, i, obj.get());
    }
    return res;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Typeface__1nGetFamilyName
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(ptr);
    SkString name;
    instance->getFamilyName(&name);
    return new SkString(name);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Typeface__1nGetBounds
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Typeface__1nGetBounds");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Typeface__1nGetBounds
  (KNativePointer ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>((ptr));
    return skija::Rect::fromSkRect(env, instance->getBounds());
}
#endif

