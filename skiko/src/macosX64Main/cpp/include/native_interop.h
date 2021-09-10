#ifndef NATIVE_INTEROP
#define NATIVE_INTEROP

std::unique_ptr<SkMatrix> skMatrix(jfloatArray matrixArray);
std::unique_ptr<SkM44> skM44(jfloatArray matrixArray);

namespace skija {
    namespace RRect {
        SkRRect toSkRRect(jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray jradii, jint size);
    }
}

#endif // NATIVE_INTEROP
