#ifdef SK_METAL

#import <stdatomic.h>

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

/// True while an interactive live resize is in progress. When set, the layer drives a synchronous,
/// transactional redraw from its own bounds change (on the main thread) so content, drawableSize and
/// the window backing all land in the same CATransaction.
@property BOOL liveResizing;

@end

@interface MetalDevice : NSObject {
@public
    /// Coalesces animation-driven resize frames onto the AppKit main thread: at most one is in flight
    /// at a time. Owned entirely by scheduleFrameOnAppKitThread (test-and-set to dispatch, cleared
    /// unconditionally at the start of the dispatched block). Zero-inits to false. atomic_bool (not a
    /// @property) so the schedule side can do a single race-free atomic_exchange; scheduling can come
    /// from both the EDT and the main thread.
    atomic_bool frameOnAppKitThreadScheduled;
}

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