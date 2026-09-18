import nodeGles from "node-gles-webgl2";
import sdlModule from "@kmamal/sdl";

const sdl = sdlModule.default ?? sdlModule;
const useCpuCopy = process.env.SKIKO_NODE_CPU_COPY === "1";
const debugNodeRunner = process.env.SKIKO_NODE_DEBUG === "1";
const windowWidth = 606;
const windowHeight = 706;
const browserCanvases = [
    { id: "c1", width: 600, height: 600, x: 0, y: 0, displayWidth: 300, displayHeight: 300 },
    { id: "c2", width: 600, height: 600, x: 306, y: 0, displayWidth: 300, displayHeight: 300 },
    { id: "c3", width: 1212, height: 800, x: 0, y: 306, displayWidth: 606, displayHeight: 400 },
];

class NodeCanvas {
    constructor(width, height, id, renderer) {
        this.width = width;
        this.height = height;
        this.id = id;
        this.style = {};
        this.renderer = renderer;
        this._gl = null;
    }

    getContext(type, attributes = {}) {
        if (type !== "webgl2") return null;
        if (!this._gl) {
            debugLog(`creating webgl2 context for ${this.id}`);
            this._gl = this.renderer.createCanvasContext(this, attributes);
            debugLog(`created webgl2 context for ${this.id}`);
            this._gl.canvas = this;
        }
        return this._gl;
    }
}

function debugLog(message) {
    if (debugNodeRunner) {
        console.error(`[skiko-node] ${message}`);
    }
}

class CpuCopyRenderer {
    constructor(nativeWindow) {
        this.nativeWindow = nativeWindow;
    }

    createCanvasContext(canvas, attributes) {
        const gl = nodeGles.createWebGLRenderingContext({
            width: canvas.width,
            height: canvas.height,
            majorVersion: 3,
            minorVersion: 0,
            webGLCompatibility: true,
            preserveDrawingBuffer: true,
            alpha: attributes.alpha !== 0,
            depth: attributes.depth !== 0,
            stencil: attributes.stencil !== 0,
            antialias: attributes.antialias !== 0,
            premultipliedAlpha: attributes.premultipliedAlpha !== 0,
        });

        if (!gl) {
            throw new Error("Failed to create a WebGL2 context with node-gles-webgl2");
        }

        validateWebGL2Context(gl);

        if (typeof gl.getContextAttributes !== "function") {
            gl.getContextAttributes = () => ({
                alpha: attributes.alpha !== 0,
                depth: attributes.depth !== 0,
                stencil: attributes.stencil !== 0,
                antialias: attributes.antialias !== 0,
                premultipliedAlpha: attributes.premultipliedAlpha !== 0,
                preserveDrawingBuffer: true,
                preferLowPowerToHighPerformance: false,
                failIfMajorPerformanceCaveat: false,
            });
        }

        return adaptWebGL2Context(gl);
    }

    present(canvases) {
        const output = Buffer.alloc(windowWidth * windowHeight * 4, 255);

        for (const canvas of canvases) {
            drawBorder(output, windowWidth, windowHeight, canvas.x, canvas.y, canvas.displayWidth, canvas.displayHeight);
            const src = readCanvasPixels(canvas);
            blitScaledFlipped(src, canvas.width, canvas.height, output, windowWidth, canvas.x + 1, canvas.y + 1, canvas.displayWidth - 2, canvas.displayHeight - 2);
        }

        this.nativeWindow.render(windowWidth, windowHeight, windowWidth * 4, "rgba32", output, { scaling: "linear" });
    }

    destroy(canvases) {
        for (const canvas of canvases) {
            canvas.getContext("webgl2")?.__rawGL?.destroy?.();
        }
    }
}

class NativeWindowRenderer {
    constructor(nativeWindow) {
        this.nativeWindow = nativeWindow;
        this.logicalWidth = windowWidth;
        this.logicalHeight = windowHeight;
        this.pixelWidth = nativeWindow.pixelWidth ?? windowWidth;
        this.pixelHeight = nativeWindow.pixelHeight ?? windowHeight;
        this.scaleX = this.pixelWidth / this.logicalWidth;
        this.scaleY = this.pixelHeight / this.logicalHeight;
        this.windowGL = nodeGles.createWebGLRenderingContext({
            width: this.pixelWidth,
            height: this.pixelHeight,
            majorVersion: 3,
            minorVersion: 0,
            webGLCompatibility: true,
            preserveDrawingBuffer: true,
            alpha: true,
            depth: true,
            stencil: true,
            antialias: false,
            premultipliedAlpha: true,
            window: nativeWindow.native,
        });
        if (!this.windowGL) {
            throw new Error("Failed to create a native OpenGL context for the SDL window");
        }
        validateWebGL2Context(this.windowGL);
        this.canvasStates = new Map();
        this.compositor = createCompositor(this.windowGL);
    }

