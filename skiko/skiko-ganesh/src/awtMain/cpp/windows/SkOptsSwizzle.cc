// Skia's Ganesh code (GrAtlasTypes) reads the variable SkOpts::RGBA_to_BGRA, which lives in the core DLL.
// Windows can only import a variable into code compiled for that (__declspec(dllimport)), and Skia's prebuilt
// libraries are not, so the read would hit core's import stub instead. Ganesh gets its own copy instead.

#include "src/core/SkSwizzlePriv.h"

static void RGBA_to_BGRA(uint32_t* dst, const uint32_t* src, int count) {
    for (int i = 0; i < count; i++) {
        uint32_t c = src[i];
        dst[i] = (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c & 0xFF) << 16);
    }
}

SkOpts::Swizzle_8888_u32 SkOpts::RGBA_to_BGRA = ::RGBA_to_BGRA;
