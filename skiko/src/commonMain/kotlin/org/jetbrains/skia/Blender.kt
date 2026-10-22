package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.interopScope

/**
 * Blender represents a custom blend function in the Skia pipeline.  A blender combines a source
 * color (the result of our paint) and destination color (from the canvas) into a final color.
 */
class Blender internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        fun makeMode(mode: BlendMode): Blender {
            Stats.onNativeCall()
            return Blender(_nMakeMode(mode.ordinal))
        }

        fun makeArithmetic(
            k1: Float,
            k2: Float,
            k3: Float,
            k4: Float,
            enforcePMColor: Boolean,
        ): Blender {
            Stats.onNativeCall()
            return interopScope {
                Blender(
                    _nMakeArithmetic(
                        k1,
                        k2,
                        k3,
                        k4,
                        enforcePMColor,
                    ),
                )
            }
        }
    }
}

// Native: SkBlenders::Arithmetic -> GetKnownRuntimeEffect compiles SkSL in a function-local static
@ExternalSymbolName("org_jetbrains_skia_Blender__1nMakeArithmetic")
@ForceNativeThreadState
private external fun _nMakeArithmetic(
    k1: Float,
    k2: Float,
    k3: Float,
    k4: Float,
    enforcePMColor: Boolean
): NativePointer

// Native: SkBlender::Mode -> GetBlendModeSingleton can wait for function-local static initialization
@ExternalSymbolName("org_jetbrains_skia_Blender__1nMakeMode")
@ForceNativeThreadState
private external fun _nMakeMode(mode: Int): NativePointer
