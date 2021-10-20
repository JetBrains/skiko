#include "Paragraph.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_TextBox__1nGetArraySize
    (KNativePointer blob) {

    std::vector<TextBox>* vect = reinterpret_cast<std::vector<TextBox> *>(blob);
    return static_cast<KInt>(vect->size());
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextBox__1nDisposeArray
    (KNativePointer blob) {

    std::vector<TextBox>* vect = reinterpret_cast<std::vector<TextBox> *>(blob);
    delete vect;
}

SKIKO_EXPORT void org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement
    (KNativePointer blob, KInt index, KNativePointer rectangleArray, KNativePointer directionArray) {

    std::vector<TextBox>* vect = reinterpret_cast<std::vector<TextBox> *>(blob);
    float* rectangle = reinterpret_cast<float *>(rectangleArray);
    int* direction = reinterpret_cast<int *>(directionArray);

    TextBox box = vect->at(index);
    rectangle[0] = box.rect.fLeft;
    rectangle[1] = box.rect.fTop;
    rectangle[2] = box.rect.fRight;
    rectangle[3] = box.rect.fBottom;
    direction[0] = static_cast<int>(box.direction);
}

