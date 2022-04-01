#include "SkShaper.h"
#include "common.h"

static void deleteRunIterator(SkShaper::RunIterator* instance) {
    // std::cout << "Deleting [RunIterator " << instance << "]" << std::endl;
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_shaper_ManagedRunIterator__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteRunIterator));
}

SKIKO_EXPORT void org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume
  (KNativePointer ptr) {
    SkShaper::RunIterator* instance = reinterpret_cast<SkShaper::RunIterator*>((ptr));
    instance->consume();
}

SKIKO_EXPORT KInt org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun
  (KNativePointer ptr, KNativePointer textPtr) {
    SkShaper::RunIterator* instance = reinterpret_cast<SkShaper::RunIterator*>((ptr));
    SkString* text = reinterpret_cast<SkString*>((textPtr));
    size_t end8 = instance->endOfCurrentRun();
    return skija::UtfIndicesConverter(*text).from8To16(end8);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd
  (KNativePointer ptr) {
    SkShaper::RunIterator* instance = reinterpret_cast<SkShaper::RunIterator*>((ptr));
    return instance->atEnd();
}
