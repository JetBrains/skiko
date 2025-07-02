package org.jetbrains.skia

internal expect fun <R> commonSynchronized(lock: Any, block: () -> R)

internal expect fun defaultLanguageTag(): String

expect class Pattern {
    fun split(input: CharSequence): Array<String>
    fun matcher(input: CharSequence): Matcher
}

expect class Matcher {
    // Named groups are not supported in k/n. That's why we can use only numeric groups
    fun group(ix: Int): String?
    fun matches(): Boolean
}

internal expect fun compilePattern(regex: String): Pattern

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class ExternalSymbolName(val name: String)

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FILE)
expect annotation class QuasiJsModule(
    val import: String,
)

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FILE)
expect annotation class QuasiJsNonModule()

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FILE)
expect annotation class QuasiJsQualifier(
    val value: String,
)

//@OptIn(ExperimentalMultiplatform::class)
//@OptionalExpectation
//@Target(AnnotationTarget.FUNCTION)
//expect annotation class QuasiJsName(
//    val name: String,
//)
