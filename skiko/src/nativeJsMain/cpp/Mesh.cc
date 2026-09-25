#include <utility>
#include <vector>

#include "SkColorSpace.h"
#include "SkData.h"
#include "SkMesh.h"
#include "MeshInterop.hh"
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nGetColorSpace
  (KNativePointer ptr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return reinterpret_cast<KNativePointer>(sk_ref_sp(specification->colorSpace()).release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshSpecification__1nGetAttributeCount
  (KNativePointer ptr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return specification->attributes().size();
}

SKIKO_EXPORT void org_jetbrains_skia_MeshSpecification__1nGetAttributeFields
  (KNativePointer ptr, KInt* resultArray) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    SkSpan<const SkMeshSpecification::Attribute> attributes = specification->attributes();
    for (size_t i = 0; i < attributes.size(); i++) {
        resultArray[2 * i] = static_cast<KInt>(attributes[i].type);
        resultArray[2 * i + 1] = static_cast<KInt>(attributes[i].offset);
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nGetAttributeName
  (KNativePointer ptr, KInt index) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return reinterpret_cast<KNativePointer>(new SkString(specification->attributes()[index].name));
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshSpecification__1nGetUniformSize
  (KNativePointer ptr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return specification->uniformSize();
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshSpecification__1nGetUniformCount
  (KNativePointer ptr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return specification->uniforms().size();
}

SKIKO_EXPORT void org_jetbrains_skia_MeshSpecification__1nGetUniformFields
  (KNativePointer ptr, KInt* resultArray) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    SkSpan<const SkMeshSpecification::Uniform> uniforms = specification->uniforms();
    for (size_t i = 0; i < uniforms.size(); i++) {
        KInt* uniform = resultArray + i * kMeshUniformFields;
        uniform[0] = static_cast<KInt>(uniforms[i].offset);
        uniform[1] = static_cast<KInt>(uniforms[i].type);
        uniform[2] = static_cast<KInt>(uniforms[i].count);
        uniform[3] = static_cast<KInt>(uniforms[i].sizeInBytes());
        uniform[4] = static_cast<KInt>(uniforms[i].flags);
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nGetUniformName
  (KNativePointer ptr, KInt index) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    // The name is a string view into the program's source, so it is not null terminated.
    std::string_view name = specification->uniforms()[index].name;
    return reinterpret_cast<KNativePointer>(new SkString(name.data(), name.size()));
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshSpecification__1nGetChildCount
  (KNativePointer ptr) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    return specification->children().size();
}

SKIKO_EXPORT void org_jetbrains_skia_MeshSpecification__1nGetChildFields
  (KNativePointer ptr, KInt* resultArray) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    SkSpan<const SkMeshSpecification::Child> children = specification->children();
    for (size_t i = 0; i < children.size(); i++) {
        resultArray[2 * i] = static_cast<KInt>(children[i].type);
        resultArray[2 * i + 1] = static_cast<KInt>(children[i].index);
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshSpecification__1nGetChildName
  (KNativePointer ptr, KInt index) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    // The name is a string view into the program's source, so it is not null terminated.
    std::string_view name = specification->children()[index].name;
    return reinterpret_cast<KNativePointer>(new SkString(name.data(), name.size()));
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshSpecification__1nFindVaryingType
  (KNativePointer ptr, KInteropPointer name) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(ptr);
    SkString varyingName = skString(name);
    const SkMeshSpecification::Varying* varying = specification->findVarying(varyingName.c_str());
    return varying ? static_cast<KInt>(varying->type) : -1;
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshVertexBuffer__1nMakeFromFloats
  (KFloat* dataArr, KInt count) {
    sk_sp<SkMesh::VertexBuffer> buffer = SkMeshes::MakeVertexBuffer(dataArr, count * sizeof(float));
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

static std::vector<SkMesh::ChildPtr> meshChildren(KNativePointerArray childrenPtrsArr, KInt* childTypesArr,
                                                 KInt childCount) {
    KNativePointer* childrenPtrs = reinterpret_cast<KNativePointer*>(childrenPtrsArr);
    std::vector<SkMesh::ChildPtr> children(childCount);
    for (size_t i = 0; i < childCount; i++) {
        children[i] = meshChild(childrenPtrs[i], childTypesArr[i]);
    }
    return children;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nMake
  (KNativePointer specificationPtr, KInt mode, KNativePointer vertexBufferPtr, KInt vertexCount, KInt vertexOffset,
   KNativePointer uniformsPtr, KNativePointerArray childrenPtrsArr, KInt* childTypesArr, KInt childCount,
   KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(specificationPtr);
    SkMesh::VertexBuffer* vertexBuffer = reinterpret_cast<SkMesh::VertexBuffer*>(vertexBufferPtr);
    std::vector<SkMesh::ChildPtr> children = meshChildren(childrenPtrsArr, childTypesArr, childCount);

    SkMesh::Result* result = new SkMesh::Result {
        SkMesh::Make(
            sk_ref_sp(specification),
            static_cast<SkMesh::Mode>(mode),
            sk_ref_sp(vertexBuffer),
            vertexCount,
            vertexOffset,
            meshUniforms(reinterpret_cast<const SkData*>(uniformsPtr)),
            SkSpan(children),
            SkRect::MakeLTRB(left, top, right, bottom))
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nMakeIndexed
  (KNativePointer specificationPtr, KInt mode, KNativePointer vertexBufferPtr, KInt vertexCount, KInt vertexOffset,
   KNativePointer indexBufferPtr, KInt indexCount, KInt indexOffset,
   KNativePointer uniformsPtr, KNativePointerArray childrenPtrsArr, KInt* childTypesArr, KInt childCount,
   KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkMeshSpecification* specification = reinterpret_cast<SkMeshSpecification*>(specificationPtr);
    SkMesh::VertexBuffer* vertexBuffer = reinterpret_cast<SkMesh::VertexBuffer*>(vertexBufferPtr);
    SkMesh::IndexBuffer* indexBuffer = reinterpret_cast<SkMesh::IndexBuffer*>(indexBufferPtr);
    std::vector<SkMesh::ChildPtr> children = meshChildren(childrenPtrsArr, childTypesArr, childCount);

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
            meshUniforms(reinterpret_cast<const SkData*>(uniformsPtr)),
            SkSpan(children),
            SkRect::MakeLTRB(left, top, right, bottom))
    };
    return reinterpret_cast<KNativePointer>(result);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nGetSpecification
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return reinterpret_cast<KNativePointer>(mesh->refSpec().release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Mesh__1nGetMode
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return static_cast<KInt>(mesh->mode());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nGetVertexBuffer
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return reinterpret_cast<KNativePointer>(mesh->refVertexBuffer().release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Mesh__1nGetVertexOffset
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return mesh->vertexOffset();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Mesh__1nGetVertexCount
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return mesh->vertexCount();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nGetIndexBuffer
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return reinterpret_cast<KNativePointer>(mesh->refIndexBuffer().release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Mesh__1nGetIndexOffset
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return mesh->indexOffset();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Mesh__1nGetIndexCount
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    return mesh->indexCount();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nGetUniforms
  (KNativePointer ptr) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    // SkData is immutable, so the Kotlin peer may hold a reference to the very same object.
    return reinterpret_cast<KNativePointer>(const_cast<SkData*>(mesh->refUniforms().release()));
}

SKIKO_EXPORT void org_jetbrains_skia_Mesh__1nGetBounds
  (KNativePointer ptr, KInteropPointer resultArray) {
    SkMesh* mesh = reinterpret_cast<SkMesh*>(ptr);
    skija::Rect::copyToInterop(mesh->bounds(), resultArray);
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
    return builder->write(name.c_str(), MeshUniformBuilder::ValueKind::kInt, valuesArr, count * sizeof(int32_t));
}

SKIKO_EXPORT KInt org_jetbrains_skia_MeshUniformBuilder__1nUniformFloats
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat* valuesArr, KInt count) {
    MeshUniformBuilder* builder = reinterpret_cast<MeshUniformBuilder*>(builderPtr);
    SkString name = skString(uniformName);
    return builder->write(name.c_str(), MeshUniformBuilder::ValueKind::kFloat, valuesArr, count * sizeof(float));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_MeshUniformBuilder__1nBuild
  (KNativePointer builderPtr) {
    MeshUniformBuilder* builder = reinterpret_cast<MeshUniformBuilder*>(builderPtr);
    return reinterpret_cast<KNativePointer>(builder->build().release());
}
