package org.jetbrains.skia.shaper

import org.jetbrains.skia.FontFeature.Companion.arrayOfFontFeaturesToInterop
import org.jetbrains.skia.ManagedString
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
    val managedFontIter = toManaged(fontIter)
    val managedBidiIter = toManaged(bidiIter)
    val managedScriptIter = toManaged(scriptIter)
    val managedLangIter = toManaged(langIter)
    try {
        interopScope {
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
                runHandler = runHandler as InteropPointer
            )
        }
    } finally {
        reachabilityBarrier(managedFontIter)
        reachabilityBarrier(managedBidiIter)
        reachabilityBarrier(managedScriptIter)
        reachabilityBarrier(managedLangIter)
    }
}

private fun toManaged(iter: Iterator<FontRun?>): Managed =
    if (iter is ManagedRunIterator<FontRun?>) { iter } else { RunIteratorBase.fromIterator(iter) }
private fun toManaged(iter: Iterator<LanguageRun?>): Managed =
    if (iter is ManagedRunIterator<LanguageRun?>) { iter } else { RunIteratorBase.fromIterator(iter) }
private fun toManaged(iter: Iterator<BidiRun?>): Managed =
    if (iter is ManagedRunIterator<BidiRun?>) { iter } else { RunIteratorBase.fromIterator(iter) }
private fun toManaged(iter: Iterator<ScriptRun?>): Managed =
    if (iter is ManagedRunIterator<ScriptRun?>) { iter } else { RunIteratorBase.fromIterator(iter) }

abstract class RunIteratorBase protected constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
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
                onConsume = virtual { this@RunIteratorBase.consume() },
                onEndOfCurrentRun = virtualInt { this@RunIteratorBase.endOfCurrentRun() },
                onAtEnd = virtualBoolean { this@RunIteratorBase.atEnd() },
                onCurrent = getOnCurrentCallback(),
            )
        }
    }

    protected abstract fun InteropScope.getOnCurrentCallback(): InteropPointer

    companion object {
        fun fromIterator(iterator: Iterator<BidiRun?>): RunIteratorBase =
            IteratorBasedBidiRunIterator(RunIterator_nCreateRunIterator(BIDI_RUN_ITERATOR_TYPE), iterator)

        fun fromIterator(iterator: Iterator<FontRun?>): RunIteratorBase =
            IteratorBasedFontRunIterator(RunIterator_nCreateRunIterator(FONT_RUN_ITERATOR_TYPE), iterator)

        fun fromIterator(iterator: Iterator<ScriptRun?>): RunIteratorBase =
            IteratorBasedScriptRunIterator(RunIterator_nCreateRunIterator(SCRIPT_RUN_ITERATOR_TYPE), iterator)

        fun fromIterator(iterator: Iterator<LanguageRun?>): RunIteratorBase =
            IteratorBasedLanguageRunIterator(RunIterator_nCreateRunIterator(LANGUAGE_RUN_ITERATOR_TYPE), iterator)
    }
}

abstract class FontRunIterator protected constructor(ptr: NativePointer): RunIteratorBase(ptr) {
    abstract fun currentFont(): FontRun?

    override fun InteropScope.getOnCurrentCallback() = virtualNativePointer { getPtr(currentFont()?.font) }
}

abstract class BiDiRunIterator protected constructor(ptr: NativePointer): RunIteratorBase(ptr) {
    abstract fun currentLevel(): BidiRun?

    override fun InteropScope.getOnCurrentCallback() = virtualInt { currentLevel()?.level ?: 0 }
}

abstract class ScriptRunIterator protected constructor(ptr: NativePointer): RunIteratorBase(ptr) {
    abstract fun currentScript(): ScriptRun?

    override fun InteropScope.getOnCurrentCallback() = virtualInt { currentScript()?.scriptTag ?: 0 }
}

abstract class LanguageRunIterator protected constructor(ptr: NativePointer): RunIteratorBase(ptr) {
    abstract fun currentLanguage(): LanguageRun?

    private var interopScope: InteropScope? = null

    private fun langToInterop(): InteropPointer {
        if (interopScope != null) {
            interopScope!!.release()
            interopScope = InteropScope()
        }
        return interopScope!!.toInterop(currentLanguage()?.language)
    }

    override fun InteropScope.getOnCurrentCallback() = virtualInteropPointer { langToInterop() }
}

class IteratorBasedFontRunIterator internal constructor(ptr: NativePointer, val iterator: Iterator<FontRun?>) : FontRunIterator(ptr) {
    private var current: FontRun? = null

    override fun currentFont() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

class IteratorBasedBidiRunIterator internal constructor(ptr: NativePointer, val iterator: Iterator<BidiRun?>) : BiDiRunIterator(ptr) {
    private var current: BidiRun? = null

    override fun currentLevel() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

class IteratorBasedScriptRunIterator internal constructor(ptr: NativePointer, val iterator: Iterator<ScriptRun?>) : ScriptRunIterator(ptr) {
    private var current: ScriptRun? = null

    override fun currentScript() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

class IteratorBasedLanguageRunIterator internal constructor(ptr: NativePointer, val iterator: Iterator<LanguageRun?>) : LanguageRunIterator(ptr) {
    private var current: LanguageRun? = null

    override fun currentLanguage() = current
    override fun consume() { current = iterator.next() }
    override fun endOfCurrentRun() = current?.end ?: 0
    override fun atEnd() = !iterator.hasNext()
}

private const val FONT_RUN_ITERATOR_TYPE: Int = 1
private const val BIDI_RUN_ITERATOR_TYPE: Int = 2
private const val SCRIPT_RUN_ITERATOR_TYPE: Int = 3
private const val LANGUAGE_RUN_ITERATOR_TYPE: Int = 4
