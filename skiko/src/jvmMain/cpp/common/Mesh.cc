#include <jni.h>
#include <utility>
#include <vector>

#include "MeshUniformBuilder.hh"
#include "SkColorSpace.h"
#include "SkData.h"
#include "SkMesh.h"
#include "SkRect.h"
#include "SkShader.h"
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

    // A null color space means sRGB, matching the color space Skia's shorter Make overloads supply.
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

// SkMesh::Make reads the uniform data while validating the mesh, so absent uniforms become empty
// data rather than null. A specification that declares uniforms then reports the shortfall as an
// error instead of dereferencing null.
static sk_sp<const SkData> meshUniforms(jlong uniformsPtr) {
    const SkData* uniforms = jlongToPtr<const SkData*>(uniformsPtr);
    return uniforms ? sk_ref_sp(uniforms) : SkData::MakeEmpty();
}

static std::vector<SkMesh::ChildPtr> meshChildren(JNIEnv* env, jlongArray childrenPtrsArr, jint childCount) {
    jlong* childrenPtrs = env->GetLongArrayElements(childrenPtrsArr, 0);
    std::vector<SkMesh::ChildPtr> children;
    children.reserve(childCount);
    for (jint i = 0; i < childCount; ++i) {
        children.push_back(SkMesh::ChildPtr(sk_ref_sp(jlongToPtr<SkShader*>(childrenPtrs[i]))));
    }
    env->ReleaseLongArrayElements(childrenPtrsArr, childrenPtrs, 0);
    return children;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteMesh));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nMake
  (JNIEnv* env, jclass jclass, jlong specificationPtr, jint mode, jlong vertexBufferPtr, jint vertexCount, jint vertexOffset,
   jlong uniformsPtr, jlongArray childrenPtrsArr, jint childCount, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    std::vector<SkMesh::ChildPtr> children = meshChildren(env, childrenPtrsArr, childCount);
    SkMesh::Result result = SkMesh::Make(
        sk_ref_sp(jlongToPtr<SkMeshSpecification*>(specificationPtr)),
        static_cast<SkMesh::Mode>(mode),
        sk_ref_sp(jlongToPtr<SkMesh::VertexBuffer*>(vertexBufferPtr)),
        static_cast<size_t>(vertexCount),
        static_cast<size_t>(vertexOffset),
        meshUniforms(uniformsPtr),
        SkSpan(children),
        SkRect::MakeLTRB(left, top, right, bottom));
    return ptrToJlong(new SkMesh::Result{std::move(result)});
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshKt_Mesh_1nMakeIndexed
  (JNIEnv* env, jclass jclass, jlong specificationPtr, jint mode, jlong vertexBufferPtr, jint vertexCount, jint vertexOffset,
   jlong indexBufferPtr, jint indexCount, jint indexOffset, jlong uniformsPtr, jlongArray childrenPtrsArr, jint childCount,
   jfloat left, jfloat top, jfloat right, jfloat bottom) {
    std::vector<SkMesh::ChildPtr> children = meshChildren(env, childrenPtrsArr, childCount);
    SkMesh::Result result = SkMesh::MakeIndexed(
        sk_ref_sp(jlongToPtr<SkMeshSpecification*>(specificationPtr)),
        static_cast<SkMesh::Mode>(mode),
        sk_ref_sp(jlongToPtr<SkMesh::VertexBuffer*>(vertexBufferPtr)),
        static_cast<size_t>(vertexCount),
        static_cast<size_t>(vertexOffset),
        sk_ref_sp(jlongToPtr<SkMesh::IndexBuffer*>(indexBufferPtr)),
        static_cast<size_t>(indexCount),
        static_cast<size_t>(indexOffset),
        meshUniforms(uniformsPtr),
        SkSpan(children),
        SkRect::MakeLTRB(left, top, right, bottom));
    return ptrToJlong(new SkMesh::Result{std::move(result)});
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
    MeshUniformBuilder::Status status = builder->write(name.c_str(), values, count * sizeof(int32_t));
    env->ReleaseIntArrayElements(valuesArr, values, 0);
    return static_cast<jint>(status);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nUniformFloats
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloatArray valuesArr, jint count) {
    MeshUniformBuilder* builder = jlongToPtr<MeshUniformBuilder*>(builderPtr);
    SkString name = skString(env, uniformName);
    jfloat* values = env->GetFloatArrayElements(valuesArr, 0);
    MeshUniformBuilder::Status status = builder->write(name.c_str(), values, count * sizeof(float));
    env->ReleaseFloatArrayElements(valuesArr, values, 0);
    return static_cast<jint>(status);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_MeshUniformBuilderKt_MeshUniformBuilder_1nBuild
  (JNIEnv* env, jclass jclass, jlong builderPtr) {
    MeshUniformBuilder* builder = jlongToPtr<MeshUniformBuilder*>(builderPtr);
    return ptrToJlong(builder->build().release());
}
