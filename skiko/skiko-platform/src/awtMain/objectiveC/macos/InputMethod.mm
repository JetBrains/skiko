#import "jni.h"
#import <Cocoa/Cocoa.h>

extern "C"
{

JNIEXPORT void Java_org_jetbrains_skiko_PlatformOperationsKt_osxOrderEmojiAndSymbolsPopup() {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSApplication* app = [NSApplication sharedApplication];
        [app orderFrontCharacterPalette:nil];
    });
}

} // extern C