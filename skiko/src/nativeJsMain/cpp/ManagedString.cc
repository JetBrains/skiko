
// This file has been auto generated.

#include "SkString.h"
#include "common.h"
#include "mppinterop.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ManagedString__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>(&skikoMpp::finalizers::deleteString);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ManagedString__1nMake
  (KInteropPointer textStr) {
    SkString* text = new SkString(skString(textStr));
    return reinterpret_cast<KNativePointer>(text);
}

SKIKO_EXPORT KInt org_jetbrains_skia_ManagedString__nStringSize
  (KNativePointer ptr) {
    SkString* instance = reinterpret_cast<SkString*>(ptr);
    return instance->size();
}

SKIKO_EXPORT void org_jetbrains_skia_ManagedString__nStringData
  (KNativePointer ptr, KByte* bytes, KInt size) {
    SkString* instance = reinterpret_cast<SkString*>(ptr);
    memcpy(bytes, instance->c_str(), size);
}

SKIKO_EXPORT void org_jetbrains_skia_ManagedString__1nInsert
  (KNativePointer ptr, KInt offset, KInteropPointer s) {
    SkString* instance = reinterpret_cast<SkString*>(ptr);
    skija::UtfIndicesConverter conv(*instance);
    instance->insert(conv.from16To8(offset), skString(s));
}

SKIKO_EXPORT void org_jetbrains_skia_ManagedString__1nAppend
  (KNativePointer ptr, KInteropPointer s) {
    SkString* instance = reinterpret_cast<SkString*>((ptr));
    instance->append(skString(s));
}

SKIKO_EXPORT void org_jetbrains_skia_ManagedString__1nRemoveSuffix
  (KNativePointer ptr, KInt from) {
    SkString* instance = reinterpret_cast<SkString*>((ptr));
    skija::UtfIndicesConverter conv(*instance);
    size_t from8 = conv.from16To8(from);
    instance->remove(from8, instance->size() - from8);
}

SKIKO_EXPORT void org_jetbrains_skia_ManagedString__1nRemove
  (KNativePointer ptr, KInt from, KInt len) {
    SkString* instance = reinterpret_cast<SkString*>((ptr));
    skija::UtfIndicesConverter conv(*instance);
    size_t from8 = conv.from16To8(from);
    size_t to8 = conv.from16To8(from + len);
    instance->remove(from8, to8 - from8);
}
