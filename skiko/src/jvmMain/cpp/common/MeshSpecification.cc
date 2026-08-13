#include <jni.h>
#include <vector>

#include "SkColorSpace.h"
#include "SkMesh.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshSpecificationKt__1nMake(
    JNIEnv* env,
    jclass,
    jintArray attributeTypesArray,
    jintArray attributeOffsetsArray,
    jobjectArray attributeNamesArray,
    jint vertexStride,
    jintArray varyingTypesArray,
    jobjectArray varyingNamesArray,
    jint attributeCount,
    jint varyingCount,
    jstring vertexShader,
    jstring fragmentShader,
    jlong colorSpacePtr,
    jint alphaType
) {
    jint* attributeTypes = env->GetIntArrayElements(attributeTypesArray, nullptr);
    jint* attributeOffsets = env->GetIntArrayElements(attributeOffsetsArray, nullptr);
    std::vector<SkString> attributeNames = skStringVector(env, attributeNamesArray);
    std::vector<SkMeshSpecification::Attribute> attributes(attributeCount);
    for (int i = 0; i < attributeCount; ++i) {
        attributes[i] = {
            static_cast<SkMeshSpecification::Attribute::Type>(attributeTypes[i]),
            static_cast<size_t>(attributeOffsets[i]),
            attributeNames[i]
        };
    }
    env->ReleaseIntArrayElements(attributeTypesArray, attributeTypes, JNI_ABORT);
    env->ReleaseIntArrayElements(attributeOffsetsArray, attributeOffsets, JNI_ABORT);

    jint* varyingTypes = env->GetIntArrayElements(varyingTypesArray, nullptr);
    std::vector<SkString> varyingNames = skStringVector(env, varyingNamesArray);
    std::vector<SkMeshSpecification::Varying> varyings(varyingCount);
    for (int i = 0; i < varyingCount; ++i) {
        varyings[i] = {
            static_cast<SkMeshSpecification::Varying::Type>(varyingTypes[i]),
            varyingNames[i]
        };
    }
    env->ReleaseIntArrayElements(varyingTypesArray, varyingTypes, JNI_ABORT);

    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    auto* result = new SkMeshSpecification::Result {
        SkMeshSpecification::Make(
            attributes,
            vertexStride,
            varyings,
            skString(env, vertexShader),
            skString(env, fragmentShader),
            sk_ref_sp(colorSpace),
            static_cast<SkAlphaType>(alphaType)
        )
    };
    return ptrToJlong(result);
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshSpecificationKt__1nMeshSpecificationResultGetSpecification(
    JNIEnv*,
    jclass,
    jlong resultPtr
) {
    auto* result = jlongToPtr<SkMeshSpecification::Result*>(resultPtr);
    return ptrToJlong(result->specification.release());
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshSpecificationKt__1nMeshSpecificationResultGetError(
    JNIEnv*,
    jclass,
    jlong resultPtr
) {
    auto* result = jlongToPtr<SkMeshSpecification::Result*>(resultPtr);
    return result->error.isEmpty() ? 0 : ptrToJlong(&result->error);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_MeshSpecificationKt__1nMeshSpecificationResultDestroy(
    JNIEnv*,
    jclass,
    jlong resultPtr
) {
    delete jlongToPtr<SkMeshSpecification::Result*>(resultPtr);
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshSpecificationKt__1nGetColorSpace(
    JNIEnv*,
    jclass,
    jlong specificationPtr
) {
    auto* specification = jlongToPtr<SkMeshSpecification*>(specificationPtr);
    return ptrToJlong(sk_ref_sp(specification->colorSpace()).release());
}
