#include "Paragraph.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_LineMetrics__1nPopArrayElement
    (KNativePointer blob, KNativePointer indexArg, KNativePointer longArgs, KNativePointer doubleArgs) {

    if (blob == nullptr) return nullptr;
    std::vector<LineMetrics>* vect = reinterpret_cast<std::vector<LineMetrics> *>(blob);
    int* index = reinterpret_cast<int*>(indexArg);
    float* longs = reinterpret_cast<float *>(longArgs);
    int* doubles = reinterpret_cast<int *>(doubleArgs);

    int size = vect->size();
    index[0] = size - 1;
    if (size == 0) {
        delete vect;
        return nullptr;
    } else {
        LineMetrics lm = vect->back();
        vect->pop_back();

        longs[0] = lm.fStartIndex;
        longs[1] = lm.fEndIndex;
        longs[2] = lm.fEndExcludingWhitespaces;
        longs[3] = lm.fEndIncludingNewline;
        longs[4] = lm.fHardBreak;
        longs[5] = lm.fLineNumber;

        doubles[0] = lm.fAscent;
        doubles[1] = lm.fDescent;
        doubles[2] = lm.fUnscaledAscent;
        doubles[3] = lm.fHeight;
        doubles[4] = lm.fWidth;
        doubles[5] = lm.fLeft;
        doubles[6] = lm.fBaseline;
        return reinterpret_cast<KNativePointer>(vect);
    }
}
