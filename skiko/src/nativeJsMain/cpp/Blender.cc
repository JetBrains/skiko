#include <iostream>
#include "SkBlender.h"
#include "SkBlenders.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Blender__1nMakeArithmetic
  (KFloat k1, KFloat k2, KFloat k3, KFloat k4, KBoolean enforcePMColor) {
    SkBlender* ptr = SkBlenders::Arithmetic(k1, k2, k3, k4, enforcePMColor).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
