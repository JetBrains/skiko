#include "Paragraph.h"
using namespace std;
using namespace skia::textlayout;
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_paragraph_TextBox__1nPopArrayElement
    (KNativePointer blob, KNativePointer indexArray, KNativePointer rectangleArray, KNativePointer directionArray) {

    if (blob == nullptr) return nullptr;
    std::vector<TextBox>* vect = reinterpret_cast<std::vector<TextBox> *>(blob);
    int* index = reinterpret_cast<int*>(indexArray);
    float* rectangle = reinterpret_cast<float *>(rectangleArray);
    int* direction = reinterpret_cast<int *>(directionArray);

    int size = vect->size();
    index[0] = size - 1;
    if (size == 0) {
        delete vect;
        return nullptr;
    } else {
        TextBox box = vect->back();
        vect->pop_back();
        rectangle[0] = box.rect.fLeft;
        rectangle[1] = box.rect.fTop;
        rectangle[2] = box.rect.fRight;
        rectangle[3] = box.rect.fBottom;
        direction[0] = static_cast<int>(box.direction);
        return reinterpret_cast<KNativePointer>(vect);
    }
}