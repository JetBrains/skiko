#pragma once

#include <cstdint>
#include <cstring>
#include <string_view>
#include <utility>
#include <vector>

#include "SkData.h"
#include "SkMesh.h"
#include "SkRefCnt.h"

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
    };

    explicit MeshUniformBuilder(sk_sp<SkMeshSpecification> specification)
        : fSpecification(std::move(specification))
        , fUniforms(fSpecification->uniformSize(), 0) {}

    // Overwrites the named uniform with sizeInBytes bytes read from values.
    Status write(std::string_view name, const void* values, size_t sizeInBytes) {
        const SkMeshSpecification::Uniform* uniform = fSpecification->findUniform(name);
        if (!uniform) {
            return kUnknownUniform;
        }
        if (uniform->sizeInBytes() != sizeInBytes) {
            return kSizeMismatch;
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
    sk_sp<SkMeshSpecification> fSpecification;
    std::vector<uint8_t> fUniforms;
};
