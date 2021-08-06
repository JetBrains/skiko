package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.LinuxOpenGLRedrawer

enum class GraphicsApi {
    UNKNOWN, SOFTWARE, OPENGL, DIRECT3D, VULKAN, METAL
}

enum class GpuPriority(val value: String) {
    Auto("auto"), Integrated("integrated"), Discrete("discrete");

    companion object {
        fun parse(value: String?): GpuPriority? = GpuPriority.values().find { it.value == value }
    }
}

private val notSupportedAdapters by lazy {
    val resource = SkiaLayer::class.java.getResource("/not-supported-adapter.list").readText()
    resource.split(";").map { it.trim() }
}

internal fun isVideoCardSupported(renderApi: GraphicsApi): Boolean {
    return when (renderApi) {
        GraphicsApi.DIRECT3D -> {
            val adaptersList = notSupportedAdapters.filter { it.startsWith("directx:") }.map {
                it.replace("directx:", "")
            }

            var index = 0
            var adapter = getNextDirectXAdapter(index++)
            while (adapter != null) {
                var isFound = true
                adaptersList.forEach checkList@{
                    if (adapter!!.startsWith(it)) {
                        isFound = false
                        return@checkList
                    }
                }
                if (isFound) {
                    break
                }
                adapter = getNextDirectXAdapter(index++)
            }
            (adapter != null)
        }
        GraphicsApi.OPENGL -> {
            val gl = OpenGLApi.instance
            val adaptersList = notSupportedAdapters.filter { it.startsWith("opengl:") }.map {
                it.replace("opengl:", "")
            }
            var adapter = gl.glGetString(gl.GL_RENDERER)
            adaptersList.forEach {
                if (adapter.startsWith(it)) {
                    return false
                }
            }
            true
        }
        else -> true
    }
}

private external fun getNextDirectXAdapter(index: Int = 0): String?
