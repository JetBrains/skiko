
// This file has been auto generated.

#include "SkString.h"
#include "common.h"

static void deleteString(SkString* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_ManagedString__1nGetFinalizer
  () {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteString));
}


extern "C" jlong org_jetbrains_skia_ManagedString__1nMake
  (jstring textStr) {
    TODO("implement org_jetbrains_skia_ManagedString__1nMake");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ManagedString__1nMake
  (jstring textStr) {
    SkString* text = new SkString(skString(env, textStr));
    return reinterpret_cast<jlong>(text);
}
#endif



extern "C" jobject org_jetbrains_skia_ManagedString__1nToString
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_ManagedString__1nToString");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_ManagedString__1nToString
  (jlong ptr) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    return javaString(env, *instance);
}
#endif



extern "C" void org_jetbrains_skia_ManagedString__1nInsert
  (jlong ptr, jint offset, jstring s) {
    TODO("implement org_jetbrains_skia_ManagedString__1nInsert");
}
     
#if 0 
extern "C" void org_jetbrains_skia_ManagedString__1nInsert
  (jlong ptr, jint offset, jstring s) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    skija::UtfIndicesConverter conv(*instance);
    instance->insert(conv.from16To8(offset), skString(env, s));
}
#endif



extern "C" void org_jetbrains_skia_ManagedString__1nAppend
  (jlong ptr, jstring s) {
    TODO("implement org_jetbrains_skia_ManagedString__1nAppend");
}
     
#if 0 
extern "C" void org_jetbrains_skia_ManagedString__1nAppend
  (jlong ptr, jstring s) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    instance->append(skString(env, s));
}
#endif


extern "C" void org_jetbrains_skia_ManagedString__1nRemoveSuffix
  (jlong ptr, jint from) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    skija::UtfIndicesConverter conv(*instance);
    size_t from8 = conv.from16To8(from);
    instance->remove(from8, instance->size() - from8);
}

extern "C" void org_jetbrains_skia_ManagedString__1nRemove
  (jlong ptr, jint from, jint len) {
    SkString* instance = reinterpret_cast<SkString*>(static_cast<uintptr_t>(ptr));
    skija::UtfIndicesConverter conv(*instance);
    size_t from8 = conv.from16To8(from);
    size_t to8 = conv.from16To8(from + len);
    instance->remove(from8, to8 - from8);
}
