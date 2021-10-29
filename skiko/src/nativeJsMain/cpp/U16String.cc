
// This file has been auto generated.

#include <string>
#include "SkString.h"
#include "common.h"

static void deleteU16String(std::u16string* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteU16String));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nMake(KInteropPointer str) {
    std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> convertU8ToU16;
    std::u16string* result = new { std::move(convertU8ToU16.from_bytes(u8)) }
    return reinterpret_cast<KInteropPointer>(result);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_U16String__1nToString(KNativePointer ptr) {
    std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> convertU8ToU16;
    std::string* result = new { std::move(convertU8ToU16.to_bytes(u8)) };
    return reinterpret_cast<KInteropPointer>(result);
}
 