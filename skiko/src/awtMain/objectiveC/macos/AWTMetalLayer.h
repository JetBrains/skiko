#import <QuartzCore/CAMetalLayer.h>
#import <jni.h>

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

@end