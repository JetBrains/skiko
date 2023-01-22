package org.jetbrains.skiko.sample

import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skiko.SkiaLayer
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.Metal.MTLDeviceProtocol
import platform.UIKit.UIView

class SwiftHelper {
    fun getViewController() = getSkikoViewContoller()
}
//
//fun createMetalTexture(uiView:UIView, device: MTLDeviceProtocol) {
//        val (width, height) = uiView.bounds().useContents { size.width to size.height }
//        val context = CGBitmapContextCreate(null, width.toULong(), height.toULong(), 8, 0, CGColorSpaceCreateDeviceRGB(), CGImageAlphaInfo.kCGImageAlphaPremultipliedLast);
//        val data = CGBitmapContextCreate(context)
//        void *data = CGBitmapContextGetData(context);
//        [uiView.layer renderInContext:context];
//        MTLTextureDescriptor *desc = [MTLTextureDescriptor texture2DDescriptorWithPixelFormat:MTLPixelFormatRGBA8Unorm width:width height:height mipmapped:false];
//        id <MTLTexture> texture = [device newTextureWithDescriptor:desc];
//        [texture replaceRegion:MTLRegionMake2D(0, 0, width, height) mipmapLevel:0 withBytes:data bytesPerRow:CGBitmapContextGetBytesPerRow(context)];
//        return texture;
//}
