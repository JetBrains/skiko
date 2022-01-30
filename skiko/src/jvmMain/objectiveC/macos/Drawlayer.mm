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
@property (weak) NSWindow *window;

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

- (void) setUpCustomHeader
{
    /**
     * The view hierarchy looks as follows:
     * NSThemeFrame
     * ├─NSView (content view)
     * └─NSTitlebarContainerView
     *   ├─NSTitlebarView
     *   │ ├─NSVisualEffectView (only on Big Sur and newer)
     *   │ ├─NSView (only on Big Sur and newer)
     *   │ ├─_NSThemeCloseWidget - Close
     *   │ ├─_NSThemeZoomWidget - Full Screen
     *   │ └─_NSThemeWidget - Minimize (note the different order compared to their layout)
     *   └─_NSTitlebarDecorationView
     */
    NSView* themeFrame = self.window.contentView.superview;
    NSView* titlebarContainer = [self.window standardWindowButton:NSWindowCloseButton].superview.superview;
    NSView* titlebar = titlebarContainer.subviews[0];
    NSView* titlebarDecoration = titlebarContainer.subviews[1];

    // The following two views are only there on Big Sur and forward
    BOOL runningAtLeastBigSur = [NSProcessInfo.processInfo isOperatingSystemAtLeastVersion:{ .majorVersion = 11, .minorVersion = 0, .patchVersion = 0 }];
    NSView* titlebarVisualEffect = runningAtLeastBigSur ? titlebar.subviews[0] : nil;
    NSView* titlebarBackground = runningAtLeastBigSur ? titlebar.subviews[1] : nil;

    NSView* dragger = [[WindowDragView alloc] init];
    [titlebar addSubview:dragger];

    NSMutableArray* newConstraints = [[NSMutableArray alloc] init];

    titlebar.translatesAutoresizingMaskIntoConstraints = NO;
    [newConstraints addObjectsFromArray:@[
        [titlebar.leftAnchor constraintEqualToAnchor:themeFrame.leftAnchor],
        [titlebar.widthAnchor constraintEqualToAnchor:themeFrame.widthAnchor],
        [titlebar.topAnchor constraintEqualToAnchor:themeFrame.topAnchor],
        [titlebar.heightAnchor constraintEqualToConstant:_customHeaderHeight], // This is the important one
    ]];

    NSArray* viewsToChange = runningAtLeastBigSur
        ? @[titlebarContainer, titlebarDecoration, titlebarVisualEffect, titlebarBackground, dragger]
        : @[titlebarContainer, titlebarDecoration, dragger];
    for (NSView* view in viewsToChange)
    {
        view.translatesAutoresizingMaskIntoConstraints = NO;
        [newConstraints addObjectsFromArray:@[
            [view.leftAnchor constraintEqualToAnchor:titlebar.leftAnchor],
            [view.rightAnchor constraintEqualToAnchor:titlebar.rightAnchor],
            [view.topAnchor constraintEqualToAnchor:titlebar.topAnchor],
            [view.bottomAnchor constraintEqualToAnchor:titlebar.bottomAnchor],
        ]];
    }

    // In some scenarios, we still have an `NSTextField` inside the `NSTitlebar` containing the window title, even
    // though we called `setTitleVisibility` with `NSWindowTitleHidden`. We need to make it zero size because otherwise,
    // it would swallow click events.
    NSUInteger potentialTitleIndex = runningAtLeastBigSur ? 2 : 0;
    NSTextField* title =
        [titlebar.subviews[potentialTitleIndex] isKindOfClass:[NSTextField class]]
            ? titlebar.subviews[potentialTitleIndex]
            : nil;
    if (title != nil)
    {
        title.translatesAutoresizingMaskIntoConstraints = NO;
        [newConstraints addObjectsFromArray:@[
            [title.heightAnchor constraintEqualToConstant:0],
            [title.widthAnchor constraintEqualToConstant:0],
        ]];
    }

    NSView* closeButtonView = [self.window standardWindowButton:NSWindowCloseButton];
    NSView* miniaturizeButtonView = [self.window standardWindowButton:NSWindowMiniaturizeButton];
    NSView* zoomButtonView = [self.window standardWindowButton:NSWindowZoomButton];
    CGFloat horizontalButtonOffset = 20.0;

    [@[closeButtonView, miniaturizeButtonView, zoomButtonView] enumerateObjectsUsingBlock:^(NSView* button, NSUInteger i, BOOL* stop)
    {
        button.translatesAutoresizingMaskIntoConstraints = NO;
        [newConstraints addObjectsFromArray:@[
            [button.centerYAnchor constraintEqualToAnchor:titlebar.centerYAnchor],
            [button.centerXAnchor constraintEqualToAnchor:titlebar.leftAnchor constant:(_customHeaderHeight/2.0 + (i * horizontalButtonOffset))],
        ]];
    }];

    [NSLayoutConstraint activateConstraints:newConstraints];
}

