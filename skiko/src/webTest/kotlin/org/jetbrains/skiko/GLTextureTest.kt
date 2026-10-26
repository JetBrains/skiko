@file:OptIn(ExperimentalSkikoApi::class, ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko

import kotlinx.browser.document
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skiko.tests.runTest
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContextBase
import org.khronos.webgl.WebGLTexture
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GLTextureTest {
    @Test
    fun registeredTextureIsVisibleToNativeBindingQueries() = runTest {
        withTexture { gl, texture ->
            val textureId = pushTexture(texture)
            try {
                assertTrue(textureId > 0)
                assertEquals(texture, gl.getParameter(WebGLRenderingContext.TEXTURE_BINDING_2D))
                assertEquals(textureId, nativeTextureBinding2D())
                assertEquals(1, textureRegistrationCount(GL, texture))
            } finally {
                unregisterTexture(textureId)
            }
        }
    }

    @Test
    fun registeringTheSameTextureReusesItsId() = runTest {
        withTexture { _, texture ->
            val firstId = pushTexture(texture)
            var secondId = 0
            try {
                secondId = pushTexture(texture)
                assertEquals(firstId, secondId)
                assertEquals(firstId, nativeTextureBinding2D())
                assertEquals(1, textureRegistrationCount(GL, texture))

                unregisterTexture(firstId)
                assertEquals(0, nativeTextureBinding2D())
                assertEquals(0, textureRegistrationCount(GL, texture))
            } finally {
                unregisterTexture(firstId)
                unregisterTexture(secondId)
            }
        }
    }

    @Test
    fun unregisteringReleasesTheNameWithoutDeletingTheTexture() = runTest {
        withTexture { gl, texture ->
            val firstId = pushTexture(texture)
            var replacementId = 0
            try {
                unregisterTexture(firstId)
                assertEquals(0, nativeTextureBinding2D())
                assertEquals(0, textureRegistrationCount(GL, texture))
                assertTrue(gl.isTexture(texture), "unregister must leave the WebGL texture alive")
                assertEquals(texture, gl.getParameter(WebGLRenderingContext.TEXTURE_BINDING_2D))

                replacementId = pushTexture(texture)
                assertTrue(replacementId > 0)
                assertNotEquals(firstId, replacementId)
                assertEquals(replacementId, nativeTextureBinding2D())

                // Releasing an old id again must not invalidate the new registration.
                unregisterTexture(firstId)
                assertEquals(replacementId, nativeTextureBinding2D())
                assertEquals(1, textureRegistrationCount(GL, texture))
            } finally {
                unregisterTexture(firstId)
                unregisterTexture(replacementId)
            }
        }
    }
}

private fun withTexture(block: (WebGLRenderingContextBase, WebGLTexture) -> Unit) {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = 1
    canvas.height = 1
    document.body!!.appendChild(canvas)
    val layer = SkiaLayer()
    try {
        layer.attachTo(canvas)
        val gl = assertNotNull(currentGLContext(GL))
        val previousBinding = gl.getParameter(WebGLRenderingContext.TEXTURE_BINDING_2D) as WebGLTexture?
        val texture = assertNotNull(gl.createTexture())
        try {
            gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture)
            block(gl, texture)
        } finally {
            gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, previousBinding)
            gl.deleteTexture(texture)
        }
    } finally {
        layer.detach()
        canvas.remove()
    }
}

@JsFun("(gl, texture) => gl.textures.filter(entry => entry === texture).length")
private external fun textureRegistrationCount(gl: GLInterface, texture: WebGLTexture): Int

// Query through the native glGetIntegerv import, rather than emulating it in JavaScript.
@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE")
@ExternalSymbolName("org_jetbrains_skiko_tests_GLTextureTest__1nGetTextureBinding2D")
private external fun nativeTextureBinding2D(): Int
