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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_impl_RefCnt__getFinalizer() {
    return reinterpret_cast<KNativePointer>(&unrefSkRefCnt);
}

SKIKO_EXPORT KInt org_jetbrains_skia_impl_RefCnt__getRefCount(KNativePointer ptr) {
    SkRefCnt* instance = reinterpret_cast<SkRefCnt*>(ptr);
    return reinterpret_cast<SkRefCntHack*>(instance)->fRefCnt.load(std::memory_order_relaxed);
}
