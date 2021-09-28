#include "common.h"

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5(KNativePointer byteArray) {
    char *bytes = reinterpret_cast<char*>(byteArray);
    bytes[0] = 1;
    bytes[1] = 2;
    bytes[2] = 3;
    bytes[3] = 4;
    bytes[4] = 5;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5(KNativePointer floatArray) {
    float *floats = reinterpret_cast<float*>(floatArray);
    floats[0] = 0.0;
    floats[1] = 1.1;
    floats[2] = 2.2;
    floats[3] = 3.3;
    floats[4] = -4.4;
}
