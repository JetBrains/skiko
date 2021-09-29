package org.jetbrains.skiko.tests

import kotlinx.coroutines.runBlocking

actual typealias IgnoreTestOnJvm = org.junit.Ignore

actual fun runTest(block: suspend () -> Unit) {
    runBlocking { block() }
}
