#ifdef SK_METAL

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

@end

// Forward declarations for dependent implementation files not to depend on non-relevant types header inclusion.
@class NSWindow;
@class CALayer;
@protocol MTLDevice;
@protocol MTLCommandQueue;
@protocol CAMetalDrawable;

@interface MetalDevice : NSObject

@property (weak) CALayer *container;
@property (weak) NSWindow *window;
@property (retain, strong) AWTMetalLayer *layer;
@property (retain, strong) id<MTLDevice> adapter;
@property (retain, strong) id<MTLCommandQueue> queue;
@property (retain, strong) id<CAMetalDrawable> drawableHandle;

@end

#endif