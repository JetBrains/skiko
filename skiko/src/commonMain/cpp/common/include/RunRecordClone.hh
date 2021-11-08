// Must match SkTextBlobPriv.h
//
// Extended Textblob runs have more data after the Pos[] array:
//
//    -------------------------------------------------------------------------
//    ... | RunRecord | Glyphs[] | Pos[] | TextSize | Clusters[] | Text[] | ...
//    -------------------------------------------------------------------------
class RunRecordClone {
public:
    SkFont    fFont;
    uint32_t  fCount;
    SkPoint   fOffset;
    uint32_t  fFlags;

    uint16_t* glyphBuffer() const {
        // Glyphs are stored immediately following the record.
        return reinterpret_cast<uint16_t*>(const_cast<RunRecordClone*>(this) + 1);
    }

    SkScalar* posBuffer() const {
        // Position scalars follow the (aligned) glyph buffer.
        return reinterpret_cast<SkScalar*>(reinterpret_cast<uint8_t*>(this->glyphBuffer()) +
                                           SkAlign4(fCount * sizeof(uint16_t)));
    }

    uint32_t* textSizePtr() const {
        // textSize follows the position buffer.
        return (uint32_t*)(&this->posBuffer()[fCount * ScalarsPerGlyph(positioning())]);
    }

    uint32_t textSize() const {
        return isExtended() ? *this->textSizePtr() : 0;
    }

    uint32_t* clusterBuffer() const {
        // clusters follow the textSize.
        return isExtended() ? 1 + this->textSizePtr() : nullptr;
    }

    char* textBuffer() const {
        return isExtended()
               ? reinterpret_cast<char*>(this->clusterBuffer() + fCount)
               : nullptr;
    }

    uint8_t positioning() const {
        return fFlags & 0x3; // kPositioning_Mask
    }

    bool isExtended() const {
        return fFlags & 0x8; // kExtended_Flag
    }

    static unsigned ScalarsPerGlyph(uint8_t pos) {
        uint8_t res = 0;
        switch(pos) {
            case 0: // kDefault_Positioning
            case 1: // kHorizontal_Positioning
            case 2: // kFull_Positioning
                res = pos;
                break;
            case 3: // kRSXform_Positioning
                res = 4;
                break;
        }
        return res;
    }
};
