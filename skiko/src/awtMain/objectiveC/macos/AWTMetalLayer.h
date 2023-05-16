#ifdef SK_METAL

#ifndef SK_AWT_METAL_LAYER_H
#define SK_AWT_METAL_LAYER_H

#import <QuartzCore/CAMetalLayer.h>
#import <jni.h>

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

@end

#endif // SK_AWT_METAL_LAYER_H

#endif // SK_METAL