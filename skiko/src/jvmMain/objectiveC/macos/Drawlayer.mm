#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

@interface WindowDragView : NSView
@end

@implementation WindowDragView

- (void)mouseDown:(NSEvent *)event
{
    [self.window performWindowDragWithEvent:event];
    [super mouseDown:event];
}

- (void)mouseUp:(NSEvent *)event
{
    if (event.clickCount == 2)
    {
        [self.window performZoom:nil];
    }
    [super mouseUp:event];
}

@end

@interface LayerHandler : NSObject

@property jobject canvasGlobalRef;
@property (retain, strong) CALayer *container;
@property (retain, strong) NSWindow *window;

@end

@implementation LayerHandler
{
    BOOL _titlebarDisabled;
    CGFloat _customHeaderHeight;
}

- (id) init
{
    self = [super init];

    if (self)
    {
        self.canvasGlobalRef = NULL;
        self.container = nil;
        self.window = nil;
        _titlebarDisabled = NO;
    }

    return self;
}

-(void) dealloc
{
    self.canvasGlobalRef = NULL;
    [self.container release];
    [self.window release];
    [super dealloc];
}

- (void) setupCustomHeader
{
    NSView* themeFrame = self.window.contentView.superview;
    NSView* titlebarContainer = themeFrame.subviews[1];
    NSView* titlebar = titlebarContainer.subviews[0];
    NSView* titlebarDecoration = titlebarContainer.subviews[1];
    NSView* titlebarVisualEffect = titlebar.subviews[0];
    NSView* titlebarBackground = titlebar.subviews[1];
    NSView* dragger = [[WindowDragView alloc] init];
    [titlebarBackground addSubview:dragger];

    titlebarContainer.translatesAutoresizingMaskIntoConstraints = NO;
    titlebarDecoration.translatesAutoresizingMaskIntoConstraints = NO;
    titlebar.translatesAutoresizingMaskIntoConstraints = NO;
    titlebarVisualEffect.translatesAutoresizingMaskIntoConstraints = NO;
    titlebarBackground.translatesAutoresizingMaskIntoConstraints = NO;
    dragger.translatesAutoresizingMaskIntoConstraints = NO;

    [NSLayoutConstraint activateConstraints:@[
        [titlebarBackground.leftAnchor constraintEqualToAnchor:themeFrame.leftAnchor],
        [titlebarBackground.rightAnchor constraintEqualToAnchor:themeFrame.rightAnchor],
        [titlebarBackground.topAnchor constraintEqualToAnchor:themeFrame.topAnchor],
        [titlebarBackground.heightAnchor constraintEqualToConstant:_customHeaderHeight],

        [titlebarVisualEffect.leftAnchor constraintEqualToAnchor:titlebarBackground.leftAnchor],
        [titlebarVisualEffect.rightAnchor constraintEqualToAnchor:titlebarBackground.rightAnchor],
        [titlebarVisualEffect.topAnchor constraintEqualToAnchor:titlebarBackground.topAnchor],
        [titlebarVisualEffect.bottomAnchor constraintEqualToAnchor:titlebarBackground.bottomAnchor],

        [titlebar.leftAnchor constraintEqualToAnchor:titlebarBackground.leftAnchor],
        [titlebar.widthAnchor constraintEqualToAnchor:titlebarBackground.widthAnchor],
        [titlebar.topAnchor constraintEqualToAnchor:titlebarBackground.topAnchor],
        [titlebar.heightAnchor constraintEqualToAnchor:titlebarBackground.heightAnchor],

        [titlebarDecoration.leftAnchor constraintEqualToAnchor:titlebarBackground.leftAnchor],
        [titlebarDecoration.widthAnchor constraintEqualToAnchor:titlebarBackground.widthAnchor],
        [titlebarDecoration.topAnchor constraintEqualToAnchor:titlebarBackground.topAnchor],
        [titlebarDecoration.heightAnchor constraintEqualToAnchor:titlebarBackground.heightAnchor],

        [titlebarContainer.leftAnchor constraintEqualToAnchor:titlebar.leftAnchor],
        [titlebarContainer.rightAnchor constraintEqualToAnchor:titlebar.rightAnchor],
        [titlebarContainer.topAnchor constraintEqualToAnchor:titlebar.topAnchor],
        [titlebarContainer.heightAnchor constraintEqualToAnchor:titlebar.heightAnchor],

        [dragger.leftAnchor constraintEqualToAnchor:titlebarBackground.leftAnchor],
        [dragger.rightAnchor constraintEqualToAnchor:titlebarBackground.rightAnchor],
        [dragger.topAnchor constraintEqualToAnchor:titlebarBackground.topAnchor],
        [dragger.bottomAnchor constraintEqualToAnchor:titlebarBackground.bottomAnchor],
    ]];

    NSView* closeButtonView = [self.window standardWindowButton:NSWindowCloseButton];
    NSView* miniaturizeButtonView = [self.window standardWindowButton:NSWindowMiniaturizeButton];
    NSView* zoomButtonView = [self.window standardWindowButton:NSWindowZoomButton];
    CGFloat horizontalButtonOffset = 20.0;

    NSArray* windowButtons = @[ closeButtonView, miniaturizeButtonView, zoomButtonView ];
    for (NSUInteger i = 0; i < windowButtons.count; i++) {
        NSView* button = [windowButtons objectAtIndex:i];
        button.translatesAutoresizingMaskIntoConstraints = NO;
        [NSLayoutConstraint activateConstraints:@[
            [button.centerYAnchor constraintEqualToAnchor:titlebar.centerYAnchor],
            [button.centerXAnchor constraintEqualToAnchor:titlebar.leftAnchor constant:(_customHeaderHeight/2.0 + (i * horizontalButtonOffset))],
        ]];
    }
}

- (void) resetHeader
{
    NSView* themeFrame = self.window.contentView.superview;
    NSView* titlebarContainer = themeFrame.subviews[1];
    NSView* titlebar = titlebarContainer.subviews[0];
    NSView* titlebarDecoration = titlebarContainer.subviews[1];
    NSView* titlebarVisualEffect = titlebar.subviews[0];
    NSView* titlebarBackground = titlebar.subviews[1];
    NSView* closeButtonView = [self.window standardWindowButton:NSWindowCloseButton];
    NSView* miniaturizeButtonView = [self.window standardWindowButton:NSWindowMiniaturizeButton];
    NSView* zoomButtonView = [self.window standardWindowButton:NSWindowZoomButton];

    NSArray* changedViews = @[titlebarContainer, titlebarDecoration, titlebar, titlebarVisualEffect, titlebarBackground, closeButtonView, miniaturizeButtonView, zoomButtonView];
    for (NSView* changedView in changedViews)
    {
        [changedView removeConstraints:changedView.constraints];
        changedView.translatesAutoresizingMaskIntoConstraints = YES;
    }
    NSView* dragger = titlebar.subviews[1].subviews[0];
    [dragger removeFromSuperview];
}

- (void) disableTitlebar: (CGFloat) customHeaderHeight
{
    _customHeaderHeight = customHeaderHeight;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [self.window setTitlebarAppearsTransparent:YES];
        [self.window setTitleVisibility:NSWindowTitleHidden];
        [self.window setStyleMask:[self.window styleMask]|NSWindowStyleMaskFullSizeContentView];

        if (!self.isFullScreen) {
            [self setupCustomHeader];
        }
    });
    _titlebarDisabled = YES;
}

- (BOOL) isFullScreen
{
    NSUInteger masks = [self.window styleMask];
    return (masks & NSWindowStyleMaskFullScreen) != 0;
}

- (void) resetHeaderAndEnableFullscreen
{
    [self resetHeader];
    [self.window toggleFullScreen:nil];
}

- (void) disableFullScreenAndSetupCustomHeader
{
    [self.window toggleFullScreen:nil];
    [self setupCustomHeader];
}

- (void) makeFullscreen: (BOOL) value
{
    if (value && !self.isFullScreen)
    {
        if (_titlebarDisabled) {
            // todo[unterhofer] This isn't the only way to go into full screen mode. We will probably need to move this into JBR to hook into windowWillEnterFullScreen
            [self performSelectorOnMainThread:@selector(resetHeaderAndEnableFullscreen) withObject:nil waitUntilDone:NO];
        }
        else
        {
            [self.window performSelectorOnMainThread:@selector(toggleFullScreen:) withObject:nil waitUntilDone:NO];
        }
    }
    else if (!value && self.isFullScreen)
    {
        if (_titlebarDisabled) {
            // todo[unterhofer] This isn't the only way to go into full screen mode. We will probably need to move this into JBR to hook into windowWillEnterFullScreen
            [self performSelectorOnMainThread:@selector(disableFullScreenAndSetupCustomHeader) withObject:nil waitUntilDone:NO];
        }
        else
        {
            [self.window performSelectorOnMainThread:@selector(toggleFullScreen:) withObject:nil waitUntilDone:NO];
        }
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

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxDisableTitleBar(JNIEnv *env, jobject properties, jobject component, jfloat customHeaderHeight)
{
    LayerHandler *layer = findByObject(env, component);
    if (layer != NULL)
    {
        [layer disableTitlebar:((CGFloat) customHeaderHeight)];
    }
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemTheme_1jvmKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
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