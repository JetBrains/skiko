#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#import <Cocoa/Cocoa.h>
#import <CoreGraphics/CGWindow.h>
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
    if (platformInfoPtr == 0) return nil;
    NSObject<JAWT_SurfaceLayers>* dsi_mac = (NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;
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
    NSObject<JAWT_SurfaceLayers>* dsi_mac = (NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;
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

bool cfStringEqual(CFStringRef notification, CFStringRef notifType) {
    return CFStringCompare(notification, notifType, 0) == 0;
}

struct ContextData {
    NSWindow* window;
    int x_relative;
    int y_relative;
    int width;
    int height;
};

void AnAXObserverCallback(AXObserverRef observer, AXUIElementRef element,
                          CFStringRef notificationName, void* contextData)
{
    fprintf(stderr, "AnAXObserverCallback: %s\n", CFStringGetCStringPtr(notificationName, kCFStringEncodingMacRoman));
    ContextData* context = (ContextData*)contextData;
    NSWindow* window = context->window;
    //CFArrayRef attrNames = NULL;
    //AXUIElementCopyAttributeNames(element, &attrNames);
    //NSArray* attrs = (NSArray*)(attrNames);
    //NSLog(@"%@", attrs);
    AXValueRef positionValue = NULL;
    AXUIElementCopyAttributeValue(element, CFSTR("AXPosition"), (CFTypeRef *)&positionValue);
    CGPoint position;
    AXValueGetValue(positionValue, (AXValueType)kAXValueCGPointType, &position);

    AXValueRef sizeValue = NULL;
    AXUIElementCopyAttributeValue(element, CFSTR("AXSize"), (CFTypeRef*)&sizeValue);
    CGSize size;
    AXValueGetValue(sizeValue, (AXValueType)kAXValueCGSizeType, &size);

    CFRelease(positionValue);
    //CFRelease(sizeValue);

    //fprintf(stderr, "we are at (%f, %f): size is (%f, %f)\n", position.x, position.y, size.width, size.height);

    if (cfStringEqual(notificationName, kAXWindowResizedNotification)) {
        fprintf(stderr, "resize %p\n", element);
    } else if (cfStringEqual(notificationName, kAXWindowMovedNotification)) {
        fprintf(stderr, "move %p\n", element);

        NSRect screenSize = [[NSScreen mainScreen] frame];
        /*NSRect newFrame = NSMakeRect(
            position.x + context->x_relative,
            screenSize.size.height - (position.y - context->y_relative) - size.height,
            context->width,
            context->height);
        [window setFrame:newFrame display:YES animate:NO];
        */
        NSPoint topLeft = NSMakePoint(
            position.x + context->x_relative,
            screenSize.size.height - (position.y + context->y_relative)
        );
        [window setFrameTopLeftPoint:topLeft];
    }
}

jboolean listenForChanges(jlong parentPid, jlong platformInfoPtr,
                          int x_relative, int y_relative, int width, int height) {
    AXUIElementRef app = AXUIElementCreateApplication(parentPid);
    if (!app) {
        fprintf(stderr, "AXUIElementCreateApplication failed\n");
        return JNI_FALSE;
    }
    AXObserverRef observer = NULL;
    AXError err = AXObserverCreate(parentPid, AnAXObserverCallback, &observer);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverCreate failed: %d\n", err);
        return JNI_FALSE;
    }
    NSWindow* me = findWindow(platformInfoPtr);
    ContextData* data = new ContextData();
    data->window = me;
    data->x_relative = x_relative;
    data->y_relative = y_relative;
    data->width = width;
    data->height = height;
    err = AXObserverAddNotification(observer, app, kAXWindowCreatedNotification, data);
    if (err != kAXErrorSuccess) {
        if (err == kAXErrorAPIDisabled) {
            fprintf(stderr, "ERROR: enable assistive API in preferences!!!\n");
        } else {
            fprintf(stderr, "AXObserverAddNotification failed: %d\n", err);
        }
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXWindowMovedNotification, data);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverAddNotification failed: %d\n", err);
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXWindowResizedNotification, data);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverAddNotification failed: %d\n", err);
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXWindowMiniaturizedNotification, data);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverAddNotification failed: %d\n", err);
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXFocusedWindowChangedNotification, data);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverAddNotification failed: %d\n", err);
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXWindowDeminiaturizedNotification, data);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverAddNotification failed\n");
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXApplicationHiddenNotification, data);
    if (err != kAXErrorSuccess) {
        fprintf(stderr, "AXObserverAddNotification failed\n");
        return JNI_FALSE;
    }
    err = AXObserverAddNotification(observer, app, kAXApplicationShownNotification, data);
    if (err != kAXErrorSuccess) {
       fprintf(stderr, "AXObserverAddNotification failed: %d\n", err);
       return JNI_FALSE;
    }
    CFRunLoopAddSource([[NSRunLoop currentRunLoop] getCFRunLoop],
                        AXObserverGetRunLoopSource(observer),
                        kCFRunLoopDefaultMode);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxReparentTo(
    JNIEnv *env, jobject fileObject, jlong platformInfoPtr, jlong parentPid, jlong parentWinId,
    jint xRelative, jint yRelative, jint width, jint height)
{
    fprintf(stderr, "Java_org_jetbrains_skiko_PlatformOperationsKt_osxReparentTo %lld %lld\n",
        parentPid, parentWinId);
    jboolean __block result = JNI_FALSE;
    dispatch_sync(dispatch_get_main_queue(), ^{
        NSDictionary *options = @{(id)kAXTrustedCheckOptionPrompt: @YES};
        BOOL accessibilityEnabled = AXIsProcessTrustedWithOptions((CFDictionaryRef)options);
        result = listenForChanges(parentPid, platformInfoPtr, xRelative, yRelative, width, height);
    });
    return result;
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

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_ConvertorsKt_getWindowNumber(JNIEnv *env, jobject fileLevel, jlong platformInfoPtr)
{
    NSWindow* window = findWindow(platformInfoPtr);
    return window != nil ? (jlong)[window windowNumber] : 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxDisableTitleBar(JNIEnv *env, jobject properties, jlong platformInfoPtr)
{
    NSWindow* window = findWindow(platformInfoPtr);
    if (window == nil) return;
    dispatch_async(dispatch_get_main_queue(), ^{
        [window setTitlebarAppearsTransparent:YES];
        [window setTitleVisibility:NSWindowTitleHidden];
        [window setStyleMask:[window styleMask]|NSWindowStyleMaskFullSizeContentView];
        // always show `fullscreen` green traffic light button instead of `maximize/zoom` button
        [window setCollectionBehavior:[window collectionBehavior]|NSWindowCollectionBehaviorFullScreenPrimary];
        [window setMovable:NO];
    });
}

void getMetalDeviceAndQueue(void** device, void** queue)
{
    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];
    *device = (__bridge void*)fDevice;
    *queue = (__bridge void*)fQueue;
}

} // extern C