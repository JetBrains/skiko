#include <jni.h>
#include <memory>
#include "ShaperWithFontMgr.hh"

extern "C" JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skia_ShaperFontMgrTestKt_checkFontManagerOwnership(JNIEnv*, jclass) {
#if defined(SK_BUILD_FOR_LINUX)
    sk_sp<SkFontMgr> retained;
    {
        std::unique_ptr<skiko::ShaperWithFontMgr> first(
            skiko::ShaperWithFontMgr::make(SkShapers::Primitive::PrimitiveText()));
        std::unique_ptr<skiko::ShaperWithFontMgr> second(
            skiko::ShaperWithFontMgr::make(SkShapers::Primitive::PrimitiveText()));
        retained = first->defaultFontMgr();
        if (!retained || retained.get() != first->defaultFontMgr().get()) return JNI_FALSE;
        // A process-wide singleton would fail this check and prevent fresh font discovery.
        if (retained.get() == second->defaultFontMgr().get()) return JNI_FALSE;
    }
    // Closing the shaper releases its reference, but an existing user can keep the manager alive.
    if (!retained->unique()) return JNI_FALSE;
    return retained->countFamilies() >= 0;
#else
    return JNI_TRUE;
#endif
}
