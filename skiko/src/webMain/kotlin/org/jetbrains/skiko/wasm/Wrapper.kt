package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.InternalSkikoApi
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.JsAny
import kotlin.js.Promise

// EmscriptenWebGLContextAttributes combines:
// https://github.com/KhronosGroup/WebGL/blob/main/specs/latest/1.0/webgl.idl#L43-L58
// and its own properties https://github.com/emscripten-core/emscripten/blob/main/src/lib/libwebgl.js
@InternalSkikoApi
external interface EmscriptenWebGLContextAttributes : JsAny {
    val alpha: Int?
    val depth: Int?
    val stencil: Int?
    val antialias: Int?
    val premultipliedAlpha: Int?
    val preserveDrawingBuffer: Int?
    val powerPreference: Int?
    val failIfMajorPerformanceCaveat: Int?
    val majorVersion: Int?
    val enableExtensionsByDefault: Int?
    val explicitSwapControl: Int?
    val renderViaOffscreenBackBuffer: Int?
    val desynchronized: Int?
}

/**
 * Suppresses access to non-portable or deprecated WebGL extensions for the given [canvas].
 *
 * Skia tries to read the unmasked GPU vendor/renderer to work around driver bugs, but since Skiko
 * creates the WebGL context directly (bypassing CanvasKit), the extension stays "supported but not
 * enabled" and modern browsers print a `WebGL: INVALID_ENUM: getParameter` warning to the console.
 *
 * Similarly, Skia may try to enable `WEBGL_polygon_mode` which is non-portable and produces
 * a warning in some browsers.
 *
 * Also, Skia queries `READ_BUFFER` to save/restore state, which triggers a "READ_BUFFER attachment
 * is multisampled" warning in Firefox when MSAA is active. Firefox proactively issues this
 * warning because multisampled buffers cannot be used for `readPixels`, even if the query
 * is only intended for state capture.
 *
 * Hiding these extensions and intercepting noisy parameters is safe and simply removes console clutter.
 *
 * To disable this patching manually, set `canvas.getContext.webGlContextPatched = true` before
 * Skiko initializes the context.
 */
//language=js
internal fun patchWebGlContext(canvas: HTMLCanvasElement): Unit = js("""{
    if (!canvas.getContext.webGlContextPatched) {
        var oldGetContext = canvas.getContext;
        canvas.getContext = function() {
            var gl = oldGetContext.apply(this, arguments);
            if (gl && !gl.webGlContextPatched) {
                var oldGetSupportedExtensions = gl.getSupportedExtensions;
                if (typeof oldGetSupportedExtensions === 'function') {
                    gl.getSupportedExtensions = function() {
                        var exts = oldGetSupportedExtensions.apply(this, arguments);
                        if (exts && exts.filter) {
                            return exts.filter(function(name) {
                                return name !== 'WEBGL_debug_renderer_info' && name !== 'WEBGL_polygon_mode';
                            });
                        }
                        return exts;
                    };
                }
                var oldGetExtension = gl.getExtension;
                if (typeof oldGetExtension === 'function') {
                    gl.getExtension = function(name) {
                        if (name === 'WEBGL_debug_renderer_info' || name === 'WEBGL_polygon_mode') {
                            return null;
                        }
                        return oldGetExtension.apply(this, arguments);
                    };
                }
                var oldGetParameter = gl.getParameter;
                if (typeof oldGetParameter === 'function') {
                    gl.getParameter = function(pname) {
                        // 0x9245 = UNMASKED_VENDOR_WEBGL, 0x9246 = UNMASKED_RENDERER_WEBGL;
                        // both belong to WEBGL_debug_renderer_info, so hide them to avoid the INVALID_ENUM warning
                        if (pname === 0x9245 || pname === 0x9246) {
                            return "";
                        }
                        // 0x0C02 = READ_BUFFER;
                        // hide to avoid "The READ_BUFFER attachment is multisampled" warning in Firefox
                        if (pname === 0x0C02) {
                            var fbo = oldGetParameter.apply(this, [0x8CAA]); // 0x8CAA = READ_FRAMEBUFFER_BINDING
                            return fbo ? 0x8CE0 : 0x0405; // 0x8CE0 = COLOR_ATTACHMENT0, 0x0405 = BACK
                        }
                        return oldGetParameter.apply(this, arguments);
                    };
                }
                gl.webGlContextPatched = true;
            }
            return gl;
        };
        canvas.getContext.webGlContextPatched = true;
    }
}""")

@JsFun(
    """(attr) => ({
        alpha: attr && attr.alpha != null ? attr.alpha : 1,
        depth: attr && attr.depth != null ? attr.depth : 1,
        stencil: attr && attr.stencil != null ? attr.stencil : 8,
        antialias: attr && attr.antialias != null ? attr.antialias : 0,
        premultipliedAlpha: attr && attr.premultipliedAlpha != null ? attr.premultipliedAlpha : 1,
        preserveDrawingBuffer: attr && attr.preserveDrawingBuffer != null ? attr.preserveDrawingBuffer : 0,
        powerPreference: ['default', 'low-power', 'high-performance'][
            attr && attr.powerPreference != null ? attr.powerPreference : 0
        ],
        failIfMajorPerformanceCaveat: attr && attr.failIfMajorPerformanceCaveat != null ? attr.failIfMajorPerformanceCaveat : 0,
        majorVersion: attr && attr.majorVersion != null ? attr.majorVersion : 2,
        enableExtensionsByDefault: attr && attr.enableExtensionsByDefault != null ? attr.enableExtensionsByDefault : 1,
        explicitSwapControl: attr && attr.explicitSwapControl != null ? attr.explicitSwapControl : 0,
        renderViaOffscreenBackBuffer: attr && attr.renderViaOffscreenBackBuffer != null ? attr.renderViaOffscreenBackBuffer : 0,
        desynchronized: attr && attr.desynchronized != null ? attr.desynchronized : 0,
    })"""
)
private external fun contextAttributesWithDefaults(attr: EmscriptenWebGLContextAttributes?): EmscriptenWebGLContextAttributes

internal fun createWebGLContext(
    canvas: HTMLCanvasElement,
    attr: EmscriptenWebGLContextAttributes? = null
): NativePointer {
    patchWebGlContext(canvas)
    return org.jetbrains.skiko.GL.createContext(canvas, contextAttributesWithDefaults(attr))
}

internal expect fun onWasmReady(onReady: () -> Unit)

@InternalSkikoApi
expect val awaitSkiko: Promise<JsAny>