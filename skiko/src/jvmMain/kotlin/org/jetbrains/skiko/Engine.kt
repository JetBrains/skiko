package org.jetbrains.skiko

class Engine private constructor() {
    private var api = GraphicsApi.UNKNOWN
    fun currentApi(): GraphicsApi {
        return api
    }

    /**
     * GAG - At the moment always initializes OpenGL.
     * @param osType The current OS type of the device.
     */
    private fun initSuitableGraphicsApi(osType: OSType) {
        when {
            osType === OSType.MAC_OS -> {
                OpenGLApi.get() // for Metal
                instance.api = GraphicsApi.OPENGL
            }
            osType === OSType.LINUX -> {
                OpenGLApi.get() // for Vulkan and OpenGL
                instance.api = GraphicsApi.OPENGL
            }
            osType === OSType.WINDOWS -> {
                OpenGLApi.get() // for Vulkan and OpenGL
                instance.api = GraphicsApi.OPENGL
            }
            else -> {
                OpenGLApi.get() // default
                instance.api = GraphicsApi.OPENGL
            }
        }
    }

    fun render(drawable: Drawable) {
        drawable.updateLayer()
        drawable.redrawLayer()
    }

    companion object {
        private lateinit var instance: Engine
        fun get(): Engine {
            if (!this::instance.isInitialized) {
                instance = Engine()
                instance.initSuitableGraphicsApi(OSType.getCurrent())
            }
            return instance
        }
    }
}
