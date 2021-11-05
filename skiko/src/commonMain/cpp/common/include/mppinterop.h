#include "SkRect.h"
#include "SkTextBlob.h"
#include "SkFont.h"
#include "SkFontMetrics.h"

namespace skikoMpp {
    namespace skrect {
        void serializeAs4Floats(const SkRect& rect, float* result);
    }

    namespace textblob {
        std::unique_ptr<SkRect> getBlockBounds(SkTextBlob* instance);
    }
}
