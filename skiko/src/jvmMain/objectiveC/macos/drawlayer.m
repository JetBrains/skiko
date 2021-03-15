#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#define kNullWindowHandle NULL

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

@interface LayerHandler : NSObject

@property jobject canvasGlobalRef;
@property (retain, strong) CALayer *container;
@property (retain, strong) NSWindow *window;

@end

@implementation LayerHandler

- (id) init
{
    self = [super init];

    if (self)
    {
        self.canvasGlobalRef = NULL;
        self.container = NULL;
        self.window = NULL;
    }

    return self;
}

- (void) disposeLayer:(JNIEnv *) env
{
    (*env)->DeleteGlobalRef(env, self.canvasGlobalRef);
    self.canvasGlobalRef = NULL;
    self.container = NULL;
    self.window = NULL;
}

- (BOOL) isFullScreen {
    NSUInteger masks = [self.window styleMask];
    return (masks & NSWindowStyleMaskFullScreen) != 0;
}

- (void) makeFullscreen: (BOOL) value
{
    if (value && !self.isFullScreen)
    {
        [self.window performSelectorOnMainThread:@selector(toggleFullScreen:) withObject:nil waitUntilDone:NO];
    }
    else if (!value && self.isFullScreen)
    {
        [self.window performSelectorOnMainThread:@selector(toggleFullScreen:) withObject:nil waitUntilDone:NO];
    }
}

@end

NSMutableSet *layerStorage = nil;

LayerHandler * findByObject(JNIEnv *env, jobject object)
{
    if (layerStorage == nil)
    {
        return NULL;
    }
    for (LayerHandler* layer in layerStorage)
    {
        if ((*env)->IsSameObject(env, object, layer.canvasGlobalRef) == JNI_TRUE)
        {
            return layer;
        }
    }
    return NULL;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeInit(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
{
    if (layerStorage == nil)
    {
        layerStorage = [[NSMutableSet alloc] init];
    }

    NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;

    LayerHandler *layersSet = [[LayerHandler alloc] init];

    layersSet.container = [dsi_mac windowLayer];
    jobject canvasGlobalRef = (*env)->NewGlobalRef(env, canvas);
    [layersSet setCanvasGlobalRef: canvasGlobalRef];

    NSMutableArray<NSWindow *> *windows = [NSMutableArray arrayWithArray: [[NSApplication sharedApplication] windows]];

    for (LayerHandler* value in layerStorage)
    {
        if (layersSet.container == value.container)
        {
            layersSet.window = value.window;
        }
    }

    if (layersSet.window == NULL)
    {
        layersSet.window = [windows lastObject];
    }

    [layerStorage addObject: layersSet];
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeDispose(JNIEnv *env, jobject canvas)
{
    LayerHandler *layer = findByObject(env, canvas);
    if (layer != NULL)
    {
        [layerStorage removeObject: layer];
        [layer disposeLayer: env];
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas)
{
    return (jlong)kNullWindowHandle;
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxIsFullscreenNative(JNIEnv *env, jobject properties, jobject component)
{
    LayerHandler *layer = findByObject(env, component);
    if (layer != NULL)
    {
        return [layer isFullScreen];
    }
    return false;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxSetFullscreenNative(JNIEnv *env, jobject properties, jobject component, jboolean value)
{
    LayerHandler *layer = findByObject(env, component);
    if (layer != NULL)
    {
        [layer makeFullscreen:value];
    }
}

void getMetalDeviceAndQueue(void** device, void** queue)
{
    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];
    *device = (__bridge void*)fDevice;
    *queue = (__bridge void*)fQueue;
}
