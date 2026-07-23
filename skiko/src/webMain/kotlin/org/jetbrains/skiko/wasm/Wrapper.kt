package org.jetbrains.skiko.wasm

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.InternalSkikoApi
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.JsAny
import kotlin.js.Promise

internal external interface ContextAttributes {
    val alpha: Int?
    val depth: Int?
    val stencil: Int?
    val antialias: Int?
    val premultipliedAlpha: Int?
    val preserveDrawingBuffer: Int?
    val preferLowPowerToHighPerformance: Int?
    val failIfMajorPerformanceCaveat: Int?
    val enableExtensionsByDefault: Int?
    val explicitSwapControl: Int?
    val renderViaOffscreenBackBuffer: Int?
    val majorVersion: Int?
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

internal expect fun createWebGLContext(canvas: HTMLCanvasElement, attr: ContextAttributes? = null): NativePointer

internal expect fun onWasmReady(onReady: () -> Unit)

@InternalSkikoApi
expect val awaitSkiko: Promise<JsAny>