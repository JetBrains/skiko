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

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

@end

@interface MetalDevice : NSObject

@property (retain, strong) CALayer *container;
@property (retain, strong) AWTMetalLayer *layer;
@property (retain, strong) id<MTLDevice> device;
@property (retain, strong) id<MTLCommandQueue> queue;
@property (retain, strong) id<CAMetalDrawable> drawableHandle;

@end

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
        self.device = nil;
        self.queue = nil;
        self.drawableHandle = nil;
    }

    return self;
}

@end

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_makeMetalContext(
    JNIEnv* env, jobject redrawer, jlong devicePtr)
{
    MetalDevice *device = (MetalDevice *) devicePtr;
    GrMtlBackendContext backendContext = {};
    backendContext.fDevice.retain((GrMTLHandle) device.device);
    backendContext.fQueue.retain((GrMTLHandle) device.queue);
    return (jlong) GrDirectContext::MakeMetal(backendContext).release();
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_makeMetalRenderTarget(
    JNIEnv * env, jobject redrawer, jlong devicePtr, jint width, jint height)
{
    MetalDevice *device = (MetalDevice *) devicePtr;
    id<CAMetalDrawable> currentDrawable = [device.layer nextDrawable];
    GrMtlTextureInfo info;
    info.fTexture.retain(currentDrawable.texture);
    GrBackendRenderTarget* renderTarget = new GrBackendRenderTarget(width, height, 0, info);
    device.drawableHandle = currentDrawable;

    return (jlong) renderTarget;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_createMetalDevice(
    JNIEnv *env, jobject redrawer, jlong platformInfoPtr)
{
    MetalDevice *device = [MetalDevice new];

    NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;

    CALayer *container = [dsi_mac windowLayer];
    [container removeAllAnimations];
    [container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
    [container setNeedsDisplayOnBoundsChange: YES];

    AWTMetalLayer *layer = [AWTMetalLayer new];
    [container addSublayer: layer];
    layer.javaRef = env->NewGlobalRef(redrawer);

    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];

    device.container = container;
    device.layer = layer;
    device.device = fDevice;
    device.queue = fQueue;

    device.layer.device = device.device;
    device.layer.pixelFormat = MTLPixelFormatBGRA8Unorm;
    device.layer.contentsGravity = kCAGravityTopLeft;

    CGFloat transparent[] = { 0.0f, 0.0f, 0.0f, 0.0f };
    device.layer.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), transparent);
    device.layer.opaque = NO;

    return (jlong) device;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_resizeLayers(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jint x, jint y, jint width, jint height)
{
    MetalDevice *device = (MetalDevice *) devicePtr;
    float scale = device.layer.contentsScale;
    CGRect frame = CGRectMake(x, y, width, height);
    CGSize drawableSize = CGSizeMake(width * scale, height * scale);
    [CATransaction begin];
    [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
    device.layer.frame = frame;
    device.layer.drawableSize = drawableSize;
    [CATransaction commit];
    [CATransaction flush];
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setContentScale(JNIEnv *env, jobject obj, jlong devicePtr, jfloat contentScale)
{
    MetalDevice *device = (MetalDevice *) devicePtr;
    if (device.layer.contentsScale == contentScale) {
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

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_finishFrame(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    MetalDevice *device = (MetalDevice *) devicePtr;
    id<CAMetalDrawable> currentDrawable = device.drawableHandle;

    id<MTLCommandBuffer> commandBuffer = [device.queue commandBuffer];
    commandBuffer.label = @"Present";

    [commandBuffer presentDrawable:currentDrawable];
    [commandBuffer commit];
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_disposeDevice(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    MetalDevice *device = (MetalDevice *) devicePtr;
    [device.layer removeFromSuperlayer];
    env->DeleteGlobalRef(device.layer.javaRef);
    [device.layer release];
    [device release];
}

} // extern C
#endif