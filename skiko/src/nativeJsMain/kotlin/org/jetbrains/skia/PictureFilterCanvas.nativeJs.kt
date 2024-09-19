package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope

internal actual fun PictureFilterCanvas.doInit(ptr: NativePointer) {
    interopScope {
        val onFilter = virtualBoolean {
            onDrawPicture(
                picturePtr = PictureFilterCanvas_nGetOnDrawPicture_picture(ptr),
                matrixPtr = PictureFilterCanvas_nGetOnDrawPicture_matrix(ptr),
                paintPtr = PictureFilterCanvas_nGetOnDrawPicture_paint(ptr)
            )
        }
        PictureFilterCanvas_nInit(ptr, onFilter)
    }
}

@ExternalSymbolName("org_jetbrains_skia_PictureFilterCanvas__1nInit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureFilterCanvas__1nInit")
internal external fun PictureFilterCanvas_nInit(ptr: NativePointer, onFilter: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_picture")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_picture")
internal external fun PictureFilterCanvas_nGetOnDrawPicture_picture(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_matrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_matrix")
internal external fun PictureFilterCanvas_nGetOnDrawPicture_matrix(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_paint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_SkikoPictureFilterCanvas__1nGetOnDrawPicture_paint")
internal external fun PictureFilterCanvas_nGetOnDrawPicture_paint(ptr: NativePointer): NativePointer
