#include <iostream>
#include <jni.h>
#include "SkData.h"
#include "SkTypeface.h"
#include "interop.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetFontStyle
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return skija::FontStyle::toJava(instance->fontStyle());
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TypefaceKt__1nIsFixedPitch
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->isFixedPitch();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetVariationsCount
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray res) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->getVariationDesignPosition(nullptr, 0);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetVariations
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray res, jint count) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    if (count > 0) {
        std::vector<SkFontArguments::VariationPosition::Coordinate> coords(count);
        instance->getVariationDesignPosition(coords.data(), count);
        for (int i=0; i < count; ++i) {
             jint r[2] = {static_cast<jint>(coords[i].axis), rawBits(coords[i].value)};
             env->SetIntArrayRegion(res, 2 * i, 2, r);
        }
    }
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetVariationAxesCount
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat* axisData) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->getVariationDesignParameters(nullptr, 0);
}


extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetVariationAxes
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray axisData, jint count) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    if (count > 0) {
        std::vector<SkFontParameters::Variation::Axis> params(count);
        instance->getVariationDesignParameters(params.data(), count);
        for (int i = 0; i < count; ++i) {
            jint p[5] = { static_cast<jint>(params[i].tag), rawBits(params[i].min), rawBits(params[i].def), rawBits(params[i].max), params[i].isHidden()};
            env->SetIntArrayRegion(axisData, 5 * i, 5, p);
        }
    }
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt_Typeface_1nGetUniqueId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->uniqueID();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt_Typeface_1nEquals
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    SkTypeface* other = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(otherPtr));
    return SkTypeface::Equal(instance, other);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt_Typeface_1nMakeDefault
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(SkTypeface::MakeDefault().release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nMakeFromName
  (JNIEnv* env, jclass jclass, jstring nameStr, jint styleValue) {
    SkString name = skString(env, nameStr);
    SkFontStyle style = skija::FontStyle::fromJava(styleValue);
    sk_sp<SkTypeface> instance = SkTypeface::MakeFromName(name.c_str(), style);
    SkTypeface* ptr = instance.release();
    return reinterpret_cast<jlong>(ptr);
}
    
extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nMakeFromFile
  (JNIEnv* env, jclass jclass, jstring pathStr, jint index) {
    SkString path = skString(env, pathStr);
    sk_sp<SkTypeface> instance = SkTypeface::MakeFromFile(path.c_str(), index);
    SkTypeface* ptr = instance.release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong dataPtr, jint index) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    sk_sp<SkTypeface> instance = SkTypeface::MakeFromData(sk_ref_sp(data), index);
    SkTypeface* ptr = instance.release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nMakeClone
  (JNIEnv* env, jclass jclass, jlong typefacePtr, jintArray variationsArr, jint variationsCount, jint collectionIndex) {
    SkTypeface* typeface = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(typefacePtr));
    std::vector<SkFontArguments::VariationPosition::Coordinate> coordinates(variationsCount);
    jint* variations = env->GetIntArrayElements(variationsArr, 0);
    for (int i=0; i < variationsCount; i+=2) {
        coordinates[i] = {
            static_cast<SkFourByteTag>(variations[i]),
            fromBits(variations[i+1])
        };
    }
    env->ReleaseIntArrayElements(variationsArr, variations, 0);
    SkFontArguments arg = SkFontArguments()
                            .setCollectionIndex(collectionIndex)
                            .setVariationDesignPosition({coordinates.data(), variationsCount});
    SkTypeface* clone = typeface->makeClone(arg).release();
    return reinterpret_cast<jlong>(clone);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TypefaceKt_Typeface_1nGetUTF32Glyphs
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray uniArr, jint count, jshortArray res) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    std::vector<short> glyphs(count);
    jint* uni = env->GetIntArrayElements(uniArr, nullptr);
    instance->unicharsToGlyphs(reinterpret_cast<SkUnichar*>(uni), count, reinterpret_cast<SkGlyphID*>(glyphs.data()));
    env->ReleaseIntArrayElements(uniArr, uni, 0);
    env->SetShortArrayRegion(res, 0, count, glyphs.data());
}

extern "C" JNIEXPORT jshort JNICALL Java_org_jetbrains_skia_TypefaceKt_Typeface_1nGetUTF32Glyph
  (JNIEnv* env, jclass jclass, jlong ptr, jint uni) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->unicharToGlyph(uni);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetGlyphsCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->countGlyphs();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetTablesCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->countTables();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetTableTagsCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->countTables();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetTableTags
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray res, jint count) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    std::vector<jint> tags(count);
    instance->getTableTags(reinterpret_cast<SkFontTableTag*>(tags.data()));
    env->SetIntArrayRegion(res, 0, count, tags.data());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetTableSize
  (JNIEnv* env, jclass jclass, jlong ptr, jint tag) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->getTableSize(tag);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetTableData
  (JNIEnv* env, jclass jclass, jlong ptr, jint tag) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    SkData* data = instance->copyTableData(tag).release();
    return reinterpret_cast<jlong>(data);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetUnitsPerEm
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return instance->getUnitsPerEm();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetKerningPairAdjustments
  (JNIEnv* env, jclass jclass, jlong ptr, jshortArray glyphsArr, jint count, jintArray res) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    if (count > 0) {
        std::vector<jint> adjustments(count);
        jshort* glyphs = env->GetShortArrayElements(glyphsArr, nullptr);
        bool hasAdjustments = instance->getKerningPairAdjustments(
          reinterpret_cast<SkGlyphID*>(glyphs), count,
          reinterpret_cast<int32_t*>(adjustments.data())
        );
        env->ReleaseShortArrayElements(glyphsArr, glyphs, 0);
        if (hasAdjustments) {
            env->SetIntArrayRegion(res, 0, count, adjustments.data());
        }
        return hasAdjustments;
    }

    return false;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetFamilyNames
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    SkTypeface::LocalizedStrings* iter = instance->createFamilyNameIterator();
    std::vector<SkTypeface::LocalizedString> names;
    SkTypeface::LocalizedString name;
    std::vector<jlong>* res = new std::vector<jlong>();
    while (iter->next(&name)) {
        res->push_back(reinterpret_cast<jlong>(new SkString(name.fString)));
        res->push_back(reinterpret_cast<jlong>(new SkString(name.fLanguage)));
    }

    return reinterpret_cast<jlong>(res);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt__1nGetFamilyName
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    SkString name;
    instance->getFamilyName(&name);
    return reinterpret_cast<jlong>(new SkString(name));
}

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_TypefaceKt_Typeface_1nGetBounds
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkTypeface* instance = reinterpret_cast<SkTypeface*>(static_cast<uintptr_t>(ptr));
    return skija::Rect::fromSkRect(env, instance->getBounds());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_TypefaceKt_StdVectorDecoder_1nGetArraySize
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vect = reinterpret_cast<std::vector<jlong> *>(ptr);
        return static_cast<jint>(vect->size());
    }

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_TypefaceKt_StdVectorDecoder_1nDisposeArray
    (JNIEnv* env, jclass jclass, jlong ptr) {
        std::vector<jlong>* vect = reinterpret_cast<std::vector<jlong> *>(ptr);
        delete vect;
    }

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_TypefaceKt_StdVectorDecoder_1nGetArrayElement
    (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
        std::vector<jlong>* vect = reinterpret_cast<std::vector<jlong> *>(ptr);
        return vect->at(index);
    }

