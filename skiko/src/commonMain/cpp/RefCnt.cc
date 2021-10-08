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

#ifdef SK_BUILD_FOR_LINUX
// TODO: fix properly, now just stub this incompatible symbol.
// See https://gcc.gnu.org/bugzilla/show_bug.cgi?id=88782
// We likely shall switch to more recent Skia build, i.e. ubuntu18 variant.
extern "C" void _ZNSt19_Sp_make_shared_tag5_S_eqERKSt9type_info() {
    TODO("_ZNSt19_Sp_make_shared_tag5_S_eqERKSt9type_info called!");
}
#endif