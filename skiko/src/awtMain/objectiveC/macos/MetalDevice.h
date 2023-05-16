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

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window;
- (void)recreateDisplayLinkIfNeeded;
- (void)handleDisplayLinkFired;
- (void)waitUntilVsync;
- (void)waitForQueueSlot;
- (void)freeQueueSlot;

@end

#endif