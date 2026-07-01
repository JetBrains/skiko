#ifdef SK_METAL

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

/// True while an interactive live resize is in progress. When set, the layer drives a synchronous,
/// transactional redraw from its own bounds change (on the main thread) so content, drawableSize and
/// the window backing all land in the same CATransaction.
@property BOOL liveResizing;

@end

@interface MetalDevice : NSObject

@property (weak) CALayer *container;
@property (strong) AWTMetalLayer *layer;
@property (strong) id<MTLDevice> adapter;
@property (strong) id<MTLCommandQueue> queue;
@property (strong) id<CAMetalDrawable> drawableHandle;
@property (strong) dispatch_semaphore_t inflightSemaphore;
@property (strong) id<NSObject> occlusionObserver;
@property (strong) id<NSObject> liveResizeStartObserver;
@property (strong) id<NSObject> liveResizeEndObserver;

/// True while the window is in an interactive (edge-drag) live resize. During that window we present
/// drawables synchronously and transactionally (presentsWithTransaction) so the Metal content swap is
/// committed together with the layer, instead of racing it on another thread.
@property (atomic) BOOL inLiveResize;

@end

#endif