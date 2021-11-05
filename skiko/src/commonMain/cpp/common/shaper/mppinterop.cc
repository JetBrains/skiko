#include "mppinterop.h"

namespace skikoMpp {
    namespace skrect {
        void serializeAs4Floats(const SkRect& rect, float* result) {
            result[0] = rect.fLeft;
            result[1] = rect.fTop;
            result[2] = rect.fRight;
            result[3] = rect.fBottom;
        }
    }
}
