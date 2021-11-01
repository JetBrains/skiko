
// This file has been auto generated.

#include "SkRuntimeEffect.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (KNativePointer ptr, KNativePointer uniformPtr, KNativePointer childrenPtrsArr, KFloat* localMatrixArr, KBoolean isOpaque) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeShader");
}

#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (KNativePointer ptr, KNativePointer uniformPtr, KNativePointerArray childrenPtrsArr, KFloat* localMatrixArr, KBoolean isOpaque) {
    SkRuntimeEffect* runtimeEffect = KNativePointerToPtr<SkRuntimeEffect*>(ptr);
    SkData* uniform = KNativePointerToPtr<SkData*>(uniformPtr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);

    jsize childCount = env->GetArrayLength(childrenPtrsArr);
    KNativePointer* childrenPtrs = env->GetLongArrayElements(childrenPtrsArr, 0);
    std::vector<sk_sp<SkShader>> children(childCount);
    for (size_t i = 0; i < childCount; i++) {
        SkShader* si = KNativePointerToPtr<SkShader*>(childrenPtrs[i]);
        children[i] = sk_ref_sp(si);
    }
    env->ReleaseLongArrayElements(childrenPtrsArr, childrenPtrs, 0);

    sk_sp<SkShader> shader = runtimeEffect->makeShader(sk_ref_sp<SkData>(uniform),
                                                       children.data(),
                                                       childCount,
                                                       localMatrix.get(),
                                                       isOpaque);
    return reinterpret_cast<KNativePointer>(shader.release());
}
#endif

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForShader
    (KInteropPointer sksl) {
    SkString skslProper = skString(sksl);
    SkRuntimeEffect::Result* result = new SkRuntimeEffect::Result {
        std::move(SkRuntimeEffect::MakeForShader(skslProper))
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter
    (KInteropPointer sksl) {
    SkString skslProper = skString(sksl);
    SkRuntimeEffect::Result* result = new SkRuntimeEffect::Result {
        std::move(SkRuntimeEffect::MakeForColorFilter(skslProper))
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1Result_nDestroy
  (KNativePointer ptr) {
    delete reinterpret_cast<SkRuntimeEffect::Result*>(ptr);
}
