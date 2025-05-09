package org.jetbrains.skiko

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.jvm.JvmStatic

internal class RenderExceptionsHandler {
    companion object {
        private var output: File? = null
        @JvmStatic
        fun throwException(message: String) {
            val exception = RenderException(message)
            val systemInfo = systemInfo()
            Logger.error(exception) { "Render exception\n $systemInfo" }

            if (System.getProperty("skiko.win.exception.logger.enabled") == "true") {
                try {
                    if (output == null) {
                        output = File(
                            "${SkikoProperties.dataPath}/skiko-render-exception-${ProcessHandle.current().pid()}.log"
                        )
                        output!!.parentFile.mkdirs()
                    }
                    output?.appendText("$systemInfo\n")
                    output?.appendText(exceptionToString(exception))
                } catch (t: Throwable) {
                    Logger.error(t) { "Failed to write report" }
                }
            }
            throw exception
        }

        @JvmStatic
        fun logJava(msg: String) {
            Logger.debug { msg }
        }

        @JvmStatic
        fun logJava(msg: Long) {
            Logger.debug { msg.toString() }
        }

        private fun systemInfo(): String {
            return StringBuilder().apply {
                append("When: ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}\n")
                append("Skiko version: ${Version.skiko}\n")
                append("OS: $hostFullName\n")
                append("CPU: ${getNativeCpuInfo()}\n")
                append("Graphics adapters:\n${getNativeGraphicsAdapterInfo()}")
            }.toString()
        }

        private fun exceptionToString(exception: Exception): String {
            return StringBuilder().apply {
                append("Exception message: ${exception.message}\n")
                append("Exception stack trace:\n")
                val stackTrace = exception.stackTrace.filterIndexed { line, _ -> line > 1 }
                for(line in stackTrace) {
                    append("$line\n")
                }
                append("\n\n")
            }.toString()
        }
    }
}

private external fun getNativeGraphicsAdapterInfo(): String
private external fun getNativeCpuInfo(): String
