#ifdef SK_METAL

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

@end

@interface MetalDevice : NSObject

@property (weak) CALayer *container;
@property (strong) AWTMetalLayer *layer;
@property (strong) id<MTLDevice> adapter;
@property (strong) id<MTLCommandQueue> queue;
@property (strong) id<CAMetalDrawable> drawableHandle;
@property (strong) dispatch_semaphore_t inflightSemaphore;

@end

#endif