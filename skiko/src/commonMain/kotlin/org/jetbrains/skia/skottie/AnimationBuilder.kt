@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.Data
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class AnimationBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        internal fun _flagsToInt(vararg builderFlags: AnimationBuilderFlag): Int {
            var flags = 0
            for (flag in builderFlags) flags = flags or flag._flag
            return flags
        }

        init {
            staticLoad()
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
            _nSetFontManager(_ptr, getPtr(fontMgr))
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
            _nSetLogger(_ptr, getPtr(logger))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(logger)
        }
    }

    fun buildFromString(data: String): Animation {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope { _nBuildFromString(_ptr, toInterop(data)) }
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
                _nBuildFromData(_ptr, getPtr(data))
            require(ptr != NullPointer) { "Failed to create Animation from data" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(data)
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nGetFinalizer")
private external fun AnimationBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nMake")
private external fun AnimationBuilder_nMake(flags: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager")
private external fun _nSetFontManager(ptr: NativePointer, fontMgrPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger")
private external fun _nSetLogger(ptr: NativePointer, loggerPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString")
private external fun _nBuildFromString(ptr: NativePointer, data: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile")
internal external fun _nBuildFromFile(ptr: NativePointer, path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData")
private external fun _nBuildFromData(ptr: NativePointer, dataPtr: NativePointer): NativePointer
