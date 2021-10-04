package org.jetbrains.skiko.native

import  org.jetbrains.skiko.GraphicsApi

// TODO: jvm version uses java properties here.
// there is no properties support available for native.
internal object SkikoProperties {

    val vsyncEnabled: Boolean  = true

    val renderApi: GraphicsApi by lazy {
        GraphicsApi.OPENGL
    }

    private fun parseRenderApi(text: String?): GraphicsApi {
        when(text) {
            "SOFTWARE" -> return GraphicsApi.SOFTWARE
            "OPENGL" -> return GraphicsApi.OPENGL
            else -> return GraphicsApi.OPENGL
        }
    }
}
