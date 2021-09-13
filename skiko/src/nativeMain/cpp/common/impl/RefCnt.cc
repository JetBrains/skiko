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

extern "C" jlong org_jetbrains_skia_impl_RefCnt__1nGetFinalizer() {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefSkRefCnt));
}

extern "C" jint org_jetbrains_skia_impl_RefCnt__1nGetRefCount(jlong ptr) {
    SkRefCnt* instance = reinterpret_cast<SkRefCnt*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<SkRefCntHack*>(instance)->fRefCnt.load(std::memory_order_relaxed);
}

