#include "common.h"
#include "native_interop.h"

std::unique_ptr<SkMatrix> skMatrix(jfloatArray matrixArray) {
    if (matrixArray == nullptr)
        return std::unique_ptr<SkMatrix>(nullptr);
    else {
        jfloat* m = static_cast<jfloat*>(matrixArray);
        SkMatrix* ptr = new SkMatrix();
        ptr->setAll(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
        return std::unique_ptr<SkMatrix>(ptr);
    }
}


std::unique_ptr<SkM44> skM44(jfloatArray matrixArray) {
    if (matrixArray == nullptr)
        return std::unique_ptr<SkM44>(nullptr);
    else {
        jfloat* m = static_cast<jfloat *>(matrixArray);
        SkM44* ptr = new SkM44(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15]);
        return std::unique_ptr<SkM44>(ptr);
    }
}


