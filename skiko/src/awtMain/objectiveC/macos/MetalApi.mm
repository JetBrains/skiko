#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

#import "GrBackendSurface.h"
#import "GrDirectContext.h"
#import "ganesh/mtl/GrMtlBackendContext.h"
#import "ganesh/mtl/GrMtlDirectContext.h"
#import "ganesh/mtl/GrMtlBackendSurface.h"
#import "ganesh/mtl/GrMtlTypes.h"

#import "MetalDevice.h"

#define MuxGraphicsCard 7
#define kOpen 0
#define kGetMuxState 3
#define kDriverClassName "AppleGraphicsControl"
#define AdapterPriorityAuto 0
#define AdapterPriorityIntegrated 1
#define AdapterPriorityDiscrete 2

extern "C" {

void *objc_autoreleasePoolPush(void);
void objc_autoreleasePoolPop(void *);

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_MetalApiKt_openAutoreleasepool(
    JNIEnv *env, jobject redrawer) {
    return (jlong)objc_autoreleasePoolPush();
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_MetalApiKt_closeAutoreleasepool(
    JNIEnv *env, jobject redrawer, jlong handle) {
    objc_autoreleasePoolPop((void *)handle);
}

static BOOL isUsingIntegratedGPU() {
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

static BOOL preferLowPowerGPU(int adapterPriority) {
    switch (adapterPriority) {
        case AdapterPriorityAuto:
            return isUsingIntegratedGPU();
            
        case AdapterPriorityIntegrated:
            return YES;             
            
        default: // AdapterPriorityDiscrete or invalid adapterPriority
            return NO;
    }
}

static id<MTLDevice> createIntegratedMTLDevice(int adapterPriority) {
    if (preferLowPowerGPU(adapterPriority)) {
        NSArray<id<MTLDevice>> *devices = MTLCopyAllDevices();
        for (id<MTLDevice> device in devices) {
            if (device.isLowPower) {
                return device;
            }
        }
    }

    return MTLCreateSystemDefaultDevice();
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_MetalApiKt_chooseAdapter(
    JNIEnv *env, jobject obj, jlong adapterPriority) {
    @autoreleasepool {
        id<MTLDevice> adapter = createIntegratedMTLDevice(adapterPriority);

        if (adapter) {
            return (jlong) (__bridge_retained void *) adapter;
        } else {
            return 0;
        }
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_MetalApiKt_disposeAdapter(
    JNIEnv *env, jobject obj, jlong adapterPtr) {
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge_transfer id<MTLDevice>) (void *) adapterPtr;
    }
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_MetalApiKt_getAdapterName(
    JNIEnv *env, jobject obj, jlong adapterPtr) {
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;

        const char *currentAdapterName = [[adapter name] cStringUsingEncoding:NSUTF8StringEncoding];

        if (currentAdapterName) {
            return env->NewStringUTF(currentAdapterName);
        } else {
            return env->NewStringUTF("Unknown Metal adapter");
        }
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_MetalApiKt_getAdapterMemorySize(
    JNIEnv *env, jobject obj, jlong adapterPtr) {
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;
        uint64_t totalMemory = [adapter recommendedMaxWorkingSetSize];
        return (jlong)totalMemory;
    }
}

} // extern "C"

#endif // SK_METAL