    createCanvasContext(canvas, attributes) {
        const gl = nodeGles.createWebGLRenderingContext({
            width: canvas.width,
            height: canvas.height,
            majorVersion: 3,
            minorVersion: 0,
            webGLCompatibility: true,
            preserveDrawingBuffer: true,
            alpha: attributes.alpha !== 0,
            depth: attributes.depth !== 0,
            stencil: attributes.stencil !== 0,
            antialias: attributes.antialias !== 0,
            premultipliedAlpha: attributes.premultipliedAlpha !== 0,
            shareContext: this.windowGL,
        });
        if (!gl) {
            throw new Error(`Failed to create a shared WebGL2 context for ${canvas.id}`);
        }
        validateWebGL2Context(gl);

        const state = createCanvasRenderTarget(gl, canvas);
        state.gl = gl;
        this.canvasStates.set(canvas, state);
        const context = createVirtualCanvasContext(gl, canvas, state, attributes);
        return adaptWebGL2Context(context);
    }

    present(canvases) {
        for (const state of this.canvasStates.values()) {
            state.gl.flush();
        }

        const gl = this.windowGL;
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        gl.viewport(0, 0, this.pixelWidth, this.pixelHeight);
        gl.disable(gl.DEPTH_TEST);
        gl.disable(gl.STENCIL_TEST);
        gl.disable(gl.SCISSOR_TEST);
        gl.disable(gl.BLEND);
        gl.colorMask(true, true, true, true);
        gl.clearColor(1, 1, 1, 1);
        gl.clear(gl.COLOR_BUFFER_BIT);

        for (const canvas of canvases) {
            const state = this.canvasStates.get(canvas);
            if (!state) continue;
            this.compositor.drawCanvas(state.texture, scaledRect(canvas, this.scaleX, this.scaleY), this.pixelWidth, this.pixelHeight);
            this.compositor.drawBorder(scaledRect(canvas, this.scaleX, this.scaleY), this.pixelWidth, this.pixelHeight);
        }

        gl.flush();
        if (typeof gl.swap !== "function") {
            throw new Error("The native OpenGL context does not expose swap(); cannot present without copying");
        }
        gl.swap();
    }

    destroy() {
        for (const state of this.canvasStates.values()) {
            state.gl.deleteFramebuffer(state.framebuffer);
            state.gl.deleteRenderbuffer(state.depthStencil);
            state.gl.deleteTexture(state.texture);
            state.gl.destroy?.();
            state.gl.dispose?.();
        }
        this.canvasStates.clear();
        this.compositor.destroy();
        this.windowGL.destroy?.();
        this.windowGL.dispose?.();
    }
}

function validateWebGL2Context(gl) {
    if (typeof gl.texImage3D !== "function" || typeof gl.readBuffer !== "function") {
        throw new Error("The native window context does not expose the WebGL2 API required by Skiko WASM");
    }
}

function createCanvasRenderTarget(gl, canvas) {
    const texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, canvas.width, canvas.height, 0, gl.RGBA, gl.UNSIGNED_BYTE, null);

    const depthStencil = gl.createRenderbuffer();
    gl.bindRenderbuffer(gl.RENDERBUFFER, depthStencil);
    gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH24_STENCIL8, canvas.width, canvas.height);

    const framebuffer = gl.createFramebuffer();
    gl.bindFramebuffer(gl.FRAMEBUFFER, framebuffer);
    gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, texture, 0);
    gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_STENCIL_ATTACHMENT, gl.RENDERBUFFER, depthStencil);
    const status = gl.checkFramebufferStatus(gl.FRAMEBUFFER);
    if (status !== gl.FRAMEBUFFER_COMPLETE) {
        throw new Error(`Failed to create framebuffer for ${canvas.id}: status 0x${status.toString(16)}`);
    }

    gl.viewport(0, 0, canvas.width, canvas.height);
    gl.clearColor(1, 1, 1, 1);
    gl.clear(gl.COLOR_BUFFER_BIT);
    gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    gl.bindRenderbuffer(gl.RENDERBUFFER, null);
    gl.bindTexture(gl.TEXTURE_2D, null);

    return {
        texture,
        framebuffer,
        depthStencil,
        logicalDrawFramebuffer: null,
        logicalReadFramebuffer: null,
    };
}

