#pragma once

#include <cstring>
#include <utility>
#include <vector>

#include "SkMesh.h"

namespace skiko::mesh {

struct MeshWrapper {
    MeshWrapper(
        sk_sp<SkMeshSpecification> specification,
        SkMesh::Mode mode,
        sk_sp<SkMesh::VertexBuffer> vertices,
        size_t vertexCount,
        sk_sp<SkMesh::IndexBuffer> indices,
        size_t indexCount,
        sk_sp<SkData> uniforms,
        const SkRect& bounds
    ) : specification(std::move(specification))
      , mode(mode)
      , vertices(std::move(vertices))
      , vertexCount(vertexCount)
      , indices(std::move(indices))
      , indexCount(indexCount)
      , uniforms(std::move(uniforms))
      , children(this->specification->children().size())
      , bounds(bounds) {}

    SkMesh mesh;
    sk_sp<SkMeshSpecification> specification;
    SkMesh::Mode mode;
    sk_sp<SkMesh::VertexBuffer> vertices;
    size_t vertexCount;
    sk_sp<SkMesh::IndexBuffer> indices;
    size_t indexCount;
    sk_sp<SkData> uniforms;
    std::vector<SkRuntimeEffect::ChildPtr> children;
    SkRect bounds;

    SkString rebuild() {
        SkMesh::Result result = indices
            ? SkMesh::MakeIndexed(
                specification,
                mode,
                vertices,
                vertexCount,
                0,
                indices,
                indexCount,
                0,
                uniforms,
                children,
                bounds
            )
            : SkMesh::Make(
                specification,
                mode,
                vertices,
                vertexCount,
                0,
                uniforms,
                children,
                bounds
            );
        if (result.error.isEmpty()) mesh = std::move(result.mesh);
        return std::move(result.error);
    }
};

struct MeshResult {
    MeshWrapper* mesh = nullptr;
    SkString error;
};

inline MeshResult make(
    SkMeshSpecification* specification,
    SkMesh::Mode mode,
    const void* vertexData,
    size_t vertexDataSize,
    size_t vertexCount,
    const uint16_t* indexData,
    size_t indexCount,
    const SkRect& bounds
) {
    auto* wrapper = new MeshWrapper(
        sk_ref_sp(specification),
        mode,
        SkMeshes::MakeVertexBuffer(vertexData, vertexDataSize),
        vertexCount,
        indexCount == 0
            ? nullptr
            : SkMeshes::MakeIndexBuffer(indexData, indexCount * sizeof(uint16_t)),
        indexCount,
        SkData::MakeZeroInitialized(specification->uniformSize()),
        bounds
    );
    SkString error = wrapper->rebuild();
    if (!error.isEmpty()) {
        delete wrapper;
        return {nullptr, std::move(error)};
    }
    return {wrapper, {}};
}

inline int componentCount(SkRuntimeEffect::Uniform::Type type) {
    using Type = SkRuntimeEffect::Uniform::Type;
    switch (type) {
        case Type::kFloat:
        case Type::kInt:
            return 1;
        case Type::kFloat2:
        case Type::kInt2:
            return 2;
        case Type::kFloat3:
        case Type::kInt3:
            return 3;
        case Type::kFloat4:
        case Type::kInt4:
            return 4;
        default:
            return 0;
    }
}

inline SkString* setUniform(
    MeshWrapper* wrapper,
    const SkString& name,
    const void* values,
    int count,
    bool integer
) {
    const SkRuntimeEffect::Uniform* uniform = wrapper->mesh.spec()->findUniform(name.c_str());
    if (!uniform) {
        auto* error = new SkString();
        error->printf("No uniform named '%s'", name.c_str());
        return error;
    }

    using Type = SkRuntimeEffect::Uniform::Type;
    const bool expectedInteger = uniform->type >= Type::kInt && uniform->type <= Type::kInt4;
    if (expectedInteger != integer) {
        auto* error = new SkString();
        error->printf(
            "Uniform '%s' is not a%s uniform",
            name.c_str(),
            integer ? "n integer" : " float"
        );
        return error;
    }

    const int components = componentCount(uniform->type);
    const int expectedCount = components * (uniform->isArray() ? uniform->count : 1);
    if (components == 0 || count != expectedCount) {
        auto* error = new SkString();
        error->printf(
            "Uniform '%s' requires %d value%s, received %d",
            name.c_str(),
            expectedCount,
            expectedCount == 1 ? "" : "s",
            count
        );
        return error;
    }

    std::memcpy(
        static_cast<char*>(wrapper->uniforms->writable_data()) + uniform->offset,
        values,
        count * sizeof(uint32_t)
    );
    return nullptr;
}

inline SkString* setColorUniform(
    MeshWrapper* wrapper,
    const SkString& name,
    float r,
    float g,
    float b,
    float a
) {
    const SkRuntimeEffect::Uniform* uniform = wrapper->mesh.spec()->findUniform(name.c_str());
    if (!uniform) {
        auto* error = new SkString();
        error->printf("No uniform named '%s'", name.c_str());
        return error;
    }
    using Type = SkRuntimeEffect::Uniform::Type;
    if (
        uniform->isArray() ||
        !uniform->isColor() ||
        (uniform->type != Type::kFloat3 && uniform->type != Type::kFloat4)
    ) {
        auto* error = new SkString();
        error->printf("Uniform '%s' is not a layout(color) float3 or float4 uniform", name.c_str());
        return error;
    }

    float color[4] = {r, g, b, a};
    const int count = uniform->type == Type::kFloat3 ? 3 : 4;
    std::memcpy(
        static_cast<char*>(wrapper->uniforms->writable_data()) + uniform->offset,
        color,
        count * sizeof(float)
    );
    return nullptr;
}

inline SkString* setChild(
    MeshWrapper* wrapper,
    const SkString& name,
    SkRuntimeEffect::ChildPtr child,
    SkRuntimeEffect::ChildType type
) {
    const SkRuntimeEffect::Child* descriptor = wrapper->mesh.spec()->findChild(name.c_str());
    if (!descriptor) {
        auto* error = new SkString();
        error->printf("No child named '%s'", name.c_str());
        return error;
    }
    if (descriptor->type != type) {
        auto* error = new SkString();
        error->printf("Child '%s' has a different type", name.c_str());
        return error;
    }

    SkRuntimeEffect::ChildPtr previous = wrapper->children[descriptor->index];
    wrapper->children[descriptor->index] = std::move(child);
    SkString rebuildError = wrapper->rebuild();
    if (rebuildError.isEmpty()) return nullptr;

    wrapper->children[descriptor->index] = std::move(previous);
    return new SkString(rebuildError);
}

}  // namespace skiko::mesh
