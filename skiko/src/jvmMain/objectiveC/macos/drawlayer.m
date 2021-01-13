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

NSMutableArray *unknownWindows = nil;

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

extern jboolean Skiko_GetAWT(JNIEnv* env, JAWT* awt);

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_init(JNIEnv *env, jobject canvas)
{
    if (layerStorage == nil)
    {
        layerStorage = [[NSMutableSet alloc] init];
    }

    JAWT awt;
    JAWT_DrawingSurface *ds = NULL;
    JAWT_DrawingSurfaceInfo *dsi = NULL;

    jboolean result = JNI_FALSE;
    jint lock = 0;
    NSObject<JAWT_SurfaceLayers>* dsi_mac = NULL;

    awt.version = JAWT_VERSION_9 /* | JAWT_MACOSX_USE_CALAYER */;
    result = Skiko_GetAWT(env, &awt);
    assert(result != JNI_FALSE);

    ds = awt.GetDrawingSurface(env, canvas);
    assert(ds != NULL);

    lock = ds->Lock(ds);
    assert((lock & JAWT_LOCK_ERROR) == 0);

    dsi = ds->GetDrawingSurfaceInfo(ds);

    if (dsi != NULL)
    {
        dsi_mac = ( __bridge NSObject<JAWT_SurfaceLayers> *) dsi->platformInfo;

        LayerHandler *layersSet = [[LayerHandler alloc] init];

        layersSet.container = [dsi_mac windowLayer];
        jobject canvasGlobalRef = (*env)->NewGlobalRef(env, canvas);
        [layersSet setCanvasGlobalRef: canvasGlobalRef];

        if (unknownWindows == nil)
        {
            NSMutableArray<NSWindow *> *windows = [NSMutableArray arrayWithArray: [[NSApplication sharedApplication] windows]];
            layersSet.window = [windows lastObject];
            [windows removeObject: layersSet.window];
            unknownWindows = windows;
        }
        else
        {
            NSMutableArray<NSWindow *> *windows = [NSMutableArray arrayWithArray: [[NSApplication sharedApplication] windows]];
            for (NSWindow* value in unknownWindows)
            {
                [windows removeObject: value];
            }
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
        }

        [layerStorage addObject: layersSet];
    }

    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_dispose(JNIEnv *env, jobject canvas)
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
