#ifdef SK_METAL

#import <stdatomic.h>

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

/// True when the user drags-to-resize the window.
///
/// Explicitly atomic because it can be read off the main thread.
/// It is a plain synthesized ivar, not a CoreAnimation-backed property, so reading it on the Metal scheduler thread
/// takes no CA lock and cannot block.
@property (atomic) BOOL liveResizing;

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

@end

#endif