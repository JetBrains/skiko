#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#import <Cocoa/Cocoa.h>
#import <AppKit/AppKit.h>
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

@implementation AWTMetalLayer

- (id)init
{
    self = [super init];

    assert(self != NULL);

    [self removeAllAnimations];
    [self setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
    [self setNeedsDisplayOnBoundsChange: YES];

    return self;
}

@end

@implementation MetalDevice

- (id) init
{
    self = [super init];

    if (self)
    {
        self.layer = nil;
        self.adapter = nil;
        self.queue = nil;
        self.drawableHandle = nil;
    }

    return self;
}

@end

extern "C"
{

void* objc_autoreleasePoolPush(void);
void objc_autoreleasePoolPop(void*);

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_startRendering(
    JNIEnv * env, jobject redrawer)
{
    return (jlong)objc_autoreleasePoolPush();
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_endRendering(
    JNIEnv * env, jobject redrawer, jlong handle)
{
    objc_autoreleasePoolPop((void*)handle);
}

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

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_chooseAdapter(
    JNIEnv *env, jobject redrawer, jlong adapterPriority)
{
    @autoreleasepool {
        id<MTLDevice> adapter = MTLCreateIntegratedDevice(adapterPriority);
        return (jlong) (__bridge_retained void *) adapter;
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_createMetalDevice(
    JNIEnv *env, jobject redrawer, jlong windowPtr, jboolean transparency, jlong adapterPtr, jlong platformInfoPtr)
{
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge_transfer id<MTLDevice>) (void *) adapterPtr;

        MetalDevice *device = [MetalDevice new];

        NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) (void*) platformInfoPtr;

        CALayer *container = [dsi_mac windowLayer];
        [container removeAllAnimations];
        [container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [container setNeedsDisplayOnBoundsChange: YES];

        AWTMetalLayer *layer = [AWTMetalLayer new];
        [container addSublayer: layer];
        layer.javaRef = env->NewGlobalRef(redrawer);

        id<MTLCommandQueue> fQueue = [adapter newCommandQueue];

        device.container = container;
        device.layer = layer;
        device.adapter = adapter;
        device.queue = fQueue;

        device.layer.device = device.adapter;
        device.layer.pixelFormat = MTLPixelFormatBGRA8Unorm;
        device.layer.contentsGravity = kCAGravityTopLeft;

        CGFloat transparent[] = { 0.0f, 0.0f, 0.0f, 0.0f };
        device.layer.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), transparent);
        device.layer.opaque = NO;

        NSWindow* window = (__bridge NSWindow*) (void *) windowPtr;
//        device.window = window;

        if (transparency)
        {
            window.hasShadow = NO;
        }

        return (jlong) (__bridge_retained void *) device;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_resizeLayers(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jint x, jint y, jint width, jint height)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        float scale = device.layer.contentsScale;
        CGRect frame = CGRectMake(x, y, width, height);
        CGSize drawableSize = CGSizeMake(width * scale, height * scale);
        [CATransaction begin];
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        device.layer.frame = frame;
        // to avoid warning in console:
        // CAMetalLayer ignoring invalid setDrawableSize width=0.000000 height=0.000000
        if (width > 0 && height > 0) {
            device.layer.drawableSize = drawableSize;
        }
        [CATransaction commit];
        [CATransaction flush];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setLayerVisible(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jboolean isVisible)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        BOOL hidden = !isVisible;
        if (!device || !device.layer || device.layer.hidden == hidden) {
            return;
        }
        [CATransaction begin];
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        device.layer.hidden = hidden;
        [CATransaction commit];
        [CATransaction flush];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setContentScale(JNIEnv *env, jobject obj, jlong devicePtr, jfloat contentScale)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        if (!device || !device.layer || device.layer.contentsScale == contentScale) {
            return;
        }
        [CATransaction begin];
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        assert(contentScale != 0);
        device.container.contentsScale = contentScale;
        device.layer.contentsScale = contentScale;
        [CATransaction commit];
        [CATransaction flush];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setVSyncEnabled(JNIEnv *env, jobject obj, jlong devicePtr, jboolean enabled)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge MetalDevice *) (void *) devicePtr;
        device.layer.displaySyncEnabled = enabled;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_disposeDevice(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    @autoreleasepool {
        MetalDevice *device = (__bridge_transfer MetalDevice *) (void *) devicePtr;
        env->DeleteGlobalRef(device.layer.javaRef);
        [device.layer removeFromSuperlayer];
        [CATransaction flush];
    }
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_getAdapterName(
    JNIEnv *env, jobject redrawer, jlong adapterPtr)
{
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;
        const char *currentAdapterName = [[adapter name] cStringUsingEncoding:NSASCIIStringEncoding];
        return env->NewStringUTF(currentAdapterName);
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_getAdapterMemorySize(
    JNIEnv *env, jobject redrawer, jlong adapterPtr)
{
    @autoreleasepool {
        id<MTLDevice> adapter = (__bridge id<MTLDevice>) (void *) adapterPtr;
        uint64_t totalMemory = [adapter recommendedMaxWorkingSetSize];
        return (jlong)totalMemory;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_isOccluded(
    JNIEnv *env, jobject redrawer, jlong windowPtr) {
    @autoreleasepool {
        NSWindow* window = (__bridge NSWindow*) (void *) windowPtr;
        return ([window occlusionState] & NSWindowOcclusionStateVisible) == 0;
    }
}

} // extern C
#endif
