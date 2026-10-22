package org.jetbrains.skia.skottie

import org.jetbrains.skia.Data
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.*

class AnimationBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        internal fun _flagsToInt(vararg builderFlags: AnimationBuilderFlag): Int {
            var flags = 0
            for (flag in builderFlags) flags = flags or flag._flag
            return flags
        }

        init {
            SkottieLibrary.load()
        }
    }

    private object _FinalizerHolder {
        val PTR = AnimationBuilder_nGetFinalizer()
    }

    constructor() : this(*emptyArray<AnimationBuilderFlag>()) {}
    constructor(vararg builderFlags: AnimationBuilderFlag) : this(AnimationBuilder_nMake(_flagsToInt(*builderFlags))) {
        Stats.onNativeCall()
    }

    /**
     *
     * Specify a font manager for loading animation fonts.
     */
    fun setFontManager(fontMgr: FontMgr?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            _nSetFontManager(nativePtr, getPtr(fontMgr))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    /**
     *
     * Register a [Logger] with this builder.
     */
    fun setLogger(logger: Logger?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            _nSetLogger(nativePtr, getPtr(logger))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(logger)
        }
    }

    fun buildFromString(data: String): Animation {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope { _nBuildFromString(nativePtr, toInterop(data)) }
            require(ptr != NullPointer) { "Failed to create Animation from string: \"$data\"" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun buildFromData(data: Data): Animation {
        return try {
            Stats.onNativeCall()
            val ptr =
                _nBuildFromData(nativePtr, getPtr(data))
            require(ptr != NullPointer) { "Failed to create Animation from data" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(data)
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteAnimationBuilder
private external fun AnimationBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nMake")
// No ForceNativeThreadState: creates a builder that only stores flags and empty fields and owns nothing yet, so it is
// short and cannot block.
// Native call path: Animation::Builder::Builder
private external fun AnimationBuilder_nMake(flags: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager")
// Needs ForceNativeThreadState: can drop the old font manager, which releases CoreText font collections and typefaces.
// Native call path: Builder::setFontManager -> ~SkFontMgr_Mac
@ForceNativeThreadState
private external fun _nSetFontManager(ptr: NativePointer, fontMgrPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger")
// Needs ForceNativeThreadState: dropping the old logger calls back into Kotlin to dispose its callback.
// Native call path: setLogger -> ~SkikoLogger -> ~KCallback -> disposeCallback
@ForceNativeThreadState
private external fun _nSetLogger(ptr: NativePointer, loggerPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString")
// Needs ForceNativeThreadState: parses JSON and builds the scene, including text layout; time grows with the string
// length, and logging can call Kotlin.
// Native call path: Animation::Builder::make -> AnimationBuilder::parse -> Logger::log
@ForceNativeThreadState
private external fun _nBuildFromString(ptr: NativePointer, data: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile")
// Needs ForceNativeThreadState: reads the animation file with blocking file I/O, then parses it and can invoke a logger
// callback.
// Native call path: Builder::makeFromFile -> SkData::MakeFromFileName -> Builder::make
@ForceNativeThreadState
internal external fun _nBuildFromFile(ptr: NativePointer, path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData")
// Needs ForceNativeThreadState: copies and parses the stream, so its time grows with the data size, and can invoke a
// logger callback.
// Native call path: Builder::make -> SkData::MakeFromStream -> Builder::make
@ForceNativeThreadState
private external fun _nBuildFromData(ptr: NativePointer, dataPtr: NativePointer): NativePointer
