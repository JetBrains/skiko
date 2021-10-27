#import "jawt.h"
#import "jawt_md.h"
#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>

extern "C"
{

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_NativeApplicationKt_getApplicationWindowCount(JNIEnv *env, jobject obj)
{
    @autoreleasepool {
        return [[[NSApplication sharedApplication] windows] count];
    }
}

}