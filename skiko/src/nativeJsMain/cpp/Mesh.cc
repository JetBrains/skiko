#include <utility>
#include <vector>

#include "SkColorSpace.h"
#include "SkData.h"
#include "SkMesh.h"
#include "SkShader.h"
#include "MeshUniformBuilder.hh"
#include "common.h"

// SkMeshSpecification is an SkNVRefCnt, so it cannot share the generic RefCnt finalizer.
static void deleteMeshSpecification(SkMeshSpecification* specification) {
    specification->unref();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteMeshSpecification));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nMake
  (KInt* attributeTypesAndOffsetsArr, KInteropPointerArray attributeNamesArr, KInt attributeCount, KInt vertexStride,
   KInt* varyingTypesArr, KInteropPointerArray varyingNamesArr, KInt varyingCount,
   KInteropPointer vertexShader, KInteropPointer fragmentShader, KNativePointer colorSpacePtr, KInt alphaType) {
    std::vector<SkString> attributeNames = skStringVector(attributeNamesArr, attributeCount);
    std::vector<SkMeshSpecification::Attribute> attributes(attributeCount);
    for (size_t i = 0; i < attributeCount; i++) {
        // Every attribute contributes two ints: its type ordinal and its byte offset.
        attributes[i].type = static_cast<SkMeshSpecification::Attribute::Type>(attributeTypesAndOffsetsArr[i * 2]);
        attributes[i].offset = attributeTypesAndOffsetsArr[i * 2 + 1];
        attributes[i].name = attributeNames[i];
    }

    std::vector<SkString> varyingNames = skStringVector(varyingNamesArr, varyingCount);
    std::vector<SkMeshSpecification::Varying> varyings(varyingCount);
    for (size_t i = 0; i < varyingCount; i++) {
        varyings[i].type = static_cast<SkMeshSpecification::Varying::Type>(varyingTypesArr[i]);
        varyings[i].name = varyingNames[i];
    }

    // Skia rejects a null color space when the fragment program outputs a color, so a missing one
    // becomes sRGB, which is what Skia's own shorter Make overloads pass.
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);
    sk_sp<SkColorSpace> colorSpaceRef = colorSpace ? sk_ref_sp(colorSpace) : SkColorSpace::MakeSRGB();

    SkMeshSpecification::Result* result = new SkMeshSpecification::Result {
        SkMeshSpecification::Make(
            SkSpan(attributes),
            vertexStride,
            SkSpan(varyings),
            skString(vertexShader),
            skString(fragmentShader),
            std::move(colorSpaceRef),
            static_cast<SkAlphaType>(alphaType))
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshSpecification__1nGetStride
  (KNativePointer ptr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return specification->stride();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nResultGetSpecification
  (KNativePointer resultPtr) {
    auto result = reinterpret_cast<SkMeshSpecification::Result*>(resultPtr);
    return reinterpret_cast<KNativePointer>(result->specification.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nResultGetError
  (KNativePointer resultPtr) {
    auto result = reinterpret_cast<SkMeshSpecification::Result*>(resultPtr);
    if (result->error.isEmpty()) {
        return static_cast<KNativePointer>(nullptr);
    } else {
        return reinterpret_cast<KNativePointer>(&(result->error));
    }
}

SKIKO_EXPORT void org_jetbrains_skia_MeshSpecification__1nResultDestroy
  (KNativePointer resultPtr) {
    delete reinterpret_cast<SkMeshSpecification::Result*>(resultPtr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshVertexBuffer__1nMake
  (KByte* dataArr, KInt size) {
    sk_sp<SkMesh::VertexBuffer> buffer = SkMeshes::MakeVertexBuffer(dataArr, size);
    return reinterpret_cast<KNativePointer>(buffer.release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshVertexBuffer__1nGetSize
  (KNativePointer ptr) {
    SkMesh::VertexBuffer* buffer = reinterpret_cast<SkMesh::VertexBuffer*>(ptr);
    return buffer->size();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshIndexBuffer__1nMake
  (KShort* indicesArr, KInt indexCount) {
    sk_sp<SkMesh::IndexBuffer> buffer = SkMeshes::MakeIndexBuffer(indicesArr, indexCount * sizeof(uint16_t));
    return reinterpret_cast<KNativePointer>(buffer.release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshIndexBuffer__1nGetSize
  (KNativePointer ptr) {
    SkMesh::IndexBuffer* buffer = reinterpret_cast<SkMesh::IndexBuffer*>(ptr);
    return buffer->size();
}

// SkMesh is a value type, so the Kotlin peer owns a heap copy.
static void deleteMesh(SkMesh* mesh) {
    delete mesh;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteMesh));
}

// SkMesh::Make reads the uniform data while validating the mesh, so absent uniforms become empty
// data rather than null. A specification that declares uniforms then reports the shortfall as an
// error instead of dereferencing null.
static sk_sp<const SkData> meshUniforms(KNativePointer uniformsPtr) {
    const SkData* uniforms = reinterpret_cast<const SkData*>(uniformsPtr);
    return uniforms ? sk_ref_sp(uniforms) : SkData::MakeEmpty();
}

static std::vector<SkMesh::ChildPtr> meshChildren(KNativePointerArray childrenPtrsArr, KInt childCount) {
    KNativePointer* childrenPtrs = reinterpret_cast<KNativePointer*>(childrenPtrsArr);
    std::vector<SkMesh::ChildPtr> children(childCount);
    for (size_t i = 0; i < childCount; i++) {
        SkShader* si = reinterpret_cast<SkShader*>(childrenPtrs[i]);
        children[i] = SkMesh::ChildPtr(sk_ref_sp(si));
    }
    return children;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nMake
  (KNativePointer specificationPtr, KInt mode, KNativePointer vertexBufferPtr, KInt vertexCount, KInt vertexOffset,
   KNativePointer uniformsPtr, KNativePointerArray childrenPtrsArr, KInt childCount,
   KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(specificationPtr);
    SkMesh::VertexBuffer* vertexBuffer = reinterpret_cast<SkMesh::VertexBuffer*>(vertexBufferPtr);
    std::vector<SkMesh::ChildPtr> children = meshChildren(childrenPtrsArr, childCount);

    SkMesh::Result* result = new SkMesh::Result {
        SkMesh::Make(
            sk_ref_sp(specification),
            static_cast<SkMesh::Mode>(mode),
            sk_ref_sp(vertexBuffer),
            vertexCount,
            vertexOffset,
            meshUniforms(uniformsPtr),
            SkSpan(children),
            SkRect::MakeLTRB(left, top, right, bottom))
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nMakeIndexed
  (KNativePointer specificationPtr, KInt mode, KNativePointer vertexBufferPtr, KInt vertexCount, KInt vertexOffset,
   KNativePointer indexBufferPtr, KInt indexCount, KInt indexOffset,
   KNativePointer uniformsPtr, KNativePointerArray childrenPtrsArr, KInt childCount,
   KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(specificationPtr);
    SkMesh::VertexBuffer* vertexBuffer = reinterpret_cast<SkMesh::VertexBuffer*>(vertexBufferPtr);
    SkMesh::IndexBuffer* indexBuffer = reinterpret_cast<SkMesh::IndexBuffer*>(indexBufferPtr);
    std::vector<SkMesh::ChildPtr> children = meshChildren(childrenPtrsArr, childCount);

    SkMesh::Result* result = new SkMesh::Result {
        SkMesh::MakeIndexed(
            sk_ref_sp(specification),
            static_cast<SkMesh::Mode>(mode),
            sk_ref_sp(vertexBuffer),
            vertexCount,
            vertexOffset,
            sk_ref_sp(indexBuffer),
            indexCount,
            indexOffset,
            meshUniforms(uniformsPtr),
            SkSpan(children),
            SkRect::MakeLTRB(left, top, right, bottom))
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nResultGetMesh
  (KNativePointer resultPtr) {
    auto result = reinterpret_cast<SkMesh::Result*>(resultPtr);
    return reinterpret_cast<KNativePointer>(new SkMesh(result->mesh));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nResultGetError
  (KNativePointer resultPtr) {
    auto result = reinterpret_cast<SkMesh::Result*>(resultPtr);
    if (result->error.isEmpty()) {
        return static_cast<KNativePointer>(nullptr);
    } else {
        return reinterpret_cast<KNativePointer>(&(result->error));
    }
}

SKIKO_EXPORT void org_jetbrains_skia_Mesh__1nResultDestroy
  (KNativePointer resultPtr) {
    delete reinterpret_cast<SkMesh::Result*>(resultPtr);
}

static void deleteMeshUniformBuilder(MeshUniformBuilder* builder) {
    delete builder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshUniformBuilder__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteMeshUniformBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshUniformBuilder__1nMake
  (KNativePointer specificationPtr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(specificationPtr);
    MeshUniformBuilder* builder = new MeshUniformBuilder(sk_ref_sp(specification));
    return reinterpret_cast<KNativePointer>(builder);
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshUniformBuilder__1nUniformInts
  (KNativePointer builderPtr, KInteropPointer uniformName, KInt* valuesArr, KInt count) {
    MeshUniformBuilder* builder = reinterpret_cast<MeshUniformBuilder*>(builderPtr);
    SkString name = skString(uniformName);
    return builder->write(name.c_str(), valuesArr, count * sizeof(int32_t));
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshUniformBuilder__1nUniformFloats
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat* valuesArr, KInt count) {
    MeshUniformBuilder* builder = reinterpret_cast<MeshUniformBuilder*>(builderPtr);
    SkString name = skString(uniformName);
    return builder->write(name.c_str(), valuesArr, count * sizeof(float));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshUniformBuilder__1nBuild
  (KNativePointer builderPtr) {
    MeshUniformBuilder* builder = reinterpret_cast<MeshUniformBuilder*>(builderPtr);
    return reinterpret_cast<KNativePointer>(builder->build().release());
}
