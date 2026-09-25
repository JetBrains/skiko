#pragma once

#include <cstdint>
#include <cstring>
#include <string_view>
#include <utility>
#include <vector>

#include "SkBlender.h"
#include "SkColorFilter.h"
#include "SkData.h"
#include "SkMesh.h"
#include "SkRuntimeEffect.h"
#include "SkRefCnt.h"
#include "SkShader.h"

// Accumulates values for the uniforms declared by an SkMeshSpecification into the flat buffer that
// SkMesh::Make expects. The buffer is sized from the specification and zero-filled, so uniforms that
// are never written read as zero.
class MeshUniformBuilder {
public:
    // Status codes shared with the Kotlin peer.
    enum Status : int32_t {
        kOk = 0,
        kUnknownUniform = 1,
        kSizeMismatch = 2,
        kKindMismatch = 3,
    };

    // The two kinds of value a uniform holds. An int and a float are the same width, so a write
    // states which kind it carries and the declared type decides whether it fits.
    enum class ValueKind {
        kInt,
        kFloat,
    };

    explicit MeshUniformBuilder(sk_sp<SkMeshSpecification> specification)
        : fSpecification(std::move(specification))
        , fUniforms(fSpecification->uniformSize(), 0) {}

    // Overwrites the named uniform with sizeInBytes bytes of kind read from values.
    Status write(std::string_view name, ValueKind kind, const void* values, size_t sizeInBytes) {
        const SkMeshSpecification::Uniform* uniform = fSpecification->findUniform(name);
        if (!uniform) {
            return kUnknownUniform;
        }
        if (uniform->sizeInBytes() != sizeInBytes) {
            return kSizeMismatch;
        }
        if (uniformValueKind(uniform->type) != kind) {
            return kKindMismatch;
        }
        memcpy(fUniforms.data() + uniform->offset, values, sizeInBytes);
        return kOk;
    }

    sk_sp<SkData> build() const {
        if (fUniforms.empty()) {
            return SkData::MakeEmpty();
        }
        return SkData::MakeWithCopy(fUniforms.data(), fUniforms.size());
    }

private:
    static ValueKind uniformValueKind(SkMeshSpecification::Uniform::Type type) {
        switch (type) {
            case SkMeshSpecification::Uniform::Type::kInt:
            case SkMeshSpecification::Uniform::Type::kInt2:
            case SkMeshSpecification::Uniform::Type::kInt3:
            case SkMeshSpecification::Uniform::Type::kInt4:
                return ValueKind::kInt;
            case SkMeshSpecification::Uniform::Type::kFloat:
            case SkMeshSpecification::Uniform::Type::kFloat2:
            case SkMeshSpecification::Uniform::Type::kFloat3:
            case SkMeshSpecification::Uniform::Type::kFloat4:
            case SkMeshSpecification::Uniform::Type::kFloat2x2:
            case SkMeshSpecification::Uniform::Type::kFloat3x3:
            case SkMeshSpecification::Uniform::Type::kFloat4x4:
                return ValueKind::kFloat;
        }
        return ValueKind::kFloat;
    }

    sk_sp<SkMeshSpecification> fSpecification;
    std::vector<uint8_t> fUniforms;
};

// The number of ints the Kotlin peer reads per uniform out of the reflection buffer: offset, type,
// count, size and flags, in that order.
inline constexpr int32_t kMeshUniformFields = 5;

// SkMesh::Make reads the uniform data while validating the mesh, so an absent uniform buffer becomes
// empty data rather than null. A specification that declares uniforms then reports the shortfall as
// an error instead of dereferencing null.
inline sk_sp<const SkData> meshUniforms(const SkData* uniforms) {
    return uniforms ? sk_ref_sp(uniforms) : SkData::MakeEmpty();
}

// A pointer alone cannot say which SkFlattenable subclass a child must be upcast from, so the Kotlin
// peer sends an SkRuntimeEffect::ChildType alongside each one. This value stands for a child left
// unbound, which no ChildType names.
inline constexpr int32_t kUnboundMeshChild = -1;

inline SkMesh::ChildPtr meshChild(void* child, int32_t type) {
    if (type == kUnboundMeshChild) {
        return SkMesh::ChildPtr();
    }
    switch (static_cast<SkRuntimeEffect::ChildType>(type)) {
        case SkRuntimeEffect::ChildType::kShader:
            return SkMesh::ChildPtr(sk_ref_sp(static_cast<SkShader*>(child)));
        case SkRuntimeEffect::ChildType::kColorFilter:
            return SkMesh::ChildPtr(sk_ref_sp(static_cast<SkColorFilter*>(child)));
        case SkRuntimeEffect::ChildType::kBlender:
            return SkMesh::ChildPtr(sk_ref_sp(static_cast<SkBlender*>(child)));
    }
    return SkMesh::ChildPtr();
}
