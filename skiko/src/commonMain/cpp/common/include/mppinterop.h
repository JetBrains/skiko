#pragma once
#include "SkRect.h"
#include "SkTextBlob.h"
#include "SkFont.h"
#include "SkFontMetrics.h"

namespace skikoMpp {
    namespace skrect {
        void serializeAs4Floats(const SkRect& rect, float* result);
        std::unique_ptr<SkRect> toSkRect(float* topLeftRightBottom);
    }

    namespace textblob {
        std::unique_ptr<SkRect> getBlockBounds(SkTextBlob* instance);
        std::unique_ptr<SkRect> getTightBounds(SkTextBlob* instance);
        int getPositionsLength(SkTextBlob* instance);
        void getPositions(SkTextBlob* instance, float* resultArray);
        void getGlyphs(SkTextBlob* instance, short* resultArray);
        int getGlyphsLength(SkTextBlob* instance);
        bool getFirstBaseline(SkTextBlob* instance, float* resultArray);
        bool getLastBaseline(SkTextBlob* instance, float* resultArray);
        int getClustersLength(SkTextBlob* instance);
        bool getClusters(SkTextBlob* instance, int* clusters);
    }
}

namespace skija {

    class UtfIndicesConverter {
    public:
        UtfIndicesConverter(const char* chars8, size_t len8);
        UtfIndicesConverter(const SkString& s);

        const char* fStart8;
        const char* fPtr8;
        const char* fEnd8;
        uint32_t fPos16;

        size_t from16To8(uint32_t i16);
        uint32_t from8To16(size_t i8);
    };
}