function createVirtualCanvasContext(gl, canvas, state, attributes) {
    const framebufferTargets = new Set([gl.FRAMEBUFFER, gl.DRAW_FRAMEBUFFER, gl.READ_FRAMEBUFFER]);
    const framebufferBindingParameters = new Set([gl.FRAMEBUFFER_BINDING, gl.DRAW_FRAMEBUFFER_BINDING, gl.READ_FRAMEBUFFER_BINDING]);
    const drawCalls = new Set([
        "clear",
        "clearBufferfi",
        "clearBufferfv",
        "clearBufferiv",
        "clearBufferuiv",
        "drawArrays",
        "drawArraysInstanced",
        "drawElements",
        "drawElementsInstanced",
        "drawRangeElements",
    ]);

    const isDefaultFramebuffer = (framebuffer) => framebuffer == null || framebuffer === 0;

    const bindLogicalFramebuffer = (target, framebuffer) => {
        if (!framebufferTargets.has(target) || !isDefaultFramebuffer(framebuffer)) {
            gl.bindFramebuffer(target, framebuffer);
            if (target === gl.FRAMEBUFFER || target === gl.DRAW_FRAMEBUFFER) state.logicalDrawFramebuffer = framebuffer;
            if (target === gl.FRAMEBUFFER || target === gl.READ_FRAMEBUFFER) state.logicalReadFramebuffer = framebuffer;
            return;
        }

        gl.bindFramebuffer(target, state.framebuffer);
        if (target === gl.FRAMEBUFFER || target === gl.DRAW_FRAMEBUFFER) state.logicalDrawFramebuffer = null;
        if (target === gl.FRAMEBUFFER || target === gl.READ_FRAMEBUFFER) state.logicalReadFramebuffer = null;
    };

    const ensureCanvasDrawTarget = () => {
        if (state.logicalDrawFramebuffer === null) {
            gl.bindFramebuffer(gl.DRAW_FRAMEBUFFER ?? gl.FRAMEBUFFER, state.framebuffer);
        }
    };

    const ensureCanvasReadTarget = () => {
        if (state.logicalReadFramebuffer === null) {
            gl.bindFramebuffer(gl.READ_FRAMEBUFFER ?? gl.FRAMEBUFFER, state.framebuffer);
        }
    };

    return new Proxy(gl, {
        get(target, prop, receiver) {
            if (prop === "__rawGL") return target;
            if (prop === "__canvasState") return state;
            if (prop === "canvas") return canvas;
            if (prop === "drawingBufferWidth") return canvas.width;
            if (prop === "drawingBufferHeight") return canvas.height;
            if (prop === "getContextAttributes") {
                return () => ({
                    alpha: attributes.alpha !== 0,
                    depth: attributes.depth !== 0,
                    stencil: attributes.stencil !== 0,
                    antialias: attributes.antialias !== 0,
                    premultipliedAlpha: attributes.premultipliedAlpha !== 0,
                    preserveDrawingBuffer: true,
                    preferLowPowerToHighPerformance: false,
                    failIfMajorPerformanceCaveat: false,
                });
            }
            if (prop === "getParameter") {
                return (pname) => {
                    if (framebufferBindingParameters.has(pname)) {
                        if (pname === gl.READ_FRAMEBUFFER_BINDING) return state.logicalReadFramebuffer;
                        return state.logicalDrawFramebuffer;
                    }
                    return target.getParameter(pname);
                };
            }

            const value = Reflect.get(target, prop, receiver);
            if (typeof value !== "function") return value;

            if (prop === "bindFramebuffer") {
                return bindLogicalFramebuffer;
            }
            if (prop === "readPixels") {
                return (...args) => {
                    ensureCanvasReadTarget();
                    return value.apply(target, args);
                };
            }
            if (drawCalls.has(prop)) {
                return (...args) => {
                    ensureCanvasDrawTarget();
                    return value.apply(target, args);
                };
            }
            return (...args) => value.apply(target, args);
        },
    });
}

