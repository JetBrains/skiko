#ifdef SK_METAL

#ifndef SK_METAL_DEVICE_H
#define SK_METAL_DEVICE_H

#import <QuartzCore/QuartzCore.h>
#import <AppKit/AppKit.h>
#import <Metal/Metal.h>

#import "AWTMetalLayer.h"

@interface MetalDevice : NSObject

@property (weak) CALayer *container;
@property (strong) NSWindow *window;
@property (strong) AWTMetalLayer *layer;
@property (strong) id<MTLDevice> adapter;
@property (strong) id<MTLCommandQueue> queue;
@property (strong) id<CAMetalDrawable> drawableHandle;

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window env:(JNIEnv *)env redrawer:(jobject)redrawer callbacks:(jobject)callbacks;
- (void)disposeWithEnv:(JNIEnv *)env;
- (void)recreateDisplayLinkIfNeededWithEnv:(JNIEnv *)env;
- (void)handleDisplayLinkFired;
- (void)signalFrameCompletionWithEnv:(JNIEnv *)env;

@end

#endif // SK_AWT_METAL_DEVICE_H

#endif // SK_METAL