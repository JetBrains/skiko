package org.jetbrains.skiko.tests
expect annotation class IgnoreTestOnJvm()

expect fun runTest(block: suspend () -> Unit)
