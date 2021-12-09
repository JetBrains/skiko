package org.jetbrains.skiko

private val notSupportedAdapters by lazy {
    val resource = SkiaLayer::class.java.getResource("/not-supported-adapter.list").readText()
    resource.split(";").map { it.trim() }
}

internal fun isVideoCardSupported(renderApi: GraphicsApi): Boolean {
    return when (renderApi) {
        GraphicsApi.DIRECT3D -> {
            true
        }
        GraphicsApi.OPENGL -> {
            val gl = OpenGLApi.instance
            val adaptersList = notSupportedAdapters.filter { it.startsWith("opengl:") }.map {
                it.replace("opengl:", "")
            }
            var adapter = gl.glGetString(gl.GL_RENDERER).also {
                if (it == null) { return false }
            }
            adaptersList.forEach {
                if (adapter?.startsWith(it) == true) {
                    return false
                }
            }
            true
        }
        else -> true
    }
}

private external fun getNextDirectXAdapter(index: Int = 0): String?
