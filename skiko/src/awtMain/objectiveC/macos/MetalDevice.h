#ifdef SK_METAL

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

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window env:(JNIEnv *)env displayLinkCallback:(jobject)displayLinkCallback;
- (void)recreateDisplayLinkIfNeeded;
- (void)handleDisplayLinkFired;

@end

#endif