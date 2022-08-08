#import <jawt.h>
#import <jawt_md.h>

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

#import <GrBackendSurface.h>
#import <GrDirectContext.h>
#import <mtl/GrMtlBackendContext.h>
#import <mtl/GrMtlTypes.h>

#include "GpuChoose.h"

extern "C"
{

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_HardwareInfoKt_getPreferredGpuName(
    JNIEnv *env,
    jobject obj,
    jint priority
) {
    id<MTLDevice> device = MTLCreateIntegratedDevice(priority);
    const char *name = [[device name] cStringUsingEncoding:NSASCIIStringEncoding];
    return env->NewStringUTF(name);
}

}