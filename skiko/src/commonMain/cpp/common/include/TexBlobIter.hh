#pragma once

#include "SkTextBlob.h"

class TextBlobIter {
public:
    TextBlobIter(SkTextBlob* blob): _blob(sk_ref_sp(blob)), _iter(*blob), _buffer(), _hasNext() {
        fetch();
    }

    bool fetch() {
        _hasNext = _iter.next(&_buffer);
        return _hasNext;
    }

    bool hasNext() const {
        return _hasNext;
    }

    sk_sp<SkTypeface> getTypeface() const {
        return sk_ref_sp(_buffer.fTypeface);
    }

    int getGlyphCount() const {
        return _buffer.fGlyphCount;
    }

    int writeGlyphs(uint16_t* dst, int max) const {
        int count = std::min(max, _buffer.fGlyphCount);
        std::memcpy(dst, _buffer.fGlyphIndices, count * sizeof(uint16_t));
        return _buffer.fGlyphCount;
    }

private:
    SkTextBlob::Iter::Run _buffer;
    SkTextBlob::Iter _iter;
    bool _hasNext;
    sk_sp<SkTextBlob> _blob;
};
