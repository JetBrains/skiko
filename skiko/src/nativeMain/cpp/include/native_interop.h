#ifndef NATIVE_INTEROP
#define NATIVE_INTEROP

#include "types.h"

std::unique_ptr<SkMatrix> skMatrix(KFloat* matrixArray);
std::unique_ptr<SkM44> skM44(KFloat* matrixArray);

namespace skija {
    namespace RRect {
        SkRRect toSkRRect(KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* jradii, KInt size);
    }
}

#endif // NATIVE_INTEROP
