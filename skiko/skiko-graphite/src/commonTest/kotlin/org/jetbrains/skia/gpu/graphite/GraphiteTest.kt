package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.ExperimentalSkikoApi
import kotlin.test.Test

@OptIn(ExperimentalSkikoApi::class)
class GraphiteTest {
    @Test
    fun contextCanRecordAndSubmit() {
        withTestGraphiteContext { context ->
            context.makeRecorder().use { recorder ->
                recorder.snap().use { recording ->
                    context.insertRecording(recording)
                    context.submit(syncCpu = true)
                }
            }
        }
    }
}

@OptIn(ExperimentalSkikoApi::class)
internal expect fun withTestGraphiteContext(block: (GraphiteContext) -> Unit)
