#include "common.h"
#include <stdint.h>

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5(KNativePointer byteArray) {
    uint8_t *bytes = reinterpret_cast<uint8_t*>(byteArray);
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

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5(KNativePointer shortArray) {
    short *shorts = reinterpret_cast<short*>(shortArray);
    shorts[0] = 0;
    shorts[1] = 1;
    shorts[2] = 2;
    shorts[3] = -3;
    shorts[4] = 4;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5(KNativePointer intArray) {
    int *ints = reinterpret_cast<int*>(intArray);
    ints[0] = 0;
    ints[1] = 1;
    ints[2] = -22;
    ints[3] = 3;
    ints[4] = 4;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5(KNativePointer doubleArray) {
    double *doubles = reinterpret_cast<double*>(doubleArray);
    doubles[0] = -0.001;
    doubles[1] = 0.00222;
    doubles[2] = 2.71828;
    doubles[3] = 3.1415;
    doubles[4] = 10000000.9991;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__nStringByIndex(KInt index) {
    switch (index) {
        case 0: return new SkString("Hello");
        case 1: return new SkString("Привет");
        case 2: return new SkString("你好");
        default: TODO("unknown");
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nWriteArraysOfInts(KNativePointer* arrayOfIntArray) {
    // hardcoded length is ok for testing purposes
    size_t len = 3; //(*env)->GetArrayLength(env, arrayOfIntArray);

    int *mem = reinterpret_cast<int *>(malloc(3 * 4 * 4)); // 3 arrays. each array consists of 4 ints

    for (int i = 0; i < len; i++) {
        KNativePointer* array = reinterpret_cast<KNativePointer*>(arrayOfIntArray[i]);
        int *ints = reinterpret_cast<int*>(array);
        for (int j = 0; j < 4; j++) {
            mem[(i * 4) + j] = ints[j];
        }
    }

    return reinterpret_cast<KNativePointer>(mem);
}
