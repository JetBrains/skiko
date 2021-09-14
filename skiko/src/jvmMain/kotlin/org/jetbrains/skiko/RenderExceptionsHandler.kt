package org.jetbrains.skiko

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.jvm.JvmStatic
import org.jetbrains.skiko.hostFullName

class RenderExceptionsHandler {
    companion object {
        @JvmStatic
        fun logAndThrow(message: String) {
            val exception = Exception(message)
            if (System.getProperty("skiko.renderexceptionlogger.enabled") == "true") {
                writeLog(exception)
            }
            throw exception
        }

        private fun writeLog(exception: Exception) {
            val outputBuilder = StringBuilder().apply {
                append("When: ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}\n")
                append("Skiko version: ${Library.skikoVersion}\n")
                append("OS: $hostFullName\n")
                append("CPU: ${getNativeCpuInfo()}\n")
                append("Graphics adapters:\n${getNativeGraphicsAdapterInfo()}\n")
                append("Exception stack trace:\n")
                val stackTrace = exception.stackTrace.filterIndexed { line, _ -> line > 1 }
                for(line in stackTrace) {
                    append("$line\n")
                }
                append("\n\n")
            }
         
            val output = File("skiko-render-exception.log")
            output.appendText(outputBuilder.toString())
        }

    }
}

fun <T> withExceptionHandler(onException: () -> T, action: () -> T): T {
    try {
        return action()
    } catch (e: Exception) {
        println(e.message)
        return onException()
    }
}

private external fun getNativeGraphicsAdapterInfo(): String
private external fun getNativeCpuInfo(): String
