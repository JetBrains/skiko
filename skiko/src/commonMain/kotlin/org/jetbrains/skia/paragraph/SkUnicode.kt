package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer

/**
 * A text segmentation and unicode properties engine used by the text layout pipeline.
 *
 * An instance owns the native `SkUnicode` together with the text buffer it refers to,
 * therefore it has to be kept alive as long as the [ParagraphBuilder] it was passed to.
 */
open class SkUnicode internal constructor(ptr: NativePointer, finalizer: NativePointer) :
    Managed(ptr, finalizer)
