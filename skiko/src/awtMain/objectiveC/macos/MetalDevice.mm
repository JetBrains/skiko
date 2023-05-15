#import "MetalDevice.h"

extern JavaVM* jvm;

static CVReturn MetalDeviceDisplayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *now, const CVTimeStamp *outputTime, CVOptionFlags flagsIn, CVOptionFlags *flagsOut, void *displayLinkContext) {
    MetalDevice *device = (__bridge MetalDevice *)displayLinkContext;

    [device handleDisplayLinkFired];

    return kCVReturnSuccess;
}

@implementation MetalDevice {
    NSScreen *_displayLinkScreen;
    CVDisplayLinkRef _displayLink;
    jobject _displayLinkCallbackObject;
    jmethodID _displayLinkCallbackMethod;
}

- (instancetype)initWithContainer:(CALayer *)container adapter:(id<MTLDevice>)adapter window:(NSWindow *)window env:(JNIEnv *)env displayLinkCallback:(jobject)displayLinkCallback
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

        // TODO: Release global ref

        /// Make Kotlin callback object global and store handles to call its `invoke` method
        _displayLinkCallbackObject = env->NewGlobalRef(displayLinkCallback);
        assert(_displayLinkCallbackObject);

        jclass displayLinkCallbackClass = env->GetObjectClass(_displayLinkCallbackObject);
        assert(displayLinkCallbackClass);

        _displayLinkCallbackMethod = env->GetMethodID(displayLinkCallbackClass, "invoke", "()V");
        assert(_displayLinkCallbackMethod);
    }

    return self;
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
    JNIEnv* env = NULL;

    jint result = jvm->GetEnv((void **)&env, JNI_VERSION_1_6);

    if (result == JNI_EDETACHED) {
        /// If the current thread is not attached to the JVM, attach it
        /// TODO: do research on whether it can cause issues
        if (jvm->AttachCurrentThread((void **)&env, NULL) != 0) {
            env = NULL;
        }
    }

    if (env) {
        env->CallVoidMethod(_displayLinkCallbackObject, _displayLinkCallbackMethod);
    }
}

- (void)invalidateDisplayLink {
    if (_displayLink) {
        CVDisplayLinkStop(_displayLink);
        CVDisplayLinkRelease(_displayLink);
        _displayLink = nil;
    }
}

@end