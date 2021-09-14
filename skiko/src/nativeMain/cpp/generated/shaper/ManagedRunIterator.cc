
// This file has been auto generated.

#include "SkShaper.h"
#include "common.h"

static void deleteRunIterator(SkShaper::RunIterator* instance) {
    // std::cout << "Deleting [RunIterator " << instance << "]" << std::endl;
    delete instance;
}

extern "C" jlong org_jetbrains_skia_shaper_ManagedRunIterator__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRunIterator));
}

extern "C" void org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume
  (kref __Kinstance, jlong ptr) {
    SkShaper::RunIterator* instance = reinterpret_cast<SkShaper::RunIterator*>(static_cast<uintptr_t>(ptr));
    instance->consume();
}

extern "C" jint org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun
  (kref __Kinstance, jlong ptr, jlong textPtr) {
    SkShaper::RunIterator* instance = reinterpret_cast<SkShaper::RunIterator*>(static_cast<uintptr_t>(ptr));
    SkString* text = reinterpret_cast<SkString*>(static_cast<uintptr_t>(textPtr));
    size_t end8 = instance->endOfCurrentRun();
    return skija::UtfIndicesConverter(*text).from8To16(end8);
}

extern "C" jboolean org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd
  (kref __Kinstance, jlong ptr) {
    SkShaper::RunIterator* instance = reinterpret_cast<SkShaper::RunIterator*>(static_cast<uintptr_t>(ptr));
    return instance->atEnd();
}
