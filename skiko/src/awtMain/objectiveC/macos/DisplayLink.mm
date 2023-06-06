#ifdef SK_METAL

#import <jawt.h>
#import <jawt_md.h>

#include "skiko/interop.hh"

#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>

#import <GrBackendSurface.h>
#import <GrDirectContext.h>
#import <mtl/GrMtlBackendContext.h>
#import <mtl/GrMtlTypes.h>

static CVReturn MetalDeviceDisplayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *now, const CVTimeStamp *outputTime, CVOptionFlags flagsIn, CVOptionFlags *flagsOut, void *displayLinkContext) {
    jobject obj = (jobject) displayLinkContext;

    [device handleDisplayLinkFired];

    return kCVReturnSuccess;
}

extern "C"
{

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_macos_DisplayLink_nativeInit(JNIEnv *env, jobject obj, jlong screenPtr) {
    @autoreleasepool {
        NSScreen* screen = (__bridge NSScreen*) (void *) screenPtr;

        NSDictionary* screenDescription = [screen deviceDescription];
        NSNumber* screenID = [screenDescription objectForKey:@"NSScreenNumber"];

        CVDisplayLinkRef displayLink;
        CVReturn result;

        result = CVDisplayLinkCreateWithCGDisplay([screenID unsignedIntValue], &displayLink);

        if (result != kCVReturnSuccess) {
            return 0;
        }

        result = CVDisplayLinkSetOutputCallback(displayLink, &MetalDeviceDisplayLinkCallback, (__bridge void *)(obj));

        if (result != kCVReturnSuccess) {
            CVDisplayLinkRelease(displayLink);
            return 0;
        }

        result = CVDisplayLinkStart(displayLink);

        if (result != kCVReturnSuccess) {
            CVDisplayLinkRelease(displayLink);
            return 0;
        }

        return (jlong) (__bridge_retained void *) displayLink;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_redrawer_DisplayLink_dispose(JNIEnv *env, jobject obj, jlong displayLinkPtr) {
    @autoreleasepool {
        CVDisplayLinkRef displayLink = (__bridge_transfer CVDisplayLinkRef) (void *) displayLinkPtr;
        CVDisplayLinkStop(displayLink);
        CVDisplayLinkRelease(displayLink);
    }
}

} // extern C
#endif
