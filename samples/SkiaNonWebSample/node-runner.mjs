import sdlModule from "@kmamal/sdl";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const sdl = sdlModule.default ?? sdlModule;
const nativeGL = require(
    process.env.SKIKO_NATIVE_GL_ADDON ??
    "/Users/dustin.feucht/projects/skiko/samples/SkiaNonWebSample/native-skiko/build/Release/native_skiko.node"
);

// pre-setup-body.mjs reads this while the generated Skiko module is evaluated.
globalThis.__skikoNativeGL = nativeGL;

const windowWidth = 606;
const windowHeight = 706;
const canvasDescriptions = [
    { id: "c1", width: 600, height: 600, x: 0, y: 0, displayWidth: 300, displayHeight: 300 },
    { id: "c2", width: 600, height: 600, x: 306, y: 0, displayWidth: 300, displayHeight: 300 },
    { id: "c3", width: 1212, height: 800, x: 0, y: 306, displayWidth: 606, displayHeight: 400 },
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

let title = "Skiko WASM native OpenGL";
const nativeWindow = sdl.video.createWindow({
    title,
    width: windowWidth,
    height: windowHeight,
    resizable: false,
    accelerated: true,
    vsync: true,
});

globalThis.document = {
    get title() {
        return title;
    },
    set title(value) {
        title = String(value);
        if (!nativeWindow.destroyed) nativeWindow.setTitle(title);
    },
    getElementById(id) {
        if (id === "description") {
            return {
                set innerHTML(value) {
                    title = String(value);
                    if (!nativeWindow.destroyed) nativeWindow.setTitle(title);
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

function present() {
    const output = Buffer.alloc(windowWidth * windowHeight * 4, 255);

    for (const canvas of canvases.values()) {
        if (!canvas.__skikoNativeContext) continue;
        drawBorder(output, windowWidth, windowHeight, canvas.x, canvas.y,
            canvas.displayWidth, canvas.displayHeight);
        const pixels = nativeGL.readContextPixels(canvas.__skikoNativeContext);
        blitScaledFlipped(
            pixels,
            canvas.width,
            canvas.height,
            output,
            windowWidth,
            canvas.x + 1,
            canvas.y + 1,
            canvas.displayWidth - 2,
            canvas.displayHeight - 2
        );
    }

    nativeWindow.render(
        windowWidth,
        windowHeight,
        windowWidth * 4,
        "rgba32",
        output,
        { scaling: "linear" }
    );
}

globalThis.__skikoRequestAnimationFrame = (callback) => setTimeout(() => {
    if (!running) return;
    callback(performance.now());
    present();
}, 16);
globalThis.__skikoCancelAnimationFrame = (handle) => clearTimeout(handle);

await import("./build/wasm/packages/SkiaNonWebSample/kotlin/SkiaNonWebSample.mjs");
console.log("Skiko native OpenGL demo running. Close the window to exit.");
await closePromise;

for (const canvas of canvases.values()) {
    if (canvas.__skikoNativeContext) {
        nativeGL.destroyContext(canvas.__skikoNativeContext);
    }
}
nativeWindow.destroy();

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

function blitScaledFlipped(
    source,
    sourceWidth,
    sourceHeight,
    destination,
    destinationWidth,
    destinationX,
    destinationY,
    displayWidth,
    displayHeight
) {
    for (let y = 0; y < displayHeight; y++) {
        const sourceY = sourceHeight - 1 - Math.floor(y * sourceHeight / displayHeight);
        for (let x = 0; x < displayWidth; x++) {
            const sourceX = Math.floor(x * sourceWidth / displayWidth);
            const sourceOffset = (sourceY * sourceWidth + sourceX) * 4;
            const destinationOffset =
                ((destinationY + y) * destinationWidth + destinationX + x) * 4;
            destination[destinationOffset] = source[sourceOffset];
            destination[destinationOffset + 1] = source[sourceOffset + 1];
            destination[destinationOffset + 2] = source[sourceOffset + 2];
            destination[destinationOffset + 3] = source[sourceOffset + 3];
        }
    }
}
