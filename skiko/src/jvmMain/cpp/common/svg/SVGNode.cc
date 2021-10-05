#include <jni.h>
#include "../interop.hh"
#include "SkSVGNode.h"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_svg_SVGNodeKt_SVGNode_1nGetTag
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSVGNode* instance = reinterpret_cast<SkSVGNode*>(static_cast<uintptr_t>(ptr));
    return static_cast<jint>(instance->tag());
}