function createCompositor(gl) {
    const vertexShader = compileShader(gl, gl.VERTEX_SHADER, `#version 300 es
in vec2 aPosition;
in vec2 aTexCoord;
out vec2 vTexCoord;

void main() {
    vTexCoord = aTexCoord;
    gl_Position = vec4(aPosition, 0.0, 1.0);
}
`);
    const fragmentShader = compileShader(gl, gl.FRAGMENT_SHADER, `#version 300 es
precision mediump float;

uniform sampler2D uTexture;
uniform vec4 uColor;
uniform int uMode;
in vec2 vTexCoord;
out vec4 outColor;

void main() {
    outColor = uMode == 0 ? texture(uTexture, vTexCoord) : uColor;
}
`);
    const program = gl.createProgram();
    gl.attachShader(program, vertexShader);
    gl.attachShader(program, fragmentShader);
    gl.linkProgram(program);
    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
        throw new Error(`Failed to link compositor program: ${gl.getProgramInfoLog(program)}`);
    }
    gl.deleteShader(vertexShader);
    gl.deleteShader(fragmentShader);

    const buffer = gl.createBuffer();
    const positionLocation = gl.getAttribLocation(program, "aPosition");
    const texCoordLocation = gl.getAttribLocation(program, "aTexCoord");
    const textureLocation = gl.getUniformLocation(program, "uTexture");
    const colorLocation = gl.getUniformLocation(program, "uColor");
    const modeLocation = gl.getUniformLocation(program, "uMode");

    const drawRect = (rect, framebufferWidth, framebufferHeight, mode, texture = null) => {
        const left = rect.x / framebufferWidth * 2 - 1;
        const right = (rect.x + rect.width) / framebufferWidth * 2 - 1;
        const top = 1 - rect.y / framebufferHeight * 2;
        const bottom = 1 - (rect.y + rect.height) / framebufferHeight * 2;
        const vertices = new Float32Array([
            left, bottom, 0, 0,
            right, bottom, 1, 0,
            left, top, 0, 1,
            right, top, 1, 1,
        ]);

        gl.useProgram(program);
        gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
        gl.bufferData(gl.ARRAY_BUFFER, vertices, gl.STREAM_DRAW);
        gl.enableVertexAttribArray(positionLocation);
        gl.vertexAttribPointer(positionLocation, 2, gl.FLOAT, false, 16, 0);
        gl.enableVertexAttribArray(texCoordLocation);
        gl.vertexAttribPointer(texCoordLocation, 2, gl.FLOAT, false, 16, 8);
        gl.uniform1i(modeLocation, mode);
        if (mode === 0) {
            gl.activeTexture(gl.TEXTURE0);
            gl.bindTexture(gl.TEXTURE_2D, texture);
            gl.uniform1i(textureLocation, 0);
        } else {
            gl.uniform4f(colorLocation, 0, 0, 0, 1);
        }
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
    };

    return {
        drawCanvas(texture, rect, framebufferWidth, framebufferHeight) {
            drawRect(rect, framebufferWidth, framebufferHeight, 0, texture);
        },
        drawBorder(rect, framebufferWidth, framebufferHeight) {
            drawRect({ x: rect.x, y: rect.y, width: rect.width, height: 1 }, framebufferWidth, framebufferHeight, 1);
            drawRect({ x: rect.x, y: rect.y + rect.height - 1, width: rect.width, height: 1 }, framebufferWidth, framebufferHeight, 1);
            drawRect({ x: rect.x, y: rect.y, width: 1, height: rect.height }, framebufferWidth, framebufferHeight, 1);
            drawRect({ x: rect.x + rect.width - 1, y: rect.y, width: 1, height: rect.height }, framebufferWidth, framebufferHeight, 1);
        },
        destroy() {
            gl.deleteBuffer(buffer);
            gl.deleteProgram(program);
        },
    };
}

function compileShader(gl, type, source) {
    const shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        throw new Error(`Failed to compile compositor shader: ${gl.getShaderInfoLog(shader)}`);
    }
    return shader;
}

function scaledRect(canvas, scaleX, scaleY) {
    return {
        x: Math.round(canvas.x * scaleX),
        y: Math.round(canvas.y * scaleY),
        width: Math.round(canvas.displayWidth * scaleX),
        height: Math.round(canvas.displayHeight * scaleY),
    };
}

