package org.jetbrains.skiko

import java.io.File
import java.text.SimpleDateFormat
import java.lang.management.ManagementFactory
import java.lang.StringBuffer
import java.util.Date
import kotlin.jvm.JvmStatic
import org.jetbrains.skia.impl.isJava8

internal class RenderExceptionsHandler {
    companion object {
        private var output: File? = null
        @JvmStatic
        fun logAndThrow(message: String) {
            if (output == null) {
                output = File(
                    "${Library.cacheRoot}/skiko-render-exception-${if(isJava8) {
                        ManagementFactory.getRuntimeMXBean().getName().let { it.substring(0, it.indexOf('@')) }
                    } else { ProcessHandle.current().pid().toString() } }.log"
                )
            }
            val exception = RenderException(message)
            if (System.getProperty("skiko.win.exception.logger.enabled") == "true") {
                writeLog(exception)
            }
            throw exception
        }

        private fun writeLog(exception: Exception) {
            val outputBuilder = StringBuilder().apply {
                append("When: ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}\n")
                append("Skiko version: ${Version.skiko}\n")
                append("OS: $hostFullName\n")
                append("CPU: ${getNativeCpuInfo()}\n")
                append("Graphics adapters:\n${getNativeGraphicsAdapterInfo()}\n")
                append("Exception message: ${exception.message}\n")
                append("Exception stack trace:\n")
                val stackTrace = exception.stackTrace.filterIndexed { line, _ -> line > 1 }
                for(line in stackTrace) {
                    append("$line\n")
                }
                append("\n\n")
            }
            output?.appendText(outputBuilder.toString())
        }
    }
}

private external fun getNativeGraphicsAdapterInfo(): String
private external fun getNativeCpuInfo(): String
