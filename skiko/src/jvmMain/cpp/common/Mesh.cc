#include <jni.h>
#include <utility>
#include <vector>

#include "MeshInterop.hh"
#include "SkColorSpace.h"
#include "SkData.h"
#include "SkMesh.h"
#include "SkRect.h"
#include "SkSpan.h"
#include "interop.hh"

// SkMeshSpecification is an SkNVRefCnt, so it cannot share the generic RefCnt finalizer.
static void deleteMeshSpecification(SkMeshSpecification* specification) {
    specification->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteMeshSpecification));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nMake
  (JNIEnv* env, jclass jclass, jintArray attributeTypesAndOffsetsArr, jobjectArray attributeNamesArr, jint attributeCount,
   jint vertexStride, jintArray varyingTypesArr, jobjectArray varyingNamesArr, jint varyingCount,
   jstring vertexShader, jstring fragmentShader, jlong colorSpacePtr, jint alphaType) {
    std::vector<SkString> attributeNames = skStringVector(env, attributeNamesArr);
    std::vector<SkString> varyingNames = skStringVector(env, varyingNamesArr);

    // Every attribute is encoded as 2 ints: its type ordinal and its byte offset.
    jint* attributeTypesAndOffsets = env->GetIntArrayElements(attributeTypesAndOffsetsArr, 0);
    std::vector<SkMeshSpecification::Attribute> attributes;
    attributes.reserve(attributeCount);
    for (jint i = 0; i < attributeCount; ++i) {
        attributes.push_back({
            static_cast<SkMeshSpecification::Attribute::Type>(attributeTypesAndOffsets[2 * i]),
            static_cast<size_t>(attributeTypesAndOffsets[2 * i + 1]),
            attributeNames[i]
        });
    }
    env->ReleaseIntArrayElements(attributeTypesAndOffsetsArr, attributeTypesAndOffsets, 0);

    jint* varyingTypes = env->GetIntArrayElements(varyingTypesArr, 0);
    std::vector<SkMeshSpecification::Varying> varyings;
    varyings.reserve(varyingCount);
    for (jint i = 0; i < varyingCount; ++i) {
        varyings.push_back({
            static_cast<SkMeshSpecification::Varying::Type>(varyingTypes[i]),
            varyingNames[i]
        });
    }
    env->ReleaseIntArrayElements(varyingTypesArr, varyingTypes, 0);

    // Skia rejects a null color space when the fragment program outputs a color, so a missing one
    // becomes sRGB, which is what Skia's own shorter Make overloads pass.
    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    SkMeshSpecification::Result result = SkMeshSpecification::Make(
        SkSpan(attributes),
        static_cast<size_t>(vertexStride),
        SkSpan(varyings),
        skString(env, vertexShader),
        skString(env, fragmentShader),
        colorSpace ? sk_ref_sp(colorSpace) : SkColorSpace::MakeSRGB(),
        static_cast<SkAlphaType>(alphaType));
    return ptrToJlong(new SkMeshSpecification::Result{std::move(result)});
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetStride
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return static_cast<jint>(instance->stride());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetColorSpace
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return ptrToJlong(sk_ref_sp(instance->colorSpace()).release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetAttributeCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return static_cast<jint>(instance->attributes().size());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetAttributeFields
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray resultArray) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    SkSpan<const SkMeshSpecification::Attribute> attributes = instance->attributes();
    jint* fields = env->GetIntArrayElements(resultArray, 0);
    for (size_t i = 0; i < attributes.size(); ++i) {
        fields[2 * i] = static_cast<jint>(attributes[i].type);
        fields[2 * i + 1] = static_cast<jint>(attributes[i].offset);
    }
    env->ReleaseIntArrayElements(resultArray, fields, 0);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetAttributeName
  (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return ptrToJlong(new SkString(instance->attributes()[index].name));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetUniformSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return static_cast<jint>(instance->uniformSize());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetUniformCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return static_cast<jint>(instance->uniforms().size());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetUniformFields
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray resultArray) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    SkSpan<const SkMeshSpecification::Uniform> uniforms = instance->uniforms();
    jint* fields = env->GetIntArrayElements(resultArray, 0);
    for (size_t i = 0; i < uniforms.size(); ++i) {
        jint* uniform = fields + i * kMeshUniformFields;
        uniform[0] = static_cast<jint>(uniforms[i].offset);
        uniform[1] = static_cast<jint>(uniforms[i].type);
        uniform[2] = static_cast<jint>(uniforms[i].count);
        uniform[3] = static_cast<jint>(uniforms[i].sizeInBytes());
        uniform[4] = static_cast<jint>(uniforms[i].flags);
    }
    env->ReleaseIntArrayElements(resultArray, fields, 0);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetUniformName
  (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    // The name is a string view into the program's source, so it is not null terminated.
    std::string_view name = instance->uniforms()[index].name;
    return ptrToJlong(new SkString(name.data(), name.size()));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetChildCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    return static_cast<jint>(instance->children().size());
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetChildFields
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray resultArray) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    SkSpan<const SkMeshSpecification::Child> children = instance->children();
    jint* fields = env->GetIntArrayElements(resultArray, 0);
    for (size_t i = 0; i < children.size(); ++i) {
        fields[2 * i] = static_cast<jint>(children[i].type);
        fields[2 * i + 1] = static_cast<jint>(children[i].index);
    }
    env->ReleaseIntArrayElements(resultArray, fields, 0);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nGetChildName
  (JNIEnv* env, jclass jclass, jlong ptr, jint index) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    // The name is a string view into the program's source, so it is not null terminated.
    std::string_view name = instance->children()[index].name;
    return ptrToJlong(new SkString(name.data(), name.size()));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nFindVaryingType
  (JNIEnv* env, jclass jclass, jlong ptr, jstring name) {
    SkMeshSpecification* instance = jlongToPtr<SkMeshSpecification*>(ptr);
    SkString varyingName = skString(env, name);
    const SkMeshSpecification::Varying* varying = instance->findVarying(varyingName.c_str());
    return varying ? static_cast<jint>(varying->type) : -1;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nResultGetSpecification
  (JNIEnv* env, jclass jclass, jlong resultPtr) {
    SkMeshSpecification::Result* result = jlongToPtr<SkMeshSpecification::Result*>(resultPtr);
    return ptrToJlong(result->specification.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nResultGetError
  (JNIEnv* env, jclass jclass, jlong resultPtr) {
    SkMeshSpecification::Result* result = jlongToPtr<SkMeshSpecification::Result*>(resultPtr);
    return result->error.isEmpty() ? 0 : ptrToJlong(&result->error);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_MeshSpecificationKt_MeshSpecification_1nResultDestroy
  (JNIEnv* env, jclass jclass, jlong resultPtr) {
    delete jlongToPtr<SkMeshSpecification::Result*>(resultPtr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshVertexBufferKt_MeshVertexBuffer_1nMake
  (JNIEnv* env, jclass jclass, jbyteArray dataArr, jint size) {
    jbyte* data = env->GetByteArrayElements(dataArr, 0);
    sk_sp<SkMesh::VertexBuffer> buffer = SkMeshes::MakeVertexBuffer(data, static_cast<size_t>(size));
    env->ReleaseByteArrayElements(dataArr, data, 0);
    return ptrToJlong(buffer.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshVertexBufferKt_MeshVertexBuffer_1nMakeFromFloats
  (JNIEnv* env, jclass jclass, jfloatArray dataArr, jint count) {
    jfloat* data = env->GetFloatArrayElements(dataArr, 0);
    sk_sp<SkMesh::VertexBuffer> buffer = SkMeshes::MakeVertexBuffer(data, count * sizeof(float));
    env->ReleaseFloatArrayElements(dataArr, data, 0);
    return ptrToJlong(buffer.release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshVertexBufferKt_MeshVertexBuffer_1nGetSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh::VertexBuffer* instance = jlongToPtr<SkMesh::VertexBuffer*>(ptr);
    return static_cast<jint>(instance->size());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshIndexBufferKt_MeshIndexBuffer_1nMake
  (JNIEnv* env, jclass jclass, jshortArray indicesArr, jint indexCount) {
    jshort* indices = env->GetShortArrayElements(indicesArr, 0);
    sk_sp<SkMesh::IndexBuffer> buffer = SkMeshes::MakeIndexBuffer(indices, indexCount * sizeof(uint16_t));
    env->ReleaseShortArrayElements(indicesArr, indices, 0);
    return ptrToJlong(buffer.release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshIndexBufferKt_MeshIndexBuffer_1nGetSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh::IndexBuffer* instance = jlongToPtr<SkMesh::IndexBuffer*>(ptr);
    return static_cast<jint>(instance->size());
}

// SkMesh is a value type, so the Kotlin peer owns a heap copy.
static void deleteMesh(SkMesh* mesh) {
    delete mesh;
}

static std::vector<SkMesh::ChildPtr> meshChildren(JNIEnv* env, jlongArray childrenPtrsArr, jintArray childTypesArr,
                                                  jint childCount) {
    jlong* childrenPtrs = env->GetLongArrayElements(childrenPtrsArr, 0);
    jint* childTypes = env->GetIntArrayElements(childTypesArr, 0);
    std::vector<SkMesh::ChildPtr> children;
    children.reserve(childCount);
    for (jint i = 0; i < childCount; ++i) {
        children.push_back(meshChild(jlongToPtr<void*>(childrenPtrs[i]), childTypes[i]));
    }
    env->ReleaseIntArrayElements(childTypesArr, childTypes, 0);
    env->ReleaseLongArrayElements(childrenPtrsArr, childrenPtrs, 0);
    return children;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteMesh));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nMake
  (JNIEnv* env, jclass jclass, jlong specificationPtr, jint mode, jlong vertexBufferPtr, jint vertexCount, jint vertexOffset,
   jlong uniformsPtr, jlongArray childrenPtrsArr, jintArray childTypesArr, jint childCount,
   jfloat left, jfloat top, jfloat right, jfloat bottom) {
    std::vector<SkMesh::ChildPtr> children = meshChildren(env, childrenPtrsArr, childTypesArr, childCount);
    SkMesh::Result result = SkMesh::Make(
        sk_ref_sp(jlongToPtr<SkMeshSpecification*>(specificationPtr)),
        static_cast<SkMesh::Mode>(mode),
        sk_ref_sp(jlongToPtr<SkMesh::VertexBuffer*>(vertexBufferPtr)),
        static_cast<size_t>(vertexCount),
        static_cast<size_t>(vertexOffset),
        meshUniforms(jlongToPtr<const SkData*>(uniformsPtr)),
        SkSpan(children),
        SkRect::MakeLTRB(left, top, right, bottom));
    return ptrToJlong(new SkMesh::Result{std::move(result)});
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nMakeIndexed
  (JNIEnv* env, jclass jclass, jlong specificationPtr, jint mode, jlong vertexBufferPtr, jint vertexCount, jint vertexOffset,
   jlong indexBufferPtr, jint indexCount, jint indexOffset, jlong uniformsPtr, jlongArray childrenPtrsArr,
   jintArray childTypesArr, jint childCount, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    std::vector<SkMesh::ChildPtr> children = meshChildren(env, childrenPtrsArr, childTypesArr, childCount);
    SkMesh::Result result = SkMesh::MakeIndexed(
        sk_ref_sp(jlongToPtr<SkMeshSpecification*>(specificationPtr)),
        static_cast<SkMesh::Mode>(mode),
        sk_ref_sp(jlongToPtr<SkMesh::VertexBuffer*>(vertexBufferPtr)),
        static_cast<size_t>(vertexCount),
        static_cast<size_t>(vertexOffset),
        sk_ref_sp(jlongToPtr<SkMesh::IndexBuffer*>(indexBufferPtr)),
        static_cast<size_t>(indexCount),
        static_cast<size_t>(indexOffset),
        meshUniforms(jlongToPtr<const SkData*>(uniformsPtr)),
        SkSpan(children),
        SkRect::MakeLTRB(left, top, right, bottom));
    return ptrToJlong(new SkMesh::Result{std::move(result)});
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetSpecification
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return ptrToJlong(instance->refSpec().release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetMode
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return static_cast<jint>(instance->mode());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetVertexBuffer
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return ptrToJlong(instance->refVertexBuffer().release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetVertexOffset
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return static_cast<jint>(instance->vertexOffset());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetVertexCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return static_cast<jint>(instance->vertexCount());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetIndexBuffer
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return ptrToJlong(instance->refIndexBuffer().release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetIndexOffset
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return static_cast<jint>(instance->indexOffset());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetIndexCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    return static_cast<jint>(instance->indexCount());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetUniforms
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    // SkData is immutable, so the Kotlin peer may hold a reference to the very same object.
    return ptrToJlong(const_cast<SkData*>(instance->refUniforms().release()));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetBounds
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray resultArray) {
    SkMesh* instance = jlongToPtr<SkMesh*>(ptr);
    skija::Rect::copyToInterop(env, instance->bounds(), resultArray);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nResultGetMesh
  (JNIEnv* env, jclass jclass, jlong resultPtr) {
    SkMesh::Result* result = jlongToPtr<SkMesh::Result*>(resultPtr);
    return ptrToJlong(new SkMesh(result->mesh));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nResultGetError
  (JNIEnv* env, jclass jclass, jlong resultPtr) {
    SkMesh::Result* result = jlongToPtr<SkMesh::Result*>(resultPtr);
    return result->error.isEmpty() ? 0 : ptrToJlong(&result->error);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nResultDestroy
  (JNIEnv* env, jclass jclass, jlong resultPtr) {
    delete jlongToPtr<SkMesh::Result*>(resultPtr);
}

static void deleteMeshUniformBuilder(MeshUniformBuilder* builder) {
    delete builder;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteMeshUniformBuilder));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nMake
  (JNIEnv* env, jclass jclass, jlong specificationPtr) {
    SkMeshSpecification* specification = jlongToPtr<SkMeshSpecification*>(specificationPtr);
    return ptrToJlong(new MeshUniformBuilder(sk_ref_sp(specification)));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nUniformInts
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jintArray valuesArr, jint count) {
    MeshUniformBuilder* builder = jlongToPtr<MeshUniformBuilder*>(builderPtr);
    SkString name = skString(env, uniformName);
    jint* values = env->GetIntArrayElements(valuesArr, 0);
    MeshUniformBuilder::Status status = builder->write(name.c_str(), MeshUniformBuilder::ValueKind::kInt, values, count * sizeof(int32_t));
    env->ReleaseIntArrayElements(valuesArr, values, 0);
    return static_cast<jint>(status);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nUniformFloats
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloatArray valuesArr, jint count) {
    MeshUniformBuilder* builder = jlongToPtr<MeshUniformBuilder*>(builderPtr);
    SkString name = skString(env, uniformName);
    jfloat* values = env->GetFloatArrayElements(valuesArr, 0);
    MeshUniformBuilder::Status status = builder->write(name.c_str(), MeshUniformBuilder::ValueKind::kFloat, values, count * sizeof(float));
    env->ReleaseFloatArrayElements(valuesArr, values, 0);
    return static_cast<jint>(status);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nBuild
  (JNIEnv* env, jclass jclass, jlong builderPtr) {
    MeshUniformBuilder* builder = jlongToPtr<MeshUniformBuilder*>(builderPtr);
    return ptrToJlong(builder->build().release());
}
