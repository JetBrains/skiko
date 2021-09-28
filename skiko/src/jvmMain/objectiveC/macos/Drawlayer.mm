#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

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
        self.container = nil;
        self.window = nil;
    }

    return self;
}

-(void) dealloc {
    self.canvasGlobalRef = NULL;
    [self.container release];
    [self.window release];
    [super dealloc];
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
        if (env->IsSameObject(object, layer.canvasGlobalRef) == JNI_TRUE)
        {
            return layer;
        }
    }
    return NULL;
}

extern "C"
{

NSWindow *recursiveWindowSearch(NSView *rootView, CALayer *layer) {
    if (rootView.subviews == nil || rootView.subviews.count == 0) {
        return nil;
    }
    for (NSView* child in rootView.subviews) {
        if (child.layer == layer) {
            return rootView.window;
        }
        NSWindow* recOut = recursiveWindowSearch(child, layer);
        if (recOut != nil) {
            return recOut;
        }
    }
    return nil;
}

NSWindow *findCALayerWindow(NSView *rootView, CALayer *layer) {
    if (rootView.layer == layer) {
        return rootView.window;
    }
    return recursiveWindowSearch(rootView, layer);
}

NSWindow *findWindow(jlong platformInfoPtr)
{
    NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;
    CALayer* ca_layer = [dsi_mac windowLayer];

    NSWindow* target_window = nil;

    NSMutableArray<NSWindow *> *windows = [NSMutableArray arrayWithArray: [[NSApplication sharedApplication] windows]];
    for (NSWindow* window in windows)
    {
        target_window = findCALayerWindow(window.contentView, ca_layer);
        if (target_window != nil) break;
    }
    return target_window;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeInit(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
{
    if (layerStorage == nil)
    {
        layerStorage = [[NSMutableSet alloc] init];
    }

    LayerHandler *layersSet = [[LayerHandler alloc] init];
    NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;
    layersSet.container = [dsi_mac windowLayer];
    jobject canvasGlobalRef = env->NewGlobalRef(canvas);
    [layersSet setCanvasGlobalRef: canvasGlobalRef];
    layersSet.window = findWindow(platformInfoPtr);

    [layerStorage addObject: layersSet];
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeDispose(JNIEnv *env, jobject canvas)
{
    LayerHandler *layer = findByObject(env, canvas);
    if (layer != NULL)
    {
        [layerStorage removeObject: layer];
        env->DeleteGlobalRef(layer.canvasGlobalRef);
        [layer release];
    }
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

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
{
    NSWindow* window = findWindow(platformInfoPtr);
    return (jlong)window;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
{
    NSWindow* window = findWindow(platformInfoPtr);
    return (jlong)window;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxDisableTitleBar(JNIEnv *env, jobject properties, jlong platformInfoPtr)
{
    NSWindow* window = findWindow(platformInfoPtr);
    if (window == nil) return;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [window setTitlebarAppearsTransparent:YES];
        [window setTitleVisibility:NSWindowTitleHidden];
        [window setStyleMask:[window styleMask]|NSWindowStyleMaskFullSizeContentView];
        // always show `fullscreen` green traffic light button instead of `maximize/zoom` button
        [window setCollectionBehavior:[window collectionBehavior]|NSWindowCollectionBehaviorFullScreenPrimary];
        [window setMovable:NO];
    });
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemThemeKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
{
    NSString *osxMode = [[NSUserDefaults standardUserDefaults] stringForKey:@"AppleInterfaceStyle"];
    if ([@"Dark" isEqualToString:osxMode]) {
        // Dark.
        return 1;
    } else {
        // Light.
        return 0;
    }
}

void getMetalDeviceAndQueue(void** device, void** queue)
{
    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];
    *device = (__bridge void*)fDevice;
    *queue = (__bridge void*)fQueue;
}



} // extern C