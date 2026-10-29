package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.interopScope
import kotlin.jvm.JvmStatic

/**
 * Blender represents a custom blend function in the Skia pipeline.  A blender combines a source
 * color (the result of our paint) and destination color (from the canvas) into a final color.
 */
class Blender internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        @JvmStatic
        fun makeMode(mode: BlendMode): Blender {
            Stats.onNativeCall()
            return Blender(_nMakeMode(mode.ordinal))
        }

        @JvmStatic
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

@ExternalSymbolName("org_jetbrains_skia_Blender__1nMakeArithmetic")
// Needs ForceNativeThreadState: may compile a shader program on first use, and that setup can wait for another thread.
// Native call path: SkBlenders::Arithmetic -> GetKnownRuntimeEffect -> SkRuntimeEffect::MakeForBlender
@ForceNativeThreadState
private external fun _nMakeArithmetic(
    k1: Float,
    k2: Float,
    k3: Float,
    k4: Float,
    enforcePMColor: Boolean
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Blender__1nMakeMode")
// Needs ForceNativeThreadState: first use initializes a static object, and concurrent first calls can wait for another
// thread to finish.
// Native call path: SkBlender::Mode -> GetBlendModeSingleton
@ForceNativeThreadState
private external fun _nMakeMode(mode: Int): NativePointer
