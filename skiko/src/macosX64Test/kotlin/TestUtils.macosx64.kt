package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking

annotation class DoNothing
actual typealias IgnoreTestOnJvm = DoNothing

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}
