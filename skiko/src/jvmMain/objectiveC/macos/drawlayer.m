#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <OpenGL/gl3.h>

@interface AWTGLLayer : CAOpenGLLayer

@property jobject windowRef;
@property JavaVM *jvm;
@property bool isDrawing;

@end

@implementation AWTGLLayer

jobject windowRef;
JavaVM *jvm;

- (id)init
{
    self = [super init];

    if (self)
    {
        self.needsDisplayOnBoundsChange = YES;
        self.asynchronous = NO;
        self.windowRef = NULL;
        self.jvm = NULL;
        self.isDrawing = NO;
    }

    return self;
}

-(void)drawInCGLContext:(CGLContextObj)ctx 
            pixelFormat:(CGLPixelFormatObj)pf 
            forLayerTime:(CFTimeInterval)t 
            displayTime:(const CVTimeStamp *)ts
{
    if (self.isDrawing) return;
    self.isDrawing = YES;
    CGLSetCurrentContext(ctx);

    if (self.jvm != NULL) {
        JNIEnv *env;
        (*self.jvm)->AttachCurrentThread(self.jvm, (void **)&env, NULL);

        jclass wndClass = (*env)->GetObjectClass(env, self.windowRef);
        jmethodID drawMethod = (*env)->GetMethodID(env, wndClass, "draw", "()V");
        if (NULL == drawMethod) {
            NSLog(@"The method Window.draw() not found!");
            return;
        }
        (*env)->CallVoidMethod(env, self.windowRef, drawMethod);
    }

    [super drawInCGLContext:ctx pixelFormat:pf forLayerTime:t displayTime:ts];
    self.isDrawing = NO;
}

- (void) dispose
{
    self.windowRef = NULL;
    self.jvm = NULL;
}

@end

@interface LayersSet : NSObject

@property jobject windowRef;
@property (retain, strong) CALayer *caLayer;
@property (retain, strong) AWTGLLayer *glLayer;

@end

@implementation LayersSet

jobject windowRef;
CALayer *caLayer;
AWTGLLayer *glLayer;

- (id)init
{
    self = [super init];

    if (self)
    {
        self.windowRef = NULL;
        self.caLayer = NULL;
        self.glLayer = NULL;
    }

    return self;
}

- (void) syncSize
{
    self.glLayer.bounds = self.caLayer.bounds;
    self.glLayer.frame = self.caLayer.frame;
}

- (void) update
{
    [self.glLayer performSelectorOnMainThread:@selector(setNeedsDisplay) withObject:0 waitUntilDone:NO];
    [self.glLayer performSelectorOnMainThread:@selector(displayIfNeeded) withObject:0 waitUntilDone:NO];
}

- (void) dispose
{
    [self.glLayer dispose];
    self.glLayer = NULL;
    self.caLayer = NULL;
}

@end

NSMutableSet *windowsSet = nil;
LayersSet * findByObject(JNIEnv *env, jobject object) {
    for (LayersSet* value in windowsSet) {
        if ((*env)->IsSameObject(env, object, value.windowRef) == JNI_TRUE) {
            return value;
        }
    }
    NSLog(@"The set does not contain this window.");
    return NULL;
}

extern jboolean Skiko_GetAWT(JNIEnv* env, JAWT* awt);

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_Components_HardwareLayer_updateLayer(JNIEnv *env, jobject window)
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

    awt.version = JAWT_VERSION_9;
    result = Skiko_GetAWT(env, &awt);
    assert(result != JNI_FALSE);

    JavaVM *jvm;
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

        layersSet.caLayer = [dsi_mac windowLayer];

        NSScreen *screen = [NSScreen mainScreen];
        float scaleFactor = screen.backingScaleFactor;

        layersSet.caLayer.contentsScale = scaleFactor;

        layersSet.glLayer = [AWTGLLayer new];
        [layersSet.caLayer addSublayer: layersSet.glLayer];
        layersSet.glLayer.backgroundColor = CGColorCreateSRGB(1.0f, 1.0f, 1.0f, 1.0f);
        layersSet.glLayer.contentsScale = scaleFactor;
        
        jobject windowRef = (*env)->NewGlobalRef(env, window);

        [layersSet.glLayer setJvm: jvm];
        [layersSet.glLayer setWindowRef: windowRef];
        [layersSet setWindowRef: windowRef];        

        ds->FreeDrawingSurfaceInfo(dsi);

        ds->Unlock(ds);

        awt.FreeDrawingSurface(ds);
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_Components_HardwareLayer_redrawLayer(JNIEnv *env, jobject window) {
    LayersSet *layer = findByObject(env, window);
    if (layer != NULL) {
        [layer update];
    }
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_Components_HardwareLayer_disposeLayer(JNIEnv *env, jobject window) {
    LayersSet *layer = findByObject(env, window);
    if (layer != NULL) {
        [layer dispose];
    }
}

JNIEXPORT jfloat JNICALL Java_org_jetbrains_awthrl_Components_HardwareLayer_getContentScale(JNIEnv *env, jobject window) {
    LayersSet *layer = findByObject(env, window);
    if (layer != NULL) {
        return [layer.glLayer contentsScale];
    }
    return 1.0f;
}

//// Open GL API starts here.
JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glViewport(JNIEnv * env, jobject object, jint x, jint y, jint w, jint h) {
    glViewport(x, y, w, h);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glClearColor(JNIEnv * env, jobject object, jfloat r, jfloat g, jfloat b, jfloat a) {
    glClearColor(r, g, b, a);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glClear(JNIEnv * env, jobject object, jint mask) {
    glClear(mask);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glFinish(JNIEnv * env, jobject object) {
    glFinish();
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glEnable(JNIEnv * env, jobject object, jint cap) {
    glEnable(cap);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glBindTexture(JNIEnv * env, jobject object, jint target, jint texture) {
    glBindTexture(target, texture);
}

JNIEXPORT jint JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glGetIntegerv(JNIEnv * env, jobject object, jint pname) {
    GLint data;
    glGetIntegerv(pname, &data);
    return (jint)data;
}
