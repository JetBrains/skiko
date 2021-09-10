#include <emscripten.h>

#include "common.h"

#include "SkRefCnt.h"

class SkRefCntHack {
public:
    void* x;
    mutable std::atomic<int32_t> fRefCnt;
};

void unrefSkRefCnt(SkRefCnt* p) {
    p->unref();
}

EMSCRIPTEN_KEEPALIVE
extern "C" KPointer org_jetbrains_skia_impl_RefCnt__1nGetFinalizer() {
    return reinterpret_cast<KPointer>(&unrefSkRefCnt);
}

EMSCRIPTEN_KEEPALIVE
extern "C" KInt org_jetbrains_skia_impl_RefCnt__1nGetRefCount(KPointer ptr) {
    SkRefCnt* instance = reinterpret_cast<SkRefCnt*>(ptr);
    return reinterpret_cast<SkRefCntHack*>(instance)->fRefCnt.load(std::memory_order_relaxed);
}
