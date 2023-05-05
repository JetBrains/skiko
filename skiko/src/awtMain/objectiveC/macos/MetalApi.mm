#ifdef SK_METAL

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

#import "MetalDevice.h"

#define MuxGraphicsCard 7
#define kOpen 0
#define kGetMuxState 3
#define kDriverClassName "AppleGraphicsControl"
#define AdpapterPriorityAuto 0
#define AdpapterPriorityIntegrated 1
#define AdpapterPriorityDiscrete 2

extern "C"
{

BOOL isUsingIntegratedGPU() {
    kern_return_t kernResult = 0;
    io_iterator_t iterator = IO_OBJECT_NULL;
    io_service_t service = IO_OBJECT_NULL;

    kernResult = IOServiceGetMatchingServices(kIOMasterPortDefault, IOServiceMatching(kDriverClassName), &iterator);
    assert(kernResult == KERN_SUCCESS);

    service = IOIteratorNext(iterator);
    io_connect_t switcherConnect;

    kernResult = IOServiceOpen(service, mach_task_self(), 0, &switcherConnect);
    if (kernResult != KERN_SUCCESS) return 0;

    kernResult = IOConnectCallScalarMethod(switcherConnect, kOpen, NULL, 0, NULL, NULL);
    if (kernResult != KERN_SUCCESS) return 0;

    uint64_t output;
    uint32_t outputCount = 1;
    uint64_t scalarI_64[2] = { 1, MuxGraphicsCard };

    kernResult = IOConnectCallScalarMethod(switcherConnect,
                                           kGetMuxState,
                                           scalarI_64,
                                           2,
                                           &output,
                                           &outputCount);
    if (kernResult != KERN_SUCCESS) return 0;
    return output != 0;
}

id<MTLDevice> MTLCreateIntegratedDevice(int adapterPriority) {
    BOOL isIntegratedGPU = NO;

    if (adapterPriority == AdpapterPriorityAuto) {
        isIntegratedGPU = isUsingIntegratedGPU();
    } else if (adapterPriority == AdpapterPriorityIntegrated) {
        isIntegratedGPU = YES;
    }

    id<MTLDevice> gpu = nil;

    if (isIntegratedGPU) {
        NSArray<id<MTLDevice>> *devices = MTLCopyAllDevices();
        for (id<MTLDevice> device in devices) {
            if (device.isLowPower) {
                gpu = device;
                break;
            }
        }
    }
    if (gpu == nil) {
        gpu = MTLCreateSystemDefaultDevice();
    }
    return gpu;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_MetalApiKt_chooseAdapter(
    JNIEnv *env, jobject obj, jlong adapterPriority)
{
    @autoreleasepool {
        id<MTLDevice> adapter = MTLCreateIntegratedDevice(adapterPriority);
        return (jlong) (__bridge_retained void *) adapter;
    }
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_MetalApiKt_getAdapterName(
    JNIEnv *env, jobject obj, jlong adapterPtr)
{
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;
        const char *currentAdapterName = [[adapter name] cStringUsingEncoding:NSASCIIStringEncoding];
        return env->NewStringUTF(currentAdapterName);
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_MetalApiKt_getAdapterMemorySize(
    JNIEnv *env, jobject obj, jlong adapterPtr)
{
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;
        uint64_t totalMemory = [adapter recommendedMaxWorkingSetSize];
        return (jlong)totalMemory;
    }
}

} // extern C
#endif
