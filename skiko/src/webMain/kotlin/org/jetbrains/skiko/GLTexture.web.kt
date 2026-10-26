@file:OptIn(ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko

import kotlin.js.JsAny

@JsFun(
    """(gl, texture) => {
        const existingHandle = texture.name;
        if (existingHandle && gl.textures[existingHandle] === texture) {
            return existingHandle;
        }
        const textureHandle = gl.getNewId(gl.textures);
        // Emscripten uses this name to translate WebGL objects back to numeric GL ids.
        texture.name = textureHandle;
        gl.textures[textureHandle] = texture;
        return textureHandle;
    }"""
)
private external fun pushTexture(gl: GLInterface, texture: JsAny): Int

@JsFun(
    """(gl, textureId) => {
        const texture = gl.textures[textureId];
        if (texture && texture.name === textureId) {
            texture.name = 0;
        }
        gl.textures[textureId] = null;
    }"""
)
private external fun unregisterTexture(gl: GLInterface, textureId: Int)

/**
 * Registers an externally-created WebGLTexture in Emscripten's GL texture table.
 *
 * The returned id can be passed to Skia GL APIs that expect a numeric texture id, such as
 * [org.jetbrains.skia.BackendTexture.makeGL]. The texture must belong to the same WebGL context
 * that Skiko is using.
 *
 * Registering a texture that is already in the table returns the same id; it does not create
 * a separate registration or transfer ownership.
 *
 * This function sets the texture's Emscripten name and creates its table entry. If the returned
 * id is passed to a Skia API that takes ownership of the texture, Skia will delete the GL texture
 * through Emscripten and the table entry will be cleared there. If ownership is not transferred
 * to Skia, call [unregisterTexture] when the id is no longer needed to avoid leaking the table entry.
 */
@ExperimentalSkikoApi
fun pushTexture(texture: JsAny): Int = pushTexture(GL, texture)

/**
 * Removes a texture table entry previously created with [pushTexture].
 *
 * This does not delete the underlying WebGLTexture; it clears its Emscripten name and releases
 * Skiko/Emscripten's numeric id mapping. Use it only when the id was not handed to a Skia API
 * that takes ownership of the texture.
 */
@ExperimentalSkikoApi
fun unregisterTexture(textureId: Int): Unit = unregisterTexture(GL, textureId)
