#include "mppinterop.h"
#include "RunRecordClone.hh"

namespace skikoMpp {
    namespace skrect {
        void serializeAs4Floats(const SkRect& rect, float* result) {
            result[0] = rect.fLeft;
            result[1] = rect.fTop;
            result[2] = rect.fRight;
            result[3] = rect.fBottom;
        }
    }

    namespace textblob {
        std::unique_ptr<SkRect> getBlockBounds(SkTextBlob* instance) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;

            auto bounds = new SkRect();
            SkFontMetrics metrics;

            while (iter.next(&run)) {
                // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                if (runRecord->positioning() != 2) // kFull_Positioning
                    return std::unique_ptr<SkRect>(nullptr);

                SkScalar* posBuffer = runRecord->posBuffer();
                const SkFont& font = runRecord->fFont;
                font.getMetrics(&metrics);

                SkScalar lastLeft = posBuffer[(run.fGlyphCount - 1) * 2];
                SkScalar lastWidth;
                if (run.fGlyphCount > 1 && SkScalarNearlyEqual(posBuffer[(run.fGlyphCount - 2) * 2], lastLeft))
                    lastWidth = 0;
                else
                    font.getWidths(&run.fGlyphIndices[run.fGlyphCount - 1], 1, &lastWidth);

                auto runBounds = SkRect::MakeLTRB(posBuffer[0], posBuffer[1] + metrics.fAscent, lastLeft + lastWidth, posBuffer[1] + metrics.fDescent);
                bounds->join(runBounds);
            }

            return std::unique_ptr<SkRect>(bounds);
        }

        std::unique_ptr<SkRect> getTightBounds(SkTextBlob* instance) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            auto bounds = new SkRect();
            SkRect tmpBounds;
            while (iter.next(&run)) {
                // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                if (runRecord->positioning() != 2) // kFull_Positioning
                    return std::unique_ptr<SkRect>(nullptr);

                runRecord->fFont.measureText(runRecord->glyphBuffer(), run.fGlyphCount * sizeof(uint16_t), SkTextEncoding::kGlyphID, &tmpBounds, nullptr);
                SkScalar* posBuffer = runRecord->posBuffer();
                tmpBounds.offset(posBuffer[0], posBuffer[1]);
                bounds->join(tmpBounds);
            }
            return std::unique_ptr<SkRect>(bounds);
        }
    }
}
