#include "MeshInterop.hh"
#include "common.h"

static void deleteMesh(skiko::mesh::MeshWrapper* mesh) {
    delete mesh;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteMesh);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nMake(
    KNativePointer specificationPtr,
    KInt mode,
    KInteropPointer vertexData,
    KInt vertexDataSize,
    KInt vertexCount,
    KInteropPointer indexData,
    KInt indexCount,
    KFloat left,
    KFloat top,
    KFloat right,
    KFloat bottom
) {
    auto result = skiko::mesh::make(
        reinterpret_cast<SkMeshSpecification*>(specificationPtr),
        static_cast<SkMesh::Mode>(mode),
        vertexData,
        vertexDataSize,
        vertexCount,
        static_cast<uint16_t*>(indexData),
        indexCount,
        {left, top, right, bottom}
    );
    return reinterpret_cast<KNativePointer>(new skiko::mesh::MeshResult(std::move(result)));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nResultGetMesh(KNativePointer resultPtr) {
    auto* result = reinterpret_cast<skiko::mesh::MeshResult*>(resultPtr);
    skiko::mesh::MeshWrapper* mesh = result->mesh;
    result->mesh = nullptr;
    return reinterpret_cast<KNativePointer>(mesh);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nResultGetError(KNativePointer resultPtr) {
    auto* result = reinterpret_cast<skiko::mesh::MeshResult*>(resultPtr);
    return result->error.isEmpty() ? nullptr : reinterpret_cast<KNativePointer>(&result->error);
}

SKIKO_EXPORT void org_jetbrains_skia_Mesh__1nResultDestroy(KNativePointer resultPtr) {
    auto* result = reinterpret_cast<skiko::mesh::MeshResult*>(resultPtr);
    delete result->mesh;
    delete result;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nSetFloatUniform(
    KNativePointer meshPtr,
    KInteropPointer name,
    KFloat* values,
    KInt count
) {
    return reinterpret_cast<KNativePointer>(skiko::mesh::setUniform(
        reinterpret_cast<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(name),
        values,
        count,
        false
    ));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nSetIntUniform(
    KNativePointer meshPtr,
    KInteropPointer name,
    KInt* values,
    KInt count
) {
    return reinterpret_cast<KNativePointer>(skiko::mesh::setUniform(
        reinterpret_cast<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(name),
        values,
        count,
        true
    ));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nSetColorUniform(
    KNativePointer meshPtr,
    KInteropPointer name,
    KFloat r,
    KFloat g,
    KFloat b,
    KFloat a
) {
    return reinterpret_cast<KNativePointer>(skiko::mesh::setColorUniform(
        reinterpret_cast<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(name),
        r,
        g,
        b,
        a
    ));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Mesh__1nSetChild(
    KNativePointer meshPtr,
    KInteropPointer name,
    KNativePointer childPtr,
    KInt type
) {
    SkRuntimeEffect::ChildPtr child;
    switch (static_cast<SkRuntimeEffect::ChildType>(type)) {
        case SkRuntimeEffect::ChildType::kShader:
            child = sk_ref_sp(reinterpret_cast<SkShader*>(childPtr));
            break;
        case SkRuntimeEffect::ChildType::kColorFilter:
            child = sk_ref_sp(reinterpret_cast<SkColorFilter*>(childPtr));
            break;
        case SkRuntimeEffect::ChildType::kBlender:
            child = sk_ref_sp(reinterpret_cast<SkBlender*>(childPtr));
            break;
    }
    return reinterpret_cast<KNativePointer>(skiko::mesh::setChild(
        reinterpret_cast<skiko::mesh::MeshWrapper*>(meshPtr),
        skString(name),
        std::move(child),
        static_cast<SkRuntimeEffect::ChildType>(type)
    ));
}
