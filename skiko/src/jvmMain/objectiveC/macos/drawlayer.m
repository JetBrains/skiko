#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#define kNullWindowHandle NULL

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <OpenGL/gl3.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>
#import <pthread.h>

JavaVM *jvm = NULL;

@interface AWTGLLayer : CAOpenGLLayer

@property jobject canvasGlobalRef;

@end

@implementation AWTGLLayer

- (id)init
{
    self = [super init];

    if (self)
    {
        [self removeAllAnimations];
        [self setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [self setNeedsDisplayOnBoundsChange: YES];

        self.canvasGlobalRef = NULL;
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
        if (!wndClass) wndClass = (*env)->GetObjectClass(env, self.canvasGlobalRef);
        static jmethodID drawMethod = NULL;
        if (!drawMethod) drawMethod = (*env)->GetMethodID(env, wndClass, "draw", "()V");
        if (NULL == drawMethod)
        {
            NSLog(@"The method HardwareLayer.draw() not found!");
            return;
        }
        (*env)->CallVoidMethod(env, self.canvasGlobalRef, drawMethod);
    }

    [super drawInCGLContext:ctx pixelFormat:pf forLayerTime:t displayTime:ts];
}

@end

@interface LayerHandler : NSObject

@property (retain, strong) CALayer *container;
@property (retain, strong) AWTGLLayer *glLayer;

@end

@implementation LayerHandler

- (id) init
{
    self = [super init];

    if (self)
    {
        self.container = NULL;
        self.glLayer = NULL;
    }

    return self;
}

- (void) syncLayersSize
{
    if (jvm != NULL) {
        JNIEnv *env;
        (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
        static jclass wndClass = NULL;
        if (!wndClass)
        {
            wndClass = (*env)->GetObjectClass(env, self.glLayer.canvasGlobalRef);
        }

        // scale factor
        static jmethodID contentScaleMethod = NULL;
        if (!contentScaleMethod)
        {
            contentScaleMethod = (*env)->GetMethodID(env, wndClass, "getContentScale", "()F");
        }
        if (NULL == contentScaleMethod)
        {
            NSLog(@"The method HardwareLayer.getContentScale() not found!");
            return;
        }
        float scaleFactor = (*env)->CallFloatMethod(env, self.glLayer.canvasGlobalRef, contentScaleMethod);
        assert(scaleFactor != 0);
        self.container.contentsScale = scaleFactor;
        self.glLayer.contentsScale = scaleFactor;

        // size & position
        static jmethodID getXMethod = NULL;
        if (!getXMethod)
        {
            getXMethod = (*env)->GetMethodID(env, wndClass, "getAbsoluteX", "()I");
        }
        if (NULL == getXMethod)
        {
            NSLog(@"The method HardwareLayer.getAbsoluteX() not found!");
            return;
        }

        static jmethodID getYMethod = NULL;
        if (!getYMethod)
        {
            getYMethod = (*env)->GetMethodID(env, wndClass, "getAbsoluteY", "()I");
        }
        if (NULL == getYMethod)
        {
            NSLog(@"The method HardwareLayer.getAbsoluteY() not found!");
            return;
        }

        static jmethodID getWidthMethod = NULL;
        if (!getWidthMethod)
        {
            getWidthMethod = (*env)->GetMethodID(env, wndClass, "getWidth", "()I");
        }
        if (NULL == getWidthMethod)
        {
            NSLog(@"The method HardwareLayer.getWidth() not found!");
            return;
        }
        static jmethodID getHeightMethod = NULL;
        if (!getHeightMethod)
        {
            getHeightMethod = (*env)->GetMethodID(env, wndClass, "getHeight", "()I");
        }
        if (NULL == getHeightMethod)
        {
            NSLog(@"The method HardwareLayer.getHeight() not found!");
            return;
        }
        int x = (*env)->CallIntMethod(env, self.glLayer.canvasGlobalRef, getXMethod);
        int y = (*env)->CallIntMethod(env, self.glLayer.canvasGlobalRef, getYMethod);
        int w = (*env)->CallIntMethod(env, self.glLayer.canvasGlobalRef, getWidthMethod);
        int h = (*env)->CallIntMethod(env, self.glLayer.canvasGlobalRef, getHeightMethod);

        y = (int)self.container.frame.size.height - y - h;

        CGRect boundsRect = CGRectMake(x, y, w, h);
        self.glLayer.frame = boundsRect;
    }
}

- (void) updateLayerContent
{
    [self.glLayer performSelectorOnMainThread:@selector(setNeedsDisplay) withObject:0 waitUntilDone:NO];
}

- (void) disposeLayer:(JNIEnv *) env
{
    [self.glLayer removeFromSuperlayer];
    (*env)->DeleteGlobalRef(env, self.glLayer.canvasGlobalRef);
    self.glLayer.canvasGlobalRef = NULL;
    self.glLayer = NULL;
    self.container = NULL;
}

@end

NSMutableSet *layerStorage = nil;
pthread_mutex_t layerStorageMutex = { 0 };

void lockLayers() {
    pthread_mutex_lock(&layerStorageMutex);
}

void unlockLayers() {
    pthread_mutex_unlock(&layerStorageMutex);
}

LayerHandler * findByObject(JNIEnv *env, jobject object)
{
    for (LayerHandler* layer in layerStorage)
    {
        if ((*env)->IsSameObject(env, object, layer.glLayer.canvasGlobalRef) == JNI_TRUE)
        {
            return layer;
        }
    }
    return NULL;
}

extern jboolean Skiko_GetAWT(JNIEnv* env, JAWT* awt);

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_updateLayer(JNIEnv *env, jobject canvas)
{
    lockLayers();
    if (layerStorage != nil)
    {
        LayerHandler *layer = findByObject(env, canvas);
        if (layer != NULL)
        {
            [layer syncLayersSize];
            unlockLayers();
            return;
        }
    }
    else
    {
        layerStorage = [[NSMutableSet alloc] init];
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

    ds = awt.GetDrawingSurface(env, canvas);
    assert(ds != NULL);

    lock = ds->Lock(ds);
    assert((lock & JAWT_LOCK_ERROR) == 0);

    dsi = ds->GetDrawingSurfaceInfo(ds);

    if (dsi != NULL)
    {
        dsi_mac = ( __bridge NSObject<JAWT_SurfaceLayers> *) dsi->platformInfo;

        LayerHandler *layersSet = [[LayerHandler alloc] init];
        lockLayers();
        [layerStorage addObject: layersSet];
        unlockLayers();

        layersSet.container = [dsi_mac windowLayer];
        [layersSet.container removeAllAnimations];
        [layersSet.container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [layersSet.container setNeedsDisplayOnBoundsChange: YES];

        layersSet.glLayer = [AWTGLLayer new];
        [layersSet.container addSublayer: layersSet.glLayer];
        
        jobject canvasGlobalRef = (*env)->NewGlobalRef(env, canvas);

        [layersSet.glLayer setCanvasGlobalRef: canvasGlobalRef];

        [layersSet syncLayersSize];
    }

    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);
    unlockLayers();
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_redrawLayer(JNIEnv *env, jobject canvas)
{
    lockLayers();
    LayerHandler *layer = findByObject(env, canvas);
    unlockLayers();
    if (layer != NULL)
    {
        [layer updateLayerContent];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_disposeLayer(JNIEnv *env, jobject canvas)
{
    lockLayers();
    LayerHandler *layer = findByObject(env, canvas);
    unlockLayers();
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

void getMetalDeviceAndQueue(void** device, void** queue)
{
    id<MTLDevice> fDevice = MTLCreateSystemDefaultDevice();
    id<MTLCommandQueue> fQueue = [fDevice newCommandQueue];
    *device = (__bridge void*)fDevice;
    *queue = (__bridge void*)fQueue;
}