#include "SkColorSpace.h"

#include "common.h"
#include <emscripten.h>

extern "C" void org_jetbrains_skia_ColorSpace__nConvert(
        KPointer fromPtr, KPointer toPtr, float r, float g, float b, float a, float* result) {
    SkColorSpace* from = reinterpret_cast<SkColorSpace*>(fromPtr);
    SkColorSpace* to = reinterpret_cast<SkColorSpace*>(toPtr);

    skcms_TransferFunction fromFn;
    from->transferFn(&fromFn);

    skcms_TransferFunction toFn;
    to->invTransferFn(&toFn);

    result[0] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, r));
    result[1] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, g));
    result[2] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, b));
    result[3] = skcms_TransferFunction_eval(&toFn, skcms_TransferFunction_eval(&fromFn, a));
}