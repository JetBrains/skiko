#import "AWTMetalLayer.h"

@implementation AWTMetalLayer

- (id)init {
    self = [super init];

    if (self) {
        [self removeAllAnimations];
        [self setAutoresizingMask: (kCALayerWidthSizable|kCALayerHeightSizable)];
        [self setNeedsDisplayOnBoundsChange: YES];
    }

    return self;
}

@end
