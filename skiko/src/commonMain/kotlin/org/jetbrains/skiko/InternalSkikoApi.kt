package org.jetbrains.skiko

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message =
        "This is internal API for Skiko modules that may change frequently " +
                "and without warning.",
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.TYPEALIAS,
)
@Retention(AnnotationRetention.BINARY)
public annotation class InternalSkikoApi
