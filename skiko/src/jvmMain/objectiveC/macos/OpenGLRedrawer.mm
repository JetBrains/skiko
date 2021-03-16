#import "jawt.h"
#import "jawt_md.h"

#define GL_SILENCE_DEPRECATION

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <OpenGL/gl3.h>

JavaVM *jvm = NULL;

@interface AWTGLLayer : CAOpenGLLayer

@property jobject javaRef;

@end

@implementation AWTGLLayer

- (id)init
{
    self = [super init];

    assert(self != NULL);

    [self removeAllAnimations];
    [self setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];

    return self;
}

-(BOOL)canDrawInCGLContext:(CGLContextObj)ctx
            pixelFormat:(CGLPixelFormatObj)pf
            forLayerTime:(CFTimeInterval)t
            displayTime:(const CVTimeStamp *)ts
{
    assert(jvm != NULL);
    JNIEnv *env;
    jvm->AttachCurrentThread((void **)&env, NULL);

    static jclass cls = NULL;
    static jmethodID method = NULL;
    if (!cls) cls = env->GetObjectClass(self.javaRef);
    if (!method) method = env->GetMethodID(cls, "canDraw", "()Z");
    return env->CallBooleanMethod(self.javaRef, method);
}

-(void)drawInCGLContext:(CGLContextObj)ctx
            pixelFormat:(CGLPixelFormatObj)pf
            forLayerTime:(CFTimeInterval)t
            displayTime:(const CVTimeStamp *)ts
{
    CGLSetCurrentContext(ctx);

    assert(jvm != NULL);
    JNIEnv *env;
    jvm->AttachCurrentThread((void **)&env, NULL);

    static jclass cls = NULL;
    static jmethodID method = NULL;
    if (!cls) cls = env->GetObjectClass(self.javaRef);
    if (!method) method = env->GetMethodID(cls, "performDraw", "()V");
    env->CallVoidMethod(self.javaRef, method);

    [super drawInCGLContext:ctx pixelFormat:pf forLayerTime:t displayTime:ts];
}

@end

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MacOsOpenGLRedrawerKt_initContainer(JNIEnv *env, jobject redrawer, jlong platformInfoPtr)
{
    env->GetJavaVM(&jvm);

    NSObject<JAWT_SurfaceLayers>* dsi_mac = (__bridge NSObject<JAWT_SurfaceLayers> *) platformInfoPtr;

    CALayer *container = [dsi_mac windowLayer];
    [container removeAllAnimations];
    [container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
    [container setNeedsDisplayOnBoundsChange: YES];

    return (jlong) container;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MacOsOpenGLRedrawerKt_setContentScale(JNIEnv *env, jobject obj, jlong layerPtr, jfloat contentScale)
{
    CALayer *layer = (CALayer *) layerPtr;
    assert(contentScale != 0);
    layer.contentsScale = contentScale;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MacOsOpenGLRedrawerKt_initAWTGLLayer(JNIEnv *env, jobject obj, jlong containerPtr, jobject layer, jboolean setNeedsDisplayOnBoundsChange)
{
    CALayer *container = (CALayer *) containerPtr;

    AWTGLLayer *glLayer = [AWTGLLayer new];
    glLayer.javaRef = env->NewGlobalRef(layer);
    [glLayer setNeedsDisplayOnBoundsChange: setNeedsDisplayOnBoundsChange];
    [container addSublayer: glLayer];

    return (jlong) glLayer;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MacOsOpenGLRedrawerKt_disposeAWTGLLayer(JNIEnv *env, jobject obj, jlong ptr)
{
    AWTGLLayer *glLayer = (AWTGLLayer *) ptr;
    [glLayer removeFromSuperlayer];
    env->DeleteGlobalRef(glLayer.javaRef);
    [glLayer release];
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_redrawer_AWTGLLayer_isAsynchronous(JNIEnv *env, jobject obj, jlong ptr)
{
    AWTGLLayer *glLayer = (AWTGLLayer *) ptr;
    return glLayer.isAsynchronous;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AWTGLLayer_setAsynchronous(JNIEnv *env, jobject obj, jlong ptr, jboolean isAsynchronous)
{
    AWTGLLayer *glLayer = (AWTGLLayer *) ptr;
    [glLayer setAsynchronous: isAsynchronous];
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AWTGLLayer_setNeedsDisplayOnMainThread(JNIEnv *env, jobject obj, jlong ptr)
{
    AWTGLLayer *glLayer = (AWTGLLayer *) ptr;
    [glLayer performSelectorOnMainThread:@selector(setNeedsDisplay) withObject:0 waitUntilDone:NO];
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_AWTGLLayer_setFrame(JNIEnv *env, jobject obj, jlong containerPtr, jlong ptr, jfloat x, jfloat y, jfloat width, jfloat height)
{
    CALayer *container = (AWTGLLayer *) containerPtr;
    AWTGLLayer *glLayer = (AWTGLLayer *) ptr;

    y = (int)container.frame.size.height - y - height;

    [CATransaction begin];
    [CATransaction setValue:(id)kCFBooleanTrue
                   forKey:kCATransactionDisableActions]; // disable animations
    glLayer.frame = CGRectMake(x, y, width, height);
    [CATransaction commit];
}

} // extern C