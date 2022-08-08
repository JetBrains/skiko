#import "GpuChoose.h"

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

#define MuxGraphicsCard 7
#define kOpen 0
#define kGetMuxState 3
#define kDriverClassName "AppleGraphicsControl"
#define AdapterPriorityAuto 0
#define AdapterPriorityIntegrated 1
#define AdapterPriorityDiscrete 2

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

    if (adapterPriority == AdapterPriorityAuto) {
        isIntegratedGPU = isUsingIntegratedGPU();
    } else if (adapterPriority == AdapterPriorityIntegrated) {
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