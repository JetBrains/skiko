package org.jetbrains.skia.shaper

import org.jetbrains.skia.FontFeature.Companion.arrayOfFontFeaturesToInterop
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.Point
import org.jetbrains.skia.impl.*

internal actual fun Shaper.doShape(
    textUtf8: ManagedString,
    fontIter: Iterator<FontRun?>,
    bidiIter: Iterator<BidiRun?>,
    scriptIter: Iterator<ScriptRun?>,
    langIter: Iterator<LanguageRun?>,
    opts: ShapingOptions,
    width: Float,
    runHandler: RunHandler
) {
    val managedFontIter = toManaged(fontIter, textUtf8)
    val managedBidiIter = toManaged(bidiIter, textUtf8)
    val managedScriptIter = toManaged(scriptIter, textUtf8)
    val managedLangIter = toManaged(langIter, textUtf8)
    val managedRunHandler = RunHandlerImpl(runHandler)
    try {
        interopScope {
            @Suppress("CAST_NEVER_SUCCEEDS")
            Shaper_nShape(
                _ptr,
                getPtr(textUtf8),
                getPtr(managedFontIter) as InteropPointer,
                getPtr(managedBidiIter) as InteropPointer,
                getPtr(managedScriptIter) as InteropPointer,
                getPtr(managedLangIter) as InteropPointer,
                optsFeaturesLen = opts.features?.size ?: 0,
                optsFeaturesIntArray = arrayOfFontFeaturesToInterop(opts.features),
                optsBooleanProps = opts._booleanPropsToInt(),
                width = width,
                runHandler = getPtr(managedRunHandler) as InteropPointer
            )
        }
    } finally {
        reachabilityBarrier(managedFontIter)
        reachabilityBarrier(managedBidiIter)
        reachabilityBarrier(managedScriptIter)
        reachabilityBarrier(managedLangIter)
        reachabilityBarrier(managedRunHandler)
        reachabilityBarrier(textUtf8)
    }
}

private fun toManaged(iter: Iterator<FontRun?>, text: ManagedString): Managed =
    if (iter is ManagedRunIterator<FontRun?>) { iter } else { RunIteratorBase.fromIterator(iter, text) }
private fun toManaged(iter: Iterator<LanguageRun?>, text: ManagedString): Managed =
    if (iter is ManagedRunIterator<LanguageRun?>) { iter } else { RunIteratorBase.fromIterator(iter, text) }
private fun toManaged(iter: Iterator<BidiRun?>, text: ManagedString): Managed =
    if (iter is ManagedRunIterator<BidiRun?>) { iter } else { RunIteratorBase.fromIterator(iter, text) }
private fun toManaged(iter: Iterator<ScriptRun?>, text: ManagedString): Managed =
    if (iter is ManagedRunIterator<ScriptRun?>) { iter } else { RunIteratorBase.fromIterator(iter, text) }

sealed class RunIteratorBase(type: Int, ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    private object _FinalizerHolder {
        val PTR = RunIterator_nGetFinalizer()
    }

    abstract fun consume()
    abstract fun endOfCurrentRun(): Int
    abstract fun atEnd(): Boolean

    init {
        interopScope {
            RunIterator_nInitRunIterator (
                _ptr,
                type,
                onConsume = virtual { this@RunIteratorBase.consume() },
                onEndOfCurrentRun = virtualInt { this@RunIteratorBase.endOfCurrentRun() },
                onAtEnd = virtualBoolean { this@RunIteratorBase.atEnd() },
                onCurrent = getOnCurrentCallback(),
            )
        }
    }

    internal abstract fun InteropScope.getOnCurrentCallback(): InteropPointer

    companion object {
        fun fromIterator(iterator: Iterator<BidiRun?>, text: ManagedString): RunIteratorBase =
            IteratorBasedBidiRunIterator(RunIterator_nCreateRunIterator(BIDI_RUN_ITERATOR_TYPE, getPtr(text)), iterator)

        fun fromIterator(iterator: Iterator<FontRun?>, text: ManagedString): RunIteratorBase =
            IteratorBasedFontRunIterator(RunIterator_nCreateRunIterator(FONT_RUN_ITERATOR_TYPE, getPtr(text)), iterator)

        fun fromIterator(iterator: Iterator<ScriptRun?>, text: ManagedString): RunIteratorBase =
            IteratorBasedScriptRunIterator(RunIterator_nCreateRunIterator(SCRIPT_RUN_ITERATOR_TYPE, getPtr(text)), iterator)

        fun fromIterator(iterator: Iterator<LanguageRun?>, text: ManagedString): RunIteratorBase =
            IteratorBasedLanguageRunIterator(RunIterator_nCreateRunIterator(LANGUAGE_RUN_ITERATOR_TYPE, getPtr(text)), iterator)
    }
}

private sealed class FontRunIterator(ptr: NativePointer): RunIteratorBase(FONT_RUN_ITERATOR_TYPE, ptr) {
    abstract fun currentFont(): FontRun?

    override fun InteropScope.getOnCurrentCallback() = virtualNativePointer { getPtr(currentFont()?.font) }
}

private sealed class BiDiRunIterator(ptr: NativePointer): RunIteratorBase(BIDI_RUN_ITERATOR_TYPE, ptr) {
    abstract fun currentLevel(): BidiRun?

    override fun InteropScope.getOnCurrentCallback() = virtualInt { currentLevel()?.level ?: 0 }
}

private sealed class ScriptRunIterator(ptr: NativePointer): RunIteratorBase(SCRIPT_RUN_ITERATOR_TYPE, ptr) {
    abstract fun currentScript(): ScriptRun?

    override fun InteropScope.getOnCurrentCallback() = virtualInt { currentScript()?.scriptTag ?: 0 }
}

private sealed class LanguageRunIterator(ptr: NativePointer): RunIteratorBase(LANGUAGE_RUN_ITERATOR_TYPE, ptr) {
    abstract fun currentLanguage(): LanguageRun?
    abstract fun currentLanguageInterop(): InteropPointer

    override fun InteropScope.getOnCurrentCallback() = virtualInteropPointer { currentLanguageInterop() }
}

private class IteratorBasedFontRunIterator(ptr: NativePointer, val iterator: Iterator<FontRun?>) : FontRunIterator(ptr) {
    private var current: FontRun? = null

    override fun currentFont() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

private class IteratorBasedBidiRunIterator(ptr: NativePointer, val iterator: Iterator<BidiRun?>) : BiDiRunIterator(ptr) {
    private var current: BidiRun? = null

    override fun currentLevel() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

private class IteratorBasedScriptRunIterator(ptr: NativePointer, val iterator: Iterator<ScriptRun?>) : ScriptRunIterator(ptr) {
    private var current: ScriptRun? = null

    override fun currentScript() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

private class IteratorBasedLanguageRunIterator(ptr: NativePointer, val iterator: Iterator<LanguageRun?>) : LanguageRunIterator(ptr) {
    private var current: LanguageRun? = null
    private var interopScope: InteropScope? = null
    private var currentInterop: InteropPointer = interopScope { toInterop(null as ByteArray?) }

    override fun currentLanguage() = current
    override fun currentLanguageInterop() = currentInterop
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
    override fun consume() {
        current = iterator.next()
        currentInterop = langToInterop()
    }

    private fun langToInterop(): InteropPointer {
        if (interopScope != null) {
            interopScope!!.release()
        }
        interopScope = InteropScope()
        return interopScope!!.toInterop(currentLanguage()?.language)
    }
}

private const val FONT_RUN_ITERATOR_TYPE: Int = 1
private const val BIDI_RUN_ITERATOR_TYPE: Int = 2
private const val SCRIPT_RUN_ITERATOR_TYPE: Int = 3
private const val LANGUAGE_RUN_ITERATOR_TYPE: Int = 4

private class RunHandlerImpl(val runHandler: RunHandler) : Managed(RunHandler_nCreate(), _FinalizerHolder.PTR) {
    init {
        val impl = this
        interopScope {
            RunHandler_nInit(
                _ptr,
                onBeginLine = virtual { runHandler.beginLine() },
                onCommitRunInfo = virtual { runHandler.commitRunInfo() },
                onCommitLine = virtual { runHandler.commitLine() },
                onCommitRun = virtual {
                    val info = impl.runInfo
                    val glyphs = impl.getGlyphs(info.glyphCount)
                    val clusters = impl.getClusters(info.glyphCount)
                    val positions = impl.getPositions(info.glyphCount)
                    runHandler.commitRun(info, glyphs, positions, clusters)
                },
                onRunInfo = virtual { runHandler.runInfo(impl.runInfo) },
                onRunOffset = virtual { runHandler.runOffset(impl.runInfo)?.let { impl.setOffset(it) } }
            )
        }
    }

    private fun getGlyphs(count: Int) = withResult(ShortArray(count)) {
        RunHandler_nGetGlyphs(_ptr, it)
    }

    private fun getClusters(count: Int) = withResult(IntArray(count)) {
        RunHandler_nGetClusters(_ptr, it)
    }

    private fun getPositions(count: Int) = Point.fromArray(
        withResult(FloatArray(count * 2)) {
            RunHandler_nGetPositions(_ptr, it)
        }
    )

    private fun setOffset(point: Point) {
        RunHandler_nSetOffset(_ptr, point.x, point.y)
    }

    private val runInfo: RunInfo
        get() {
            var fontPtr = NullPointer
            val repr = withResult(IntArray(6)) {
                fontPtr = RunHandler_nGetRunInfo(_ptr, it)
            }

            return RunInfo(
                _fontPtr = fontPtr,
                bidiLevel = repr[0],
                advanceX = Float.fromBits(repr[1]),
                advanceY = Float.fromBits(repr[2]),
                glyphCount = repr[3],
                rangeBegin = repr[4],
                rangeSize = repr[5]
            )
        }

    private object _FinalizerHolder {
        val PTR = RunHandler_nGetFinalizer()
    }
}

