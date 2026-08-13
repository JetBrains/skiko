#include <jni.h>

#include "MeshInterop.hh"
#include "interop.hh"

static void deleteMesh(skiko::mesh::MeshWrapper* mesh) {
    delete mesh;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nGetFinalizer(JNIEnv*, jclass) {
    return ptrToJlong(&deleteMesh);
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nMake(
    JNIEnv* env,
    jclass,
    jlong specificationPtr,
    jint mode,
    jobject vertexDataObject,
    jint vertexDataSize,
    jint vertexCount,
    jobject indexDataObject,
    jint indexCount,
    jfloat left,
    jfloat top,
    jfloat right,
    jfloat bottom
) {
    void* vertexData = env->GetPrimitiveArrayCritical(static_cast<jarray>(vertexDataObject), nullptr);
    void* indexData = indexDataObject == nullptr
        ? nullptr
        : env->GetPrimitiveArrayCritical(static_cast<jarray>(indexDataObject), nullptr);
    auto result = skiko::mesh::make(
        jlongToPtr<SkMeshSpecification*>(specificationPtr),
        static_cast<SkMesh::Mode>(mode),
        vertexData,
        vertexDataSize,
        vertexCount,
        static_cast<uint16_t*>(indexData),
        indexCount,
        {left, top, right, bottom}
    );
    if (indexData != nullptr) {
        env->ReleasePrimitiveArrayCritical(static_cast<jarray>(indexDataObject), indexData, JNI_ABORT);
    }
    env->ReleasePrimitiveArrayCritical(static_cast<jarray>(vertexDataObject), vertexData, JNI_ABORT);
    return ptrToJlong(new skiko::mesh::MeshResult(std::move(result)));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nMeshResultGetMesh(JNIEnv*, jclass, jlong resultPtr) {
    auto* result = jlongToPtr<skiko::mesh::MeshResult*>(resultPtr);
    skiko::mesh::MeshWrapper* mesh = result->mesh;
    result->mesh = nullptr;
    return ptrToJlong(mesh);
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nMeshResultGetError(JNIEnv*, jclass, jlong resultPtr) {
    auto* result = jlongToPtr<skiko::mesh::MeshResult*>(resultPtr);
    return result->error.isEmpty() ? 0 : ptrToJlong(&result->error);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_MeshKt__1nMeshResultDestroy(JNIEnv*, jclass, jlong resultPtr) {
    auto* result = jlongToPtr<skiko::mesh::MeshResult*>(resultPtr);
    delete result->mesh;
    delete result;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nSetFloatUniform(
    JNIEnv* env,
    jclass,
    jlong meshPtr,
    jstring name,
    jfloatArray values,
    jint count
) {
    jfloat* data = env->GetFloatArrayElements(values, nullptr);
    SkString* error = skiko::mesh::setUniform(
        jlongToPtr<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(env, name),
        data,
        count,
        false
    );
    env->ReleaseFloatArrayElements(values, data, JNI_ABORT);
    return ptrToJlong(error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nSetIntUniform(
    JNIEnv* env,
    jclass,
    jlong meshPtr,
    jstring name,
    jintArray values,
    jint count
) {
    jint* data = env->GetIntArrayElements(values, nullptr);
    SkString* error = skiko::mesh::setUniform(
        jlongToPtr<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(env, name),
        data,
        count,
        true
    );
    env->ReleaseIntArrayElements(values, data, JNI_ABORT);
    return ptrToJlong(error);
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nSetColorUniform(
    JNIEnv* env,
    jclass,
    jlong meshPtr,
    jstring name,
    jfloat r,
    jfloat g,
    jfloat b,
    jfloat a
) {
    return ptrToJlong(skiko::mesh::setColorUniform(
        jlongToPtr<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(env, name),
        r,
        g,
        b,
        a
    ));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_MeshKt__1nSetChild(
    JNIEnv* env,
    jclass,
    jlong meshPtr,
    jstring name,
    jlong childPtr,
    jint type
) {
    SkRuntimeEffect::ChildPtr child;
    switch (static_cast<SkRuntimeEffect::ChildType>(type)) {
        case SkRuntimeEffect::ChildType::kShader:
            child = sk_ref_sp(jlongToPtr<SkShader*>(childPtr));
            break;
        case SkRuntimeEffect::ChildType::kColorFilter:
            child = sk_ref_sp(jlongToPtr<SkColorFilter*>(childPtr));
            break;
        case SkRuntimeEffect::ChildType::kBlender:
            child = sk_ref_sp(jlongToPtr<SkBlender*>(childPtr));
            break;
    }
    return ptrToJlong(skiko::mesh::setChild(
        jlongToPtr<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(env, name),
        std::move(child),
        static_cast<SkRuntimeEffect::ChildType>(type)
    ));
}
