
// This file has been auto generated.

#include "SkRuntimeEffect.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (jlong ptr, jlong uniformPtr, jlongArray childrenPtrsArr, jfloatArray localMatrixArr, jboolean isOpaque) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeShader");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (jlong ptr, jlong uniformPtr, jlongArray childrenPtrsArr, jfloatArray localMatrixArr, jboolean isOpaque) {
    SkRuntimeEffect* runtimeEffect = jlongToPtr<SkRuntimeEffect*>(ptr);
    SkData* uniform = jlongToPtr<SkData*>(uniformPtr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);

    jsize childCount = env->GetArrayLength(childrenPtrsArr);
    jlong* childrenPtrs = env->GetLongArrayElements(childrenPtrsArr, 0);
    std::vector<sk_sp<SkShader>> children(childCount);
    for (size_t i = 0; i < childCount; i++) {
        SkShader* si = jlongToPtr<SkShader*>(childrenPtrs[i]);
        children[i] = sk_ref_sp(si);
    }
    env->ReleaseLongArrayElements(childrenPtrsArr, childrenPtrs, 0);

    sk_sp<SkShader> shader = runtimeEffect->makeShader(sk_ref_sp<SkData>(uniform),
                                                       children.data(),
                                                       childCount,
                                                       localMatrix.get(),
                                                       isOpaque);
    return ptrToJlong(shader.release());
}
#endif



extern "C" jlong org_jetbrains_skia_RuntimeEffect__1nMakeForShader
    (jstring sksl) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeForShader");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_RuntimeEffect__1nMakeForShader
    (jstring sksl) {
    SkString skslProper = skString(env, sksl);
    SkRuntimeEffect::Result result = SkRuntimeEffect::MakeForShader(skslProper);
    if (result.errorText.isEmpty()) {
        sk_sp<SkRuntimeEffect> effect = result.effect;
        return ptrToJlong(effect.release());
    } else {
        env->ThrowNew(java::lang::RuntimeException::cls, result.errorText.c_str());
        return 0;
    }
}
#endif



extern "C" jlong org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter
    (jstring sksl) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter
    (jstring sksl) {
    SkString skslProper = skString(env, sksl);
    SkRuntimeEffect::Result result = SkRuntimeEffect::MakeForColorFilter(skslProper);
    if (result.errorText.isEmpty()) {
        return ptrToJlong(result.effect.release());
    } else {
        env->ThrowNew(java::lang::RuntimeException::cls, result.errorText.c_str());
        return 0;
    }
}
#endif

