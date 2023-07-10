package org.jetbrains.skiko

/**
 * Mark a feature that depends on running on the JetBrains Runtime.
 *
 * If you use a feature marked by this annotation, you need to make sure
 * that you are running your code on the JetBrains Runtime, as it may
 * depend on private APIs or additional features that are not available
 * on other runtimes.
 *
 * Refer to the feature documentation to understand what its failure mode
 * is when running on other Java Virtual Machine implementations.
 */
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This functionality will only work correctly on the JetBrains Runtime."
)
annotation class DependsOnJBR
