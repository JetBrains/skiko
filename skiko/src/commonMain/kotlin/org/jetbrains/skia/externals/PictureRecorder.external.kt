package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nMake")
internal external fun PictureRecorder_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nGetFinalizer")
internal external fun PictureRecorder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nBeginRecording")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nBeginRecording")
internal external fun PictureRecorder_nBeginRecording(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    bbh: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nGetRecordingCanvas")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nGetRecordingCanvas")
internal external fun PictureRecorder_nGetRecordingCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPicture")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPicture")
internal external fun PictureRecorder_nFinishRecordingAsPicture(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPictureWithCull")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPictureWithCull")
internal external fun PictureRecorder_nFinishRecordingAsPictureWithCull(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsDrawable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsDrawable")
internal external fun PictureRecorder_nFinishRecordingAsDrawable(ptr: NativePointer): NativePointer

