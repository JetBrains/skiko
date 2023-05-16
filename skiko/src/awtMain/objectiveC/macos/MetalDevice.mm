#import "MetalDevice.h"

#include <mutex>
#include <condition_variable>

static CVReturn MetalDeviceDisplayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *now, const CVTimeStamp *outputTime, CVOptionFlags flagsIn, CVOptionFlags *flagsOut, void *displayLinkContext) {
    MetalDevice *device = (__bridge MetalDevice *)displayLinkContext;

    [device handleDisplayLinkFired];

    return kCVReturnSuccess;
}

@implementation MetalDevice {
    NSScreen *_displayLinkScreen;
    CVDisplayLinkRef _displayLink;

    dispatch_semaphore_t _presentingBuffersExhaustionSemaphore;
    std::mutex _vsyncMutex;
    std::condition_variable _vsyncConditionVariable;
    bool _vsyncReady;
}

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window
{
    self = [super init];

    if (self)
    {
        self.container = container;
        self.adapter = adapter;
        self.queue = [adapter newCommandQueue];
        self.window = window;
        self.layer = [AWTMetalLayer new];

        _displayLinkScreen = nil;

        [container removeAllAnimations];
        [container setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [container setNeedsDisplayOnBoundsChange: YES];

        self.layer.device = adapter;
        self.layer.pixelFormat = MTLPixelFormatBGRA8Unorm;
        self.layer.contentsGravity = kCAGravityTopLeft;
        CGFloat transparent[] = { 0.0f, 0.0f, 0.0f, 0.0f };
        self.layer.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), transparent);
        self.layer.opaque = NO;

        [container addSublayer: self.layer];

        _presentingBuffersExhaustionSemaphore = dispatch_semaphore_create(3);
        _vsyncReady = true;
    }

    return self;
}

- (void)dealloc {
    [self invalidateDisplayLink];
}

- (void)recreateDisplayLinkIfNeeded {

    /// Check if last _displayLinkScreen is the same as current NSWindow one. If not, invalidate current displayLink and create a new one.
    if (![self.window.screen isEqualTo: _displayLinkScreen]) {
        [self invalidateDisplayLink];

        _displayLinkScreen = self.window.screen;

        NSDictionary* screenDescription = [_displayLinkScreen deviceDescription];
        NSNumber* screenID = [screenDescription objectForKey:@"NSScreenNumber"];

        /// TODO: create fallback for any possible failure
        CVReturn result;

        CVDisplayLinkRef displayLink;
        result = CVDisplayLinkCreateWithCGDisplay([screenID unsignedIntValue], &displayLink);
        assert(result == kCVReturnSuccess);

        result = CVDisplayLinkSetOutputCallback(displayLink, &MetalDeviceDisplayLinkCallback, (__bridge void *)(self));
        assert(result == kCVReturnSuccess);

        _displayLink = displayLink;

        result = CVDisplayLinkStart(displayLink);
        assert(result == kCVReturnSuccess);

        NSLog(@"DisplayLink launched for screen with ID: %@", screenID);
    }
}

- (void)handleDisplayLinkFired {
    std::unique_lock<std::mutex> lock(_vsyncMutex);

    _vsyncReady = true;

    _vsyncConditionVariable.notify_one();
}

- (void)waitUntilVsync {
    std::unique_lock<std::mutex> lock(_vsyncMutex);

    while (!_vsyncReady) {
        _vsyncConditionVariable.wait(lock);
    }

    _vsyncReady = false;
}

- (void)waitForQueueSlot {
    dispatch_semaphore_wait(_presentingBuffersExhaustionSemaphore, DISPATCH_TIME_FOREVER);
}

- (void)freeQueueSlot {
    dispatch_semaphore_signal(_presentingBuffersExhaustionSemaphore);
}

- (void)invalidateDisplayLink {
    if (_displayLink) {
        CVDisplayLinkStop(_displayLink);
        CVDisplayLinkRelease(_displayLink);
        _displayLink = nil;
    }
}

@end