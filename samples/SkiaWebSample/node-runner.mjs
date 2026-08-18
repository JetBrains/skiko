import nodeGles from "node-gles-webgl2";
import sdlModule from "@kmamal/sdl";

const sdl = sdlModule.default ?? sdlModule;
const browserCanvases = [
    { id: "c1", width: 600, height: 600, x: 0, y: 0, displayWidth: 300, displayHeight: 300 },
    { id: "c2", width: 600, height: 600, x: 306, y: 0, displayWidth: 300, displayHeight: 300 },
    { id: "c3", width: 1212, height: 800, x: 0, y: 306, displayWidth: 606, displayHeight: 400 },
];

class NodeCanvas {
    constructor(width, height, id = "canvas") {
        this.width = width;
        this.height = height;
        this.id = id;
        this.style = {};
        this._gl = null;
    }

    getContext(type, attributes = {}) {
        if (type !== "webgl2") return null;
        if (!this._gl) {
            this._gl = createWebGL2Context(this.width, this.height, attributes);
            this._gl.canvas = this;
        }
        return this._gl;
    }
}

function createWebGL2Context(width, height, attributes) {
    const gl = nodeGles.createWebGLRenderingContext({
        width,
        height,
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

    if (typeof gl.texImage3D !== "function" || typeof gl.readBuffer !== "function") {
        throw new Error("The created context does not expose the WebGL2 API");
    }

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
    const canvases = new Map(
        browserCanvases.map((item) => [item.id, Object.assign(new NodeCanvas(item.width, item.height, item.id), item)])
    );
    for (const canvas of canvases.values()) {
        canvas.getContext.webGlContextPatched = true;
    }

    let title = "Skiko WASM Node Window";
    let nativeWindow;
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

    nativeWindow = sdl.video.createWindow({
        title,
        width: 606,
        height: 706,
        resizable: false,
        accelerated: true,
        vsync: true,
    });

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
            presentWindow(nativeWindow, canvases.values());
        }, 16);
    };
    globalThis.__skikoCancelAnimationFrame = (handle) => clearTimeout(handle);

    await import("./build/wasm/packages/SkiaWebSample/kotlin/SkiaWebSample.mjs");
    console.log("Skiko Node windowed demo running. Close the native window to exit.");
    await closePromise;
    destroyCanvases(canvases.values());
    process.exit(0);
}

function readCanvasPixels(canvas) {
    const gl = canvas.getContext("webgl2");
    gl.finish();
    const pixels = new Uint8Array(canvas.width * canvas.height * 4);
    gl.readPixels(0, 0, canvas.width, canvas.height, gl.RGBA, gl.UNSIGNED_BYTE, pixels);
    return pixels;
}

function presentWindow(nativeWindow, canvases) {
    const windowWidth = 606;
    const windowHeight = 706;
    const output = Buffer.alloc(windowWidth * windowHeight * 4, 255);

    for (const canvas of canvases) {
        drawBorder(output, windowWidth, windowHeight, canvas.x, canvas.y, canvas.displayWidth, canvas.displayHeight);
        const src = readCanvasPixels(canvas);
        blitScaledFlipped(src, canvas.width, canvas.height, output, windowWidth, canvas.x + 1, canvas.y + 1, canvas.displayWidth - 2, canvas.displayHeight - 2);
    }

    nativeWindow.render(windowWidth, windowHeight, windowWidth * 4, "rgba32", output, { scaling: "linear" });
}

function drawBorder(output, windowWidth, windowHeight, x, y, width, height) {
    for (let yy = y; yy < y + height && yy < windowHeight; yy++) {
        for (let xx = x; xx < x + width && xx < windowWidth; xx++) {
            if (yy !== y && yy !== y + height - 1 && xx !== x && xx !== x + width - 1) continue;
            const offset = (yy * windowWidth + xx) * 4;
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

function destroyCanvases(canvases) {
    for (const canvas of canvases) {
        canvas.getContext("webgl2")?.__rawGL?.destroy?.();
    }
}