class WebGLObjectHandle {
    constructor(handle) {
        this.__webglHandle = handle;
    }
}

class WebGLBuffer extends WebGLObjectHandle {}
class WebGLFramebuffer extends WebGLObjectHandle {}
class WebGLProgram extends WebGLObjectHandle {}
class WebGLQuery extends WebGLObjectHandle {}
class WebGLRenderbuffer extends WebGLObjectHandle {}
class WebGLSampler extends WebGLObjectHandle {}
class WebGLShader extends WebGLObjectHandle {}
class WebGLSync extends WebGLObjectHandle {}
class WebGLTexture extends WebGLObjectHandle {}
class WebGLTransformFeedback extends WebGLObjectHandle {}
class WebGLVertexArrayObject extends WebGLObjectHandle {}
class WebGLRenderingContext {}
class WebGL2RenderingContext extends WebGLRenderingContext {}

Object.assign(globalThis, {
    WebGLBuffer,
    WebGLFramebuffer,
    WebGLProgram,
    WebGLQuery,
    WebGLRenderbuffer,
    WebGLSampler,
    WebGLShader,
    WebGLSync,
    WebGLTexture,
    WebGLTransformFeedback,
    WebGLVertexArrayObject,
    WebGLRenderingContext,
    WebGL2RenderingContext,
});

function adaptWebGL2Context(gl) {
    const wrappers = new Map();
    const createTypes = {
        createBuffer: WebGLBuffer,
        createFramebuffer: WebGLFramebuffer,
        createProgram: WebGLProgram,
        createQuery: WebGLQuery,
        createRenderbuffer: WebGLRenderbuffer,
        createSampler: WebGLSampler,
        createShader: WebGLShader,
        createSync: WebGLSync,
        createTexture: WebGLTexture,
        createTransformFeedback: WebGLTransformFeedback,
        createVertexArray: WebGLVertexArrayObject,
    };
    const bindingTypes = new Map([
        [0x8069, WebGLTexture],
        [0x8514, WebGLTexture],
        [0x8894, WebGLBuffer],
        [0x8895, WebGLBuffer],
        [0x8b8d, WebGLProgram],
        [0x8ca6, WebGLFramebuffer],
        [0x8caa, WebGLFramebuffer],
        [0x8ca7, WebGLRenderbuffer],
        [0x85b5, WebGLVertexArrayObject],
        [0x8e25, WebGLTransformFeedback],
        [0x8919, WebGLBuffer],
        [0x8f36, WebGLBuffer],
        [0x8f37, WebGLBuffer],
    ]);

    const wrap = (handle, Type) => {
        if (handle == null || handle === 0) return null;
        if (handle instanceof WebGLObjectHandle) return handle;
        if (typeof handle !== "number") return handle;
        const key = `${Type.name}:${handle}`;
        let object = wrappers.get(key);
        if (!object) {
            object = new Type(handle);
            wrappers.set(key, object);
        }
        return object;
    };
    const unwrap = (value) => value instanceof WebGLObjectHandle ? value.__webglHandle : value;

    return new Proxy(gl, {
        get(target, prop, receiver) {
            if (prop === "__rawGL") return target;

            const value = Reflect.get(target, prop, receiver);
            if (typeof value !== "function") return value;

            if (prop in createTypes) {
                return (...args) => wrap(value.apply(target, args.map(unwrap)), createTypes[prop]);
            }

            if (prop === "getParameter") {
                return (pname) => {
                    const result = value.call(target, pname);
                    const Type = bindingTypes.get(pname);
                    return Type ? wrap(result, Type) : result;
                };
            }

            if (prop === "getVertexAttrib") {
                return (index, pname) => {
                    const result = value.call(target, index, pname);
                    return pname === 0x889f ? wrap(result, WebGLBuffer) : result;
                };
            }

            return (...args) => value.apply(target, args.map(unwrap));
        },
    });
}

globalThis.window = globalThis;
globalThis.self = globalThis;
globalThis.HTMLCanvasElement = NodeCanvas;
Object.defineProperty(globalThis, "navigator", {
    configurable: true,
    value: {
        language: "en-US",
        platform: process.platform,
        userAgent: `Node.js ${process.version}`,
    },
});
globalThis.devicePixelRatio = 1;

await runWindowedDemo();

async function runWindowedDemo() {
    let title = "Skiko WASM Node Window";
    let nativeWindow = null;
    let renderer = null;

    if (!useCpuCopy) {
        nativeWindow = sdl.video.createWindow({
            title,
            width: windowWidth,
            height: windowHeight,
            resizable: false,
            opengl: true,
        });
        try {
            renderer = new NativeWindowRenderer(nativeWindow);
        } catch (error) {
            nativeWindow.destroy();
            throw new Error(
                "Native OpenGL presentation failed. Re-run `npm install` in samples/SkiaWebSample to patch and rebuild node-gles-webgl2, or set SKIKO_NODE_CPU_COPY=1 for the old readPixels path.",
                { cause: error }
            );
        }
    }

    if (!renderer) {
        nativeWindow = sdl.video.createWindow({
            title,
            width: windowWidth,
            height: windowHeight,
            resizable: false,
            accelerated: true,
            vsync: true,
        });
        renderer = new CpuCopyRenderer(nativeWindow);
    }

    const canvases = new Map(
        browserCanvases.map((item) => [item.id, Object.assign(new NodeCanvas(item.width, item.height, item.id, renderer), item)])
    );
    for (const canvas of canvases.values()) {
        canvas.getContext.webGlContextPatched = true;
    }

    globalThis.document = {
        get title() {
            return title;
        },
        set title(value) {
            title = String(value);
            if (nativeWindow && !nativeWindow.destroyed) {
                nativeWindow.setTitle(title);
            }
        },
        getElementById(id) {
            if (id === "description") {
                return {
                    set innerHTML(value) {
                        title = String(value);
                        if (nativeWindow && !nativeWindow.destroyed) {
                            nativeWindow.setTitle(title);
                        }
                    },
                };
            }
            return canvases.get(id) ?? null;
        },
    };

    let running = true;
    let closeResolve;
    const closePromise = new Promise((resolve) => {
        closeResolve = resolve;
    });

    nativeWindow.on("close", () => {
        running = false;
        closeResolve();
    });

    globalThis.__skikoRequestAnimationFrame = (callback) => {
        return setTimeout(() => {
            if (!running) return;
            callback(performance.now());
            renderer.present(canvases.values());
        }, 16);
    };
    globalThis.__skikoCancelAnimationFrame = (handle) => clearTimeout(handle);

    await import("./build/wasm/packages/SkiaWebSample/kotlin/SkiaWebSample.mjs");
    console.log("Skiko Node windowed demo running. Close the native window to exit.");
    await closePromise;
    renderer.destroy(canvases.values());
    process.exit(0);
}

function readCanvasPixels(canvas) {
    const gl = canvas.getContext("webgl2");
    gl.finish();
    const pixels = Buffer.allocUnsafe(canvas.width * canvas.height * 4);
    gl.readPixels(0, 0, canvas.width, canvas.height, gl.RGBA, gl.UNSIGNED_BYTE, pixels);
    return pixels;
}

function drawBorder(output, outputWidth, outputHeight, x, y, width, height) {
    for (let yy = y; yy < y + height && yy < outputHeight; yy++) {
        for (let xx = x; xx < x + width && xx < outputWidth; xx++) {
            if (yy !== y && yy !== y + height - 1 && xx !== x && xx !== x + width - 1) continue;
            const offset = (yy * outputWidth + xx) * 4;
            output[offset] = 0;
            output[offset + 1] = 0;
            output[offset + 2] = 0;
            output[offset + 3] = 255;
        }
    }
}

function blitScaledFlipped(src, srcWidth, srcHeight, dst, dstWidth, dstX, dstY, dstDisplayWidth, dstDisplayHeight) {
    for (let y = 0; y < dstDisplayHeight; y++) {
        const srcY = srcHeight - 1 - Math.floor(y * srcHeight / dstDisplayHeight);
        for (let x = 0; x < dstDisplayWidth; x++) {
            const srcX = Math.floor(x * srcWidth / dstDisplayWidth);
            const srcOffset = (srcY * srcWidth + srcX) * 4;
            const dstOffset = ((dstY + y) * dstWidth + dstX + x) * 4;
            dst[dstOffset] = src[srcOffset];
            dst[dstOffset + 1] = src[srcOffset + 1];
            dst[dstOffset + 2] = src[srcOffset + 2];
            dst[dstOffset + 3] = src[srcOffset + 3];
        }
    }
}
