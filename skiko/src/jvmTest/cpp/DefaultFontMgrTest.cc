#include <jni.h>
#include <array>
#include <atomic>
#include <thread>
#include "FontMgrDefaultFactory.hh"
#include "SkTypeface.h"

extern "C" JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skia_DefaultFontMgrTestKt_checkDefaultFontManagerLifetime(JNIEnv*, jclass) {
    SkFontMgr* identity;
    {
        auto first = SkFontMgrSkikoDefault();
        auto second = SkFontMgrSkikoDefault();
        if (!first || first.get() != second.get()) return JNI_FALSE;
        identity = first.get();
        second.reset();
        // The global reference keeps the manager alive after all callers release it.
        if (first->unique()) return JNI_FALSE;
    }
    auto next = SkFontMgrSkikoDefault();
    return next.get() == identity && next->countFamilies() >= 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skia_DefaultFontMgrTestKt_checkConcurrentDefaultFontManagerAccess(JNIEnv*, jclass) {
    constexpr size_t threadCount = 8;
    std::array<std::thread, threadCount> threads;
    std::array<sk_sp<SkFontMgr>, threadCount> managers;
    std::atomic<size_t> ready{0};
    std::atomic<bool> start{false};
    std::atomic<bool> valid{true};
    for (size_t i = 0; i < threadCount; ++i) {
        threads[i] = std::thread([&, i] {
            ready.fetch_add(1);
            while (!start.load()) std::this_thread::yield();
            managers[i] = SkFontMgrSkikoDefault();
            for (int call = 0; call < 100; ++call) {
                auto manager = SkFontMgrSkikoDefault();
                if (!manager || manager.get() != managers[i].get()) {
                    valid.store(false);
                    break;
                }
                // Exercise the shared backend as well as reference acquisition/release.
                // A missing font is valid on systems without the requested character.
                auto face = manager->matchFamilyStyleCharacter(
                    nullptr, SkFontStyle(), nullptr, 0, 0x4E2D);
            }
        });
    }
    while (ready.load() != threadCount) std::this_thread::yield();
    start.store(true);
    for (auto& thread : threads) thread.join();
    for (const auto& manager : managers) {
        if (manager.get() != managers[0].get()) return JNI_FALSE;
    }
    return valid.load();
}
