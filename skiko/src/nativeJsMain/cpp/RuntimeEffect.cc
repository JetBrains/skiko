#include "SkRuntimeEffect.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (KNativePointer ptr, KNativePointer uniformPtr, KNativePointerArray childrenPtrsArr, KInt childCount, KFloat* localMatrixArr) {
    SkRuntimeEffect* runtimeEffect = reinterpret_cast<SkRuntimeEffect*>(ptr);
    SkData* uniform = reinterpret_cast<SkData*>(uniformPtr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);

    KNativePointer* childrenPtrs = reinterpret_cast<KNativePointer*>(childrenPtrsArr);
    std::vector<sk_sp<SkShader>> children(childCount);
    for (size_t i = 0; i < childCount; i++) {
        SkShader* si = reinterpret_cast<SkShader*>(childrenPtrs[i]);
        children[i] = sk_ref_sp(si);
    }

    sk_sp<SkShader> shader = runtimeEffect->makeShader(sk_ref_sp<SkData>(uniform),
                                                       children.data(),
                                                       childCount,
                                                       localMatrix.get());
    return reinterpret_cast<KNativePointer>(shader.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForShader
    (KInteropPointer sksl) {
    SkString skslProper = skString(sksl);
    SkRuntimeEffect::Result* result = new SkRuntimeEffect::Result {
        SkRuntimeEffect::MakeForShader(skslProper)
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter
    (KInteropPointer sksl) {
    SkString skslProper = skString(sksl);
    SkRuntimeEffect::Result* result = new SkRuntimeEffect::Result {
        SkRuntimeEffect::MakeForColorFilter(skslProper)
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeBlender
(KNativePointer ptr, KNativePointer uniformPtr) {
    SkRuntimeEffect* runtimeEffect = reinterpret_cast<SkRuntimeEffect*>(ptr);
    SkData* uniform = reinterpret_cast<SkData*>(uniformPtr);

    sk_sp<SkBlender> blender = runtimeEffect->makeBlender(sk_ref_sp<SkData>(uniform));
    return reinterpret_cast<KNativePointer>(blender.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForBlender
    (KInteropPointer sksl) {
    SkString skslProper = skString(sksl);
    SkRuntimeEffect::Result* result = new SkRuntimeEffect::Result {
            SkRuntimeEffect::MakeForBlender(skslProper)
    };
    return reinterpret_cast<KNativePointer>(result);
}

// Result
SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1Result_nGetPtr
  (KNativePointer ptr) {
    auto result = reinterpret_cast<SkRuntimeEffect::Result*>(ptr);
    return reinterpret_cast<KNativePointer>(result->effect.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1Result_nGetError
  (KNativePointer ptr) {
    auto result = reinterpret_cast<SkRuntimeEffect::Result*>(ptr);
    if (result->errorText.isEmpty()) {
        return static_cast<KNativePointer>(nullptr);
    } else {
        return reinterpret_cast<KNativePointer>(&(result->errorText));
    }
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeEffect__1Result_nDestroy
  (KNativePointer ptr) {
    delete reinterpret_cast<SkRuntimeEffect::Result*>(ptr);
}
