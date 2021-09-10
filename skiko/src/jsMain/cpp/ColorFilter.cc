#include "SkColorFilter.h"
#include "SkColorMatrixFilter.h"
#include "SkHighContrastFilter.h"
#include "SkLumaColorFilter.h"
#include "SkOverdrawColorFilter.h"
#include "SkTableColorFilter.h"

#include "common.h"

#include <emscripten.h>

EMSCRIPTEN_KEEPALIVE
extern "C" KPointer org_jetbrains_skia_ColorFilter__nMakeTableARGB(
    KByteArray arrayA, KInt arrayASize,
    KByteArray arrayR, KInt arrayRSize,
    KByteArray arrayG, KInt arrayGSize,
    KByteArray arrayB, KInt arrayBSize
) {
    SkColorFilter* ptr = SkTableColorFilter::MakeARGB(arrayA, arrayR, arrayG, arrayB).release();
    return reinterpret_cast<KPointer>(ptr);
}