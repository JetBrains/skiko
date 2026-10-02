import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const nativeGL = require(
    process.env.SKIKO_NATIVE_GL_ADDON ??
    "./native-skiko/build/Release/native_skiko.node"
);

// pre-setup-body.mjs reads this while the generated Skiko module is evaluated.
globalThis.__skikoNativeGL = nativeGL;

const windowWidth = 606;
const windowHeight = 706;
const canvasDescriptions = [
    { id: "c1", width: 600, height: 600 },
    { id: "c2", width: 600, height: 600 },
    { id: "c3", width: 1212, height: 800 },
];

class NodeCanvas {
    constructor(description) {
        Object.assign(this, description);
        this.style = {};
        this.__skikoNativeContext = 0;
    }

    getContext(type) {
        throw new Error(`Unexpected browser canvas context request: ${type}`);
    }
}

globalThis.window = globalThis;
globalThis.self = globalThis;
globalThis.HTMLCanvasElement = NodeCanvas;
globalThis.devicePixelRatio = 1;
Object.defineProperty(globalThis, "navigator", {
    configurable: true,
    value: {
        language: "en-US",
        platform: process.platform,
        userAgent: `Node.js ${process.version}`,
    },
});

const canvases = new Map(
    canvasDescriptions.map((description) => [description.id, new NodeCanvas(description)])
);
const orderedCanvases = canvasDescriptions.map(({ id }) => canvases.get(id));

let title = "Skiko WASM native OpenGL";
nativeGL.createWindow(windowWidth, windowHeight, title);

globalThis.document = {
    get title() {
        return title;
    },
    set title(value) {
        title = String(value);
        nativeGL.setWindowTitle(title);
    },
    getElementById(id) {
        if (id === "description") {
            return {
                set innerHTML(value) {
                    title = String(value);
                    nativeGL.setWindowTitle(title);
                },
            };
        }
        return canvases.get(id) ?? null;
    },
};

let running = true;
let nextAnimationFrameId = 1;
let frameTimer = null;
const animationFrameCallbacks = new Map();
let closeResolve;
const closePromise = new Promise((resolve) => {
    closeResolve = resolve;
});

function stop() {
    if (!running) return;
    running = false;
    if (frameTimer !== null) clearTimeout(frameTimer);
    frameTimer = null;
    animationFrameCallbacks.clear();
    closeResolve();
}

function pollEvents() {
    if (running && !nativeGL.pollEvents()) stop();
}

function scheduleFrame() {
    if (!running || frameTimer !== null) return;
    frameTimer = setTimeout(runFrame, 16);
}

function runFrame() {
    frameTimer = null;
    if (!running) return;
    pollEvents();
    if (!running) return;

    const callbacks = [...animationFrameCallbacks.values()];
    animationFrameCallbacks.clear();
    const timestamp = performance.now();
    for (const callback of callbacks) callback(timestamp);

    nativeGL.present(...orderedCanvases.map((canvas) => canvas.__skikoNativeContext));
    if (animationFrameCallbacks.size > 0) scheduleFrame();
}

globalThis.__skikoRequestAnimationFrame = (callback) => {
    const id = nextAnimationFrameId++;
    animationFrameCallbacks.set(id, callback);
    scheduleFrame();
    return id;
};
globalThis.__skikoCancelAnimationFrame = (id) => {
    animationFrameCallbacks.delete(id);
};

const eventTimer = setInterval(pollEvents, 16);

try {
    await import("./build/wasm/packages/SkiaNonWebSample/kotlin/SkiaNonWebSample.mjs");
    console.log("Skiko native OpenGL demo running. Close the window to exit.");
    await closePromise;
} finally {
    clearInterval(eventTimer);
    if (frameTimer !== null) clearTimeout(frameTimer);
    for (const canvas of orderedCanvases) {
        if (canvas.__skikoNativeContext) {
            nativeGL.destroyContext(canvas.__skikoNativeContext);
        }
    }
    nativeGL.destroyWindow();
}
