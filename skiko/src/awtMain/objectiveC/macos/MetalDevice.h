#ifdef SK_METAL

@interface AWTMetalLayer : CAMetalLayer

@property jobject javaRef;

@end

@interface MetalDevice : NSObject

@property (weak) CALayer *container;
@property (retain, strong) AWTMetalLayer *layer;
@property (retain, strong) id<MTLDevice> adapter;
@property (retain, strong) id<MTLCommandQueue> queue;

@end

#endif