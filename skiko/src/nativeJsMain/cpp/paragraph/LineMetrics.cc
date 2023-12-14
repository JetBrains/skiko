#include "Paragraph.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_LineMetrics__1nGetArraySize
    (KNativePointer blob) {

    std::vector<LineMetrics>* vect = reinterpret_cast<std::vector<LineMetrics> *>(blob);
    return static_cast<KInt>(vect->size());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_LineMetrics__1nDisposeArray
    (KNativePointer blob) {

    std::vector<LineMetrics>* vect = reinterpret_cast<std::vector<LineMetrics> *>(blob);
    delete vect;
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_LineMetrics__1nGetArrayElement
    (KNativePointer blob, KInt index, KNativePointer intArgs, KNativePointer doubleArgs) {

    std::vector<LineMetrics>* vect = reinterpret_cast<std::vector<LineMetrics> *>(blob);
    int* ints = reinterpret_cast<int *>(intArgs);
    double* doubles = reinterpret_cast<double *>(doubleArgs);

    LineMetrics lm = vect->at(index);

    ints[0] = lm.fStartIndex;
    ints[1] = lm.fEndIndex;
    ints[2] = lm.fEndExcludingWhitespaces;
    ints[3] = lm.fEndIncludingNewline;
    ints[4] = lm.fHardBreak;
    ints[5] = lm.fLineNumber;

    doubles[0] = lm.fAscent;
    doubles[1] = lm.fDescent;
    doubles[2] = lm.fUnscaledAscent;
    doubles[3] = lm.fHeight;
    doubles[4] = lm.fWidth;
    doubles[5] = lm.fLeft;
    doubles[6] = lm.fBaseline;
}
