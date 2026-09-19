#import <Metal/Metal.h>

#include <jni.h>

extern "C" JNIEXPORT jlongArray JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteTestHelpersKt__1nCreateMetalObjects(
        JNIEnv* env, jclass) {
    @autoreleasepool {
        id<MTLDevice> device = MTLCreateSystemDefaultDevice();
        id<MTLCommandQueue> queue = [device newCommandQueue];
        if (device == nil || queue == nil) {
            return env->NewLongArray(0);
        }

        jlong pointers[] = {
            reinterpret_cast<jlong>((__bridge_retained void*)device),
            reinterpret_cast<jlong>((__bridge_retained void*)queue),
        };
        jlongArray result = env->NewLongArray(2);
        env->SetLongArrayRegion(result, 0, 2, pointers);
        return result;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteTestHelpersKt__1nReleaseMetalObjects(
        JNIEnv*, jclass, jlong devicePtr, jlong queuePtr) {
    CFRelease(reinterpret_cast<CFTypeRef>(devicePtr));
    CFRelease(reinterpret_cast<CFTypeRef>(queuePtr));
}
