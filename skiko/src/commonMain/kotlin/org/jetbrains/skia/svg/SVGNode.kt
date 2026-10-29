package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.nativeCall
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

abstract class SVGNode internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    val tag: SVGTag
        get() = nativeCall { SVGTag.entries[SVGNode_nGetTag(_ptr)] }
}

@ExternalSymbolName("org_jetbrains_skia_svg_SVGNode__1nGetTag")
// No ForceNativeThreadState: only returns a stored tag, so it is short and cannot block.
// Native call path: SkSVGNode::tag
private external fun SVGNode_nGetTag(ptr: NativePointer): Int
