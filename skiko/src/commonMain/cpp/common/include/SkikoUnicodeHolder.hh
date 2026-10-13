// Owner of an SkUnicode instance together with the text buffer it refers to.
//
// SkUnicodes::Client::Make() keeps only a span over the text passed to it, so the
// UTF-8 buffer must outlive the created SkUnicode. This holder keeps both alive and
// is what Kotlin's SkUnicode wrapper points to.
#pragma once

#include <string>
#include "modules/skunicode/include/SkUnicode.h"

struct SkikoUnicodeHolder {
    std::string text8;
    sk_sp<SkUnicode> unicode;
};
