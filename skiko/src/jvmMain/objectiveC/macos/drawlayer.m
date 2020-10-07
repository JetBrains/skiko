#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#define kNullWindowHandle NULL

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <OpenGL/gl3.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

JavaVM *jvm = NULL;

@interface AWTGLLayer : CAOpenGLLayer

@property jobject windowRef;

@end

@implementation AWTGLLayer

jobject windowRef;

- (id)init
{
    self = [super init];

    if (self)
    {
        [self removeAllAnimations];
        [self setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [self setNeedsDisplayOnBoundsChange: YES];

        self.windowRef = NULL;
    }

    return self;
}

-(void)drawInCGLContext:(CGLContextObj)ctx 
            pixelFormat:(CGLPixelFormatObj)pf 
            forLayerTime:(CFTimeInterval)t 
            displayTime:(const CVTimeStamp *)ts
{
    CGLSetCurrentContext(ctx);

    if (jvm != NULL) {
        JNIEnv *env;
        (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);

        static jclass wndClass = NULL;
        if (!wndClass) wndClass = (*env)->GetObjectClass(env, self.windowRef);
        static jmethodID drawMethod = NULL;
        if (!drawMethod) drawMethod = (*env)->GetMethodID(env, wndClass, "draw", "()V");
        if (NULL == drawMethod) {
            NSLog(@"The method Window.draw() not found!");
            return;
        }
        (*env)->CallVoidMethod(env, self.windowRef, drawMethod);
    }

    [super drawInCGLContext:ctx pixelFormat:pf forLayerTime:t displayTime:ts];
}

- (void) dispose
{
    self.windowRef = NULL;
}

@end

@interface LayersSet : NSObject

@property jobject windowRef;
@property (retain, strong) CALayer *caLayer;
@property (retain, strong) AWTGLLayer *glLayer;
@property (retain, strong) NSWindow *window;

@end

@implementation LayersSet

jobject windowRef;
CALayer *caLayer;
AWTGLLayer *glLayer;

- (id) init
{
    self = [super init];

    if (self)
    {
        self.windowRef = NULL;
        self.caLayer = NULL;
        self.glLayer = NULL;
        self.window = NULL;
    }

    return self;
}

- (void) syncSize
{
    float scaleFactor = [[self.window screen] backingScaleFactor];
    self.caLayer.contentsScale = scaleFactor;
    self.glLayer.contentsScale = scaleFactor;
    self.glLayer.bounds = self.caLayer.bounds;
    self.glLayer.frame = self.caLayer.frame;
}

- (void) update
{
    [self.glLayer performSelectorOnMainThread:@selector(setNeedsDisplay) withObject:0 waitUntilDone:NO];
}

- (void) dispose
{
    [self.glLayer dispose];
    self.glLayer = NULL;
    self.caLayer = NULL;
    self.window = NULL;
}

@end

NSMutableArray *unknownWindows = nil;
NSMutableSet *windowsSet = nil;
LayersSet * findByObject(JNIEnv *env, jobject object) {
    for (LayersSet* value in windowsSet) {
        if ((*env)->IsSameObject(env, object, value.windowRef) == JNI_TRUE) {
            return value;
        }
    }

    return NULL;
}

extern jboolean Skiko_GetAWT(JNIEnv* env, JAWT* awt);

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_updateLayer(JNIEnv *env, jobject window)
{
    if (windowsSet != nil) {
        LayersSet *layer = findByObject(env, window);
        if (layer != NULL) {
            if (layer.caLayer == NULL && layer.glLayer == NULL) {
                (*env)->DeleteGlobalRef(env, layer.windowRef);
                layer.windowRef = NULL;
                [windowsSet removeObject: layer];
                return;
            }
            [layer syncSize];
            return;
        }
    } else {
        windowsSet = [[NSMutableSet alloc] init];
    }

    JAWT awt;
    JAWT_DrawingSurface *ds = NULL;
    JAWT_DrawingSurfaceInfo *dsi = NULL;
    CGLPixelFormatObj pixFormatObj = NULL;
    CGLContextObj context;

    jboolean result = JNI_FALSE;
    jint lock = 0;
    NSObject<JAWT_SurfaceLayers>* dsi_mac = NULL;

    awt.version = JAWT_VERSION_9 /* | JAWT_MACOSX_USE_CALAYER */;
    result = Skiko_GetAWT(env, &awt);
    assert(result != JNI_FALSE);

    (*env)->GetJavaVM(env, &jvm);

    ds = awt.GetDrawingSurface(env, window);
    assert(ds != NULL);

    lock = ds->Lock(ds);
    assert((lock & JAWT_LOCK_ERROR) == 0);

    dsi = ds->GetDrawingSurfaceInfo(ds);

    if (dsi != NULL)
    {
        dsi_mac = ( __bridge NSObject<JAWT_SurfaceLayers> *) dsi->platformInfo;

        LayersSet *layersSet = [[LayersSet alloc] init];
        [windowsSet addObject: layersSet];

        NSMutableArray<NSWindow *> *windows = [NSMutableArray arrayWithArray: [[NSApplication sharedApplication] windows]];
        if ([windowsSet count] == 1)
        {
            NSWindow *mainWindow = [[NSApplication sharedApplication] mainWindow];
            layersSet.window = mainWindow;
            [windows removeObject: mainWindow];
            unknownWindows = windows;
        }
        else
        {
            for (NSWindow* value in unknownWindows) {
                [windows removeObject: value];
            }
            for (LayersSet* value in windowsSet) {
                [windows removeObject: value.window];
            }
            layersSet.window = [windows firstObject];
        }

        layersSet.caLayer = [dsi_mac windowLayer];
        [layersSet.caLayer removeAllAnimations];
        [layersSet.caLayer setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [layersSet.caLayer setNeedsDisplayOnBoundsChange: YES];

        layersSet.glLayer = [AWTGLLayer new];
        [layersSet.caLayer addSublayer: layersSet.glLayer];
        CGFloat white[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        layersSet.glLayer.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), white);
        
        jobject windowRef = (*env)->NewGlobalRef(env, window);

        [layersSet.glLayer setWindowRef: windowRef];
        [layersSet setWindowRef: windowRef];
        [layersSet syncSize];
    }

    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_redrawLayer(JNIEnv *env, jobject window) {
    LayersSet *layer = findByObject(env, window);
    if (layer != NULL) {
        [layer update];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_disposeLayer(JNIEnv *env, jobject window) {
    LayersSet *layer = findByObject(env, window);
    if (layer != NULL) {
        [layer dispose];
    }
}

JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentScale(JNIEnv *env, jobject window) {
    LayersSet *layer = findByObject(env, window);
    if (layer != NULL) {
        return layer.caLayer.contentsScale;
    }
    return 1.0f;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject window) {
    return (jlong)kNullWindowHandle;
}

void getMetalDeviceAndQueue(void** device, void** queue) {
    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];
    *device = (__bridge void*)fDevice;
    *queue = (__bridge void*)fQueue;
}