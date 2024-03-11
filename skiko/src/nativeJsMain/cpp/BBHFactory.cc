#include "common.h"
#include "SkBBHFactory.h"

static void deleteSkBBHFactory(SkBBHFactory* bbh) {
    delete bbh;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RTreeFactory__1nMake
  () {
    SkRTreeFactory* instance = new SkRTreeFactory();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BBHFactory__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>((&deleteSkBBHFactory));
}
