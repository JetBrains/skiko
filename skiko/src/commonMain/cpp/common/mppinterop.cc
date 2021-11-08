#include "mppinterop.h"
#include "RunRecordClone.hh"
#include "src/utils/SkUTF.h"

namespace skikoMpp {
    namespace skrect {
        void serializeAs4Floats(const SkRect& rect, float* result) {
            result[0] = rect.fLeft;
            result[1] = rect.fTop;
            result[2] = rect.fRight;
            result[3] = rect.fBottom;
        }

        std::unique_ptr<SkRect> toSkRect(float* topLeftRightBottom) {
            if (topLeftRightBottom == nullptr) {
                return std::unique_ptr<SkRect>(nullptr);
            } else {
                SkRect* rect = new SkRect();
                rect->setLTRB(
                    topLeftRightBottom[0], topLeftRightBottom[1],
                    topLeftRightBottom[2], topLeftRightBottom[3]
                );
                return std::unique_ptr<SkRect>(rect);
            }
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

        int getPositionsLength(SkTextBlob* instance) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            int count = 0;
            while (iter.next(&run)) {
                // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                unsigned scalarsPerGlyph = RunRecordClone::ScalarsPerGlyph(runRecord->positioning());
                count += run.fGlyphCount * scalarsPerGlyph;
            }
            return count;
        }

        void getPositions(SkTextBlob* instance, float* resultArray) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            size_t stored = 0;

            while (iter.next(&run)) {
                // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                unsigned scalarsPerGlyph = RunRecordClone::ScalarsPerGlyph(runRecord->positioning());
                memcpy(&resultArray[stored], runRecord->posBuffer(), run.fGlyphCount * scalarsPerGlyph * sizeof(SkScalar));
                stored += run.fGlyphCount * scalarsPerGlyph;
            }
        }

        void getGlyphs(SkTextBlob* instance, short* resultArray) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;

            size_t stored = 0;
            while (iter.next(&run)) {
                memcpy(&resultArray[stored], run.fGlyphIndices, run.fGlyphCount * sizeof(uint16_t));
                stored += run.fGlyphCount;
            }
        }

        int getGlyphsLength(SkTextBlob* instance) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            int stored = 0;
            while (iter.next(&run)) {
                stored += run.fGlyphCount;
            }
            return stored;
        }

        bool getFirstBaseline(SkTextBlob* instance, float* resultArray) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            if (iter.next(&run)) {
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                if (runRecord->positioning() != 2) // kFull_Positioning
                    return false;
                resultArray[0] = runRecord->posBuffer()[1];
                return true;
            }
            return false;
        }

        bool getLastBaseline(SkTextBlob* instance, float* resultArray) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            SkScalar baseline = 0;
            while (iter.next(&run)) {
                // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                if (runRecord->positioning() != 2) // kFull_Positioning
                    return false;

                baseline = std::max(baseline, runRecord->posBuffer()[1]);
            }
            resultArray[0] = baseline;
            return true;
        }

        int getClustersLength(SkTextBlob* instance) {
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            size_t count = 0;
            while (iter.next(&run)) {
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                if (!runRecord->isExtended())
                    return 0;
                count += run.fGlyphCount;
            }
            return count;
        }

        bool getClusters(SkTextBlob* instance, int* clusters) { // implementation was taken from skija
            SkTextBlob::Iter iter(*instance);
            SkTextBlob::Iter::Run run;
            size_t stored = 0;
            // uint32_t cluster8 = 0;
            uint32_t runStart16 = 0;

            while (iter.next(&run)) {
                // run.fGlyphIndices points directly to runRecord.glyphBuffer(), which comes directly after RunRecord itself
                auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
                if (!runRecord->isExtended()) {
                    return false;
                }

                skija::UtfIndicesConverter conv(runRecord->textBuffer(), runRecord->textSize());
                uint32_t* clusterBuffer = runRecord->clusterBuffer();
                for (int i = 0; i < run.fGlyphCount; ++i)
                    clusters[stored + i] = runStart16 + conv.from8To16(clusterBuffer[i]);
                runStart16 += conv.from8To16(runRecord->textSize());
                // memcpy(&clusters[stored], runRecord->clusterBuffer(), run.fGlyphCount * sizeof(uint32_t));

                stored += run.fGlyphCount;
            }
            return true;
        }
    }
}


namespace skija {
    UtfIndicesConverter::UtfIndicesConverter(const char* chars8, size_t len8):
      fStart8(chars8),
      fPtr8(chars8),
      fEnd8(chars8 + len8),
      fPos16(0)
    {}

    UtfIndicesConverter::UtfIndicesConverter(const SkString& str):
      skija::UtfIndicesConverter::UtfIndicesConverter(str.c_str(), str.size())
    {}

    size_t UtfIndicesConverter::from16To8(uint32_t i16) {
        if (i16 >= fPos16) {
            // if new i16 >= last fPos16, continue from where we started
        } else {
            fPtr8 = fStart8;
            fPos16 = 0;
        }

        while (fPtr8 < fEnd8 && fPos16 < i16) {
            SkUnichar u = SkUTF::NextUTF8(&fPtr8, fEnd8);
            fPos16 += (uint32_t) SkUTF::ToUTF16(u);
        }

        return fPtr8 - fStart8;
    }

    uint32_t UtfIndicesConverter::from8To16(size_t i8) {
        if (i8 >= (size_t) (fPtr8 - fStart8)) {
            // if new i8 >= last fPtr8, continue from where we started
        } else {
            fPtr8 = fStart8;
            fPos16 = 0;
        }

        while (fPtr8 < fEnd8 && (size_t) (fPtr8 - fStart8) < i8) {
            SkUnichar u = SkUTF::NextUTF8(&fPtr8, fEnd8);
            fPos16 += (uint32_t) SkUTF::ToUTF16(u);
        }

        return fPos16;
    }
}
