#include <iostream>
#include "SkColorSpace.h"
#include "common.h"

static void copySkcmsMatrix3x3ToKFloatArray(const skcms_Matrix3x3& matrix, KFloat* result) {
    result[0] = matrix.vals[0][0];
    result[1] = matrix.vals[0][1];
    result[2] = matrix.vals[0][2];
    result[3] = matrix.vals[1][0];
    result[4] = matrix.vals[1][1];
    result[5] = matrix.vals[1][2];
    result[6] = matrix.vals[2][0];
    result[7] = matrix.vals[2][1];
    result[8] = matrix.vals[2][2];
}

SKIKO_EXPORT void org_jetbrains_skia_Matrix33__1nGetSRGB
  (KFloat* result) {
    copySkcmsMatrix3x3ToKFloatArray(SkNamedGamut::kSRGB, result);
}

SKIKO_EXPORT void org_jetbrains_skia_Matrix33__1nGetAdobeRGB
  (KFloat* result) {
    copySkcmsMatrix3x3ToKFloatArray(SkNamedGamut::kAdobeRGB, result);
}

SKIKO_EXPORT void org_jetbrains_skia_Matrix33__1nGetDisplayP3
  (KFloat* result) {
    copySkcmsMatrix3x3ToKFloatArray(SkNamedGamut::kDisplayP3, result);
}

SKIKO_EXPORT void org_jetbrains_skia_Matrix33__1nGetRec2020
  (KFloat* result) {
    copySkcmsMatrix3x3ToKFloatArray(SkNamedGamut::kRec2020, result);
}

SKIKO_EXPORT void org_jetbrains_skia_Matrix33__1nGetXYZ
  (KFloat* result) {
    copySkcmsMatrix3x3ToKFloatArray(SkNamedGamut::kXYZ, result);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Matrix33__1nAdaptToXYZD50
  (KFloat wx, KFloat wy, KFloat* result) {
    skcms_Matrix3x3 toXYZD50;
    bool success = skcms_AdaptToXYZD50(wx, wy, &toXYZD50);
    if (success)
        copySkcmsMatrix3x3ToKFloatArray(toXYZD50, result);
    return success;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Matrix33__1nPrimariesToXYZD50
  (KFloat rx, KFloat ry, KFloat gx, KFloat gy, KFloat bx, KFloat by, KFloat wx, KFloat wy, KFloat* result) {
    skcms_Matrix3x3 toXYZD50;
    bool success = skcms_PrimariesToXYZD50(rx, ry, gx, gy, bx, by, wx, wy, &toXYZD50);
    if (success)
        copySkcmsMatrix3x3ToKFloatArray(toXYZD50, result);
    return success;
}
