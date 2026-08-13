#include <vector>

#include "SkColorSpace.h"
#include "SkMesh.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nMake(
    KInt* attributeTypes,
    KInt* attributeOffsets,
    KInteropPointerArray attributeNamesArray,
    KInt vertexStride,
    KInt* varyingTypes,
    KInteropPointerArray varyingNamesArray,
    KInt attributeCount,
    KInt varyingCount,
    KInteropPointer vertexShader,
    KInteropPointer fragmentShader,
    KNativePointer colorSpacePtr,
    KInt alphaType
) {
    std::vector<SkString> attributeNames = skStringVector(attributeNamesArray, attributeCount);
    std::vector<SkMeshSpecification::Attribute> attributes(attributeCount);
    for (int i = 0; i < attributeCount; ++i) {
        attributes[i] = {
            static_cast<SkMeshSpecification::Attribute::Type>(attributeTypes[i]),
            static_cast<size_t>(attributeOffsets[i]),
            attributeNames[i]
        };
    }

    std::vector<SkString> varyingNames = skStringVector(varyingNamesArray, varyingCount);
    std::vector<SkMeshSpecification::Varying> varyings(varyingCount);
    for (int i = 0; i < varyingCount; ++i) {
        varyings[i] = {
            static_cast<SkMeshSpecification::Varying::Type>(varyingTypes[i]),
            varyingNames[i]
        };
    }

    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);
    auto* result = new SkMeshSpecification::Result {
        SkMeshSpecification::Make(
            attributes,
            vertexStride,
            varyings,
            skString(vertexShader),
            skString(fragmentShader),
            sk_ref_sp(colorSpace),
            static_cast<SkAlphaType>(alphaType)
        )
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nResultGetSpecification(
    KNativePointer resultPtr
) {
    auto* result = reinterpret_cast<SkMeshSpecification::Result*>(resultPtr);
    return reinterpret_cast<KNativePointer>(result->specification.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nResultGetError(
    KNativePointer resultPtr
) {
    auto* result = reinterpret_cast<SkMeshSpecification::Result*>(resultPtr);
    return result->error.isEmpty() ? nullptr : reinterpret_cast<KNativePointer>(&result->error);
}

SKIKO_EXPORT void org_jetbrains_skia_MeshSpecification__1nResultDestroy(KNativePointer resultPtr) {
    delete reinterpret_cast<SkMeshSpecification::Result*>(resultPtr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nGetColorSpace(
    KNativePointer specificationPtr
) {
    auto* specification = reinterpret_cast<SkMeshSpecification*>(specificationPtr);
    return reinterpret_cast<KNativePointer>(sk_ref_sp(specification->colorSpace()).release());
}
