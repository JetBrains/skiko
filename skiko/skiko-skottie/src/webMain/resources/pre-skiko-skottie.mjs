import {
    loadedWasm,
    loadSkikoExtension,
    registerSkikoWasmReadyCallback,
} from "./skiko.mjs";

let skottieLoadPromise = null;
const skottieWasm = new URL("./skiko-skottie.wasm", import.meta.url).href;

const ensureSkottieLoaded = () => {
    if (!skottieLoadPromise) {
        skottieLoadPromise = loadSkikoExtension(skottieWasm);
    }
    return skottieLoadPromise;
};

registerSkikoWasmReadyCallback(() => ensureSkottieLoaded());

const isSideModuleLoaded = () => skottieLoadPromise !== null;

export { isSideModuleLoaded };
