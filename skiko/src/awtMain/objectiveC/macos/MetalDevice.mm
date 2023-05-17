#import "MetalDevice.h"
#import <stdatomic.h>

static CVReturn MetalDeviceDisplayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *now, const CVTimeStamp *outputTime, CVOptionFlags flagsIn, CVOptionFlags *flagsOut, void *displayLinkContext) {
    MetalDevice *device = (__bridge MetalDevice *)displayLinkContext;

    [device handleDisplayLinkFired];

    return kCVReturnSuccess;
}

@implementation MetalDevice {
    NSScreen *_displayLinkScreen;
    CVDisplayLinkRef _displayLink;

    dispatch_semaphore_t _presentingBuffersExhaustionSemaphore;
    NSConditionLock *_vsyncConditionLock;
    volatile atomic_bool _displayLinkOk;
}

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window {
    self = [super init];

    if (self) {
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

        _vsyncConditionLock = [[NSConditionLock alloc] initWithCondition: 1];

        atomic_store(&_displayLinkOk, true);
    }

    return self;
}

- (void)dealloc {
    [self invalidateDisplayLink];
}

- (void)handleDisplayLinkSetupFailure {
    atomic_store(&_displayLinkOk, false);

    /// Next line is here to tackle edge case where displayLink reconstruction failed
    /// but someone is already waiting at `waitUntilVsync`
    /// It's quite improbable scenario anyway
    [self handleDisplayLinkFired];
}

- (void)recreateDisplayLinkIfNeeded {
    if ([self.window.screen isEqualTo: _displayLinkScreen]) {
        return;
    }

    [self invalidateDisplayLink];

    _displayLinkScreen = self.window.screen;

    NSDictionary* screenDescription = [_displayLinkScreen deviceDescription];
    NSNumber* screenID = [screenDescription objectForKey:@"NSScreenNumber"];

    /// TODO: create fallback for any possible failure
    CVReturn result;

    CVDisplayLinkRef displayLink;
    result = CVDisplayLinkCreateWithCGDisplay([screenID unsignedIntValue], &displayLink);

    if (result != kCVReturnSuccess) {
        [self handleDisplayLinkSetupFailure];
        return;
    }

    result = CVDisplayLinkSetOutputCallback(displayLink, &MetalDeviceDisplayLinkCallback, (__bridge void *)(self));

    if (result != kCVReturnSuccess) {
        [self handleDisplayLinkSetupFailure];
        return;
    }

    _displayLink = displayLink;

    result = CVDisplayLinkStart(displayLink);

    if (result != kCVReturnSuccess) {
        [self handleDisplayLinkSetupFailure];
        return;
    }

    atomic_store(&_displayLinkOk, true);

    NSLog(@"DisplayLink launched for screen with ID: %@", screenID);
}

- (void)handleDisplayLinkFired {
    [_vsyncConditionLock lock];
    [_vsyncConditionLock unlockWithCondition:1];
}

- (void)waitUntilVsync {
    bool displayLinkOk = atomic_load(&_displayLinkOk);

    /// If display link construction was corrupted, don't perform any waiting
    if (!displayLinkOk) {
        return;
    }

    [_vsyncConditionLock lockWhenCondition:1];
    [_vsyncConditionLock unlockWithCondition:0];
}

- (void)waitForQueueSlot {
    /// In case we receive more encoded command buffers, than gpu can handle (GPU bottleneck),
    /// we need to throttle it down and start a new frame only when one currently run is finished
    ///
    /// see call place of `freeQueueSlot`
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