- (void) resetHeader
{
    NSView* themeFrame = self.window.contentView.superview;
    NSView* titlebarContainer = [self.window standardWindowButton:NSWindowCloseButton].superview.superview;
    NSView* titlebar = titlebarContainer.subviews[0];
    NSView* titlebarDecoration = titlebarContainer.subviews[1];

    // The following two views are only there on Big Sur and forward
    BOOL runningAtLeastBigSur = [NSProcessInfo.processInfo isOperatingSystemAtLeastVersion:{ .majorVersion = 11, .minorVersion = 0, .patchVersion = 0 }];
    NSView* titlebarVisualEffect = runningAtLeastBigSur ? titlebar.subviews[0] : nil;
    NSView* titlebarBackground = runningAtLeastBigSur ? titlebar.subviews[1] : nil;

    NSView* closeButtonView = [self.window standardWindowButton:NSWindowCloseButton];
    NSView* miniaturizeButtonView = [self.window standardWindowButton:NSWindowMiniaturizeButton];
    NSView* zoomButtonView = [self.window standardWindowButton:NSWindowZoomButton];

    NSArray* changedViews = runningAtLeastBigSur
        ? @[titlebarContainer, titlebarDecoration, titlebar, titlebarVisualEffect, titlebarBackground, closeButtonView, miniaturizeButtonView, zoomButtonView]
        : @[titlebarContainer, titlebarDecoration, titlebar, closeButtonView, miniaturizeButtonView, zoomButtonView];
    for (NSView* changedView in changedViews)
    {
        [NSLayoutConstraint deactivateConstraints:changedView.constraints];
        changedView.translatesAutoresizingMaskIntoConstraints = YES;
    }

    NSUInteger potentialTitleIndex = runningAtLeastBigSur ? 2 : 0;
    NSTextField* title =
        [titlebar.subviews[potentialTitleIndex] isKindOfClass:[NSTextField class]]
            ? titlebar.subviews[potentialTitleIndex]
            : nil;
    if (title != nil)
    {
        [NSLayoutConstraint deactivateConstraints:title.constraints];
        title.translatesAutoresizingMaskIntoConstraints = YES;
    }

    NSUInteger draggerIndex =
        [titlebar.subviews indexOfObjectPassingTest:^(NSView* subview, NSUInteger index, BOOL* stop)
        {
            return [subview isKindOfClass:[WindowDragView class]];
        }];
    if (draggerIndex != NSNotFound) {
        WindowDragView* dragger = titlebar.subviews[draggerIndex];
        [dragger removeFromSuperview];
    }
}

- (void) setWindowControlsHidden: (BOOL) hidden
{
    [self.window standardWindowButton:NSWindowCloseButton].superview.hidden = hidden;
}

- (void) disableTitlebar: (CGFloat) customHeaderHeight
{
    _customHeaderHeight = customHeaderHeight;
    dispatch_sync(dispatch_get_main_queue(), ^{
        [self.window setTitlebarAppearsTransparent:YES];
        [self.window setTitleVisibility:NSWindowTitleHidden];
        [self.window setStyleMask:[self.window styleMask]|NSWindowStyleMaskFullSizeContentView];

        if (!self.isFullScreen) {
            [self setUpCustomHeader];
        }
    });
    NSNotificationCenter* defaultCenter = [NSNotificationCenter defaultCenter];
    NSOperationQueue* mainQueue = [NSOperationQueue mainQueue];
    [defaultCenter addObserverForName:NSWindowWillEnterFullScreenNotification object:self.window queue:mainQueue usingBlock:^(NSNotification* notification) {
        [self resetHeader];
    }];
    [defaultCenter addObserverForName:NSWindowWillExitFullScreenNotification object:self.window queue:mainQueue usingBlock:^(NSNotification* notification) {
        [self setWindowControlsHidden:YES];
    }];
    [defaultCenter addObserverForName:NSWindowDidExitFullScreenNotification object:self.window queue:mainQueue usingBlock:^(NSNotification* notification) {
        [self setUpCustomHeader];
        [self setWindowControlsHidden:NO];
    }];
    _titlebarDisabled = YES;
}

- (BOOL) isFullScreen
{
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
    NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) ((void*)platformInfoPtr);
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
    @autoreleasepool {
        if (layerStorage == nil)
        {
            layerStorage = [[NSMutableSet alloc] init];
        }

        LayerHandler *layer = [[LayerHandler alloc] init];
        NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) (void*) platformInfoPtr;
        layer.container = [dsi_mac windowLayer];
        jobject canvasGlobalRef = env->NewGlobalRef(canvas);
        [layer setCanvasGlobalRef: canvasGlobalRef];
        layer.window = findWindow(platformInfoPtr);

        [layerStorage addObject: layer];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeDispose(JNIEnv *env, jobject canvas)
{
    @autoreleasepool {
        LayerHandler *layer = findByObject(env, canvas);
        if (layer != NULL)
        {
            [layerStorage removeObject: layer];
            env->DeleteGlobalRef(layer.canvasGlobalRef);
        }
    }
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxIsFullscreenNative(JNIEnv *env, jobject properties, jobject component)
{
    @autoreleasepool {
        LayerHandler *layer = findByObject(env, component);
        if (layer != NULL)
        {
            return [layer isFullScreen];
        }
        return false;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxSetFullscreenNative(JNIEnv *env, jobject properties, jobject component, jboolean value)
{
    @autoreleasepool {
        LayerHandler *layer = findByObject(env, component);
        if (layer != NULL)
        {
            [layer makeFullscreen:value];
        }
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject component, jlong platformInfoPtr)
{
    @autoreleasepool {
        LayerHandler *layer = findByObject(env, component);
        return (jlong) (__bridge void*) layer.window;
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentHandle(JNIEnv *env, jobject component, jlong platformInfoPtr)
{
    @autoreleasepool {
        LayerHandler *layer = findByObject(env, component);
        return (jlong) (__bridge void*) layer.window;
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxDisableTitleBar(JNIEnv *env, jobject properties, jobject component, jfloat customHeaderHeight)
{
    @autoreleasepool {
        LayerHandler *layer = findByObject(env, component);
        if (layer != NULL)
        {
            [layer disableTitlebar:((CGFloat) customHeaderHeight)];
        }
    }
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemTheme_1awtKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
{
    @autoreleasepool {
        NSString *osxMode = [[NSUserDefaults standardUserDefaults] stringForKey:@"AppleInterfaceStyle"];
        if ([@"Dark" isEqualToString:osxMode]) {
            // Dark.
            return 1;
        } else {
            // Light.
            return 0;
        }
    }
}

void getMetalDeviceAndQueue(void** device, void** queue)
{
    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];
    *device = (__bridge void*) fDevice;
    *queue = (__bridge void*) fQueue;
}

} // extern C