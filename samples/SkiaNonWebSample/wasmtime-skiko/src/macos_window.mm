#include "macos_window.h"

#import <Cocoa/Cocoa.h>
#import <CoreGraphics/CoreGraphics.h>

#include <stdexcept>

@interface SkikoFrameView : NSView
@property(nonatomic, strong) NSData* rgba;
@property(nonatomic) NSInteger imageWidth;
@property(nonatomic) NSInteger imageHeight;
@end

@implementation SkikoFrameView

- (BOOL)isFlipped {
    return YES;
}

- (void)drawRect:(NSRect)dirtyRect {
    (void)dirtyRect;
    if (self.rgba == nil) return;

    CGDataProviderRef provider = CGDataProviderCreateWithCFData(
        (__bridge CFDataRef)self.rgba);
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGImageRef image = CGImageCreate(
        static_cast<size_t>(self.imageWidth),
        static_cast<size_t>(self.imageHeight),
        8,
        32,
        static_cast<size_t>(self.imageWidth) * 4,
        colorSpace,
        kCGBitmapByteOrder32Big | kCGImageAlphaLast,
        provider,
        nullptr,
        false,
        kCGRenderingIntentDefault);

    CGContextRef context = NSGraphicsContext.currentContext.CGContext;
    CGContextSaveGState(context);
    CGContextTranslateCTM(context, 0, self.bounds.size.height);
    CGContextScaleCTM(context, 1, -1);
    CGContextSetInterpolationQuality(context, kCGInterpolationLow);
    CGContextDrawImage(context, NSRectToCGRect(self.bounds), image);
    CGContextRestoreGState(context);

    CGImageRelease(image);
    CGColorSpaceRelease(colorSpace);
    CGDataProviderRelease(provider);
}

@end

@interface SkikoWindowDelegate : NSObject <NSWindowDelegate>
@property(nonatomic) BOOL closed;
@end

@implementation SkikoWindowDelegate
- (void)windowWillClose:(NSNotification*)notification {
    (void)notification;
    self.closed = YES;
}
@end

struct MacOSWindow::Impl {
    int width;
    int height;
    __strong NSWindow* window;
    __strong SkikoFrameView* view;
    __strong SkikoWindowDelegate* delegate;
};

MacOSWindow::MacOSWindow(int width, int height, const std::string& title)
    : impl_(std::make_unique<Impl>()) {
    @autoreleasepool {
        impl_->width = width;
        impl_->height = height;
        [NSApplication sharedApplication];
        [NSApp setActivationPolicy:NSApplicationActivationPolicyRegular];
        [NSApp finishLaunching];

        const NSRect frame = NSMakeRect(0, 0, width, height);
        impl_->window = [[NSWindow alloc]
            initWithContentRect:frame
                      styleMask:NSWindowStyleMaskTitled |
                                NSWindowStyleMaskClosable |
                                NSWindowStyleMaskMiniaturizable
                        backing:NSBackingStoreBuffered
                          defer:NO];
        if (impl_->window == nil) {
            throw std::runtime_error("Could not create the macOS window");
        }
        impl_->view = [[SkikoFrameView alloc] initWithFrame:frame];
        impl_->view.imageWidth = width;
        impl_->view.imageHeight = height;
        impl_->delegate = [[SkikoWindowDelegate alloc] init];
        impl_->window.delegate = impl_->delegate;
        impl_->window.contentView = impl_->view;
        [impl_->window setTitle:[NSString stringWithUTF8String:title.c_str()]];
        [impl_->window center];
        [impl_->window makeKeyAndOrderFront:nil];
        [NSApp activateIgnoringOtherApps:YES];
    }
}

MacOSWindow::~MacOSWindow() {
    @autoreleasepool {
        impl_->window.delegate = nil;
        [impl_->window orderOut:nil];
        [impl_->window close];
    }
}

bool MacOSWindow::IsOpen() const {
    return !impl_->delegate.closed;
}

void MacOSWindow::PollEvents() {
    @autoreleasepool {
        while (NSEvent* event = [NSApp
                   nextEventMatchingMask:NSEventMaskAny
                               untilDate:NSDate.distantPast
                                  inMode:NSDefaultRunLoopMode
                                 dequeue:YES]) {
            [NSApp sendEvent:event];
        }
        [NSApp updateWindows];
    }
}

void MacOSWindow::Present(const std::vector<uint8_t>& rgba) {
    const size_t expected =
        static_cast<size_t>(impl_->width) * impl_->height * 4;
    if (rgba.size() != expected) {
        throw std::runtime_error("Invalid RGBA frame size");
    }
    @autoreleasepool {
        impl_->view.rgba = [NSData dataWithBytes:rgba.data() length:rgba.size()];
        [impl_->view setNeedsDisplay:YES];
        [impl_->view displayIfNeeded];
    }
}

void MacOSWindow::SetTitle(const std::string& title) {
    @autoreleasepool {
        [impl_->window setTitle:[NSString stringWithUTF8String:title.c_str()]];
    }
}
