import {
    loadedWasm,
    loadSkikoExtension,
    registerSkikoWasmReadyCallback,
} from "./skiko.mjs";

let skottieLoadPromise = null;
let skottieLoaded = false;
const skottieWasm = new URL("./skiko-skottie.wasm", import.meta.url).href;

const ensureSkottieLoaded = () => {
    if (!skottieLoadPromise) {
        skottieLoadPromise = loadSkikoExtension(skottieWasm).then(() => {
            skottieLoaded = true;
        });
    }
    return skottieLoadPromise;
};

registerSkikoWasmReadyCallback(() => ensureSkottieLoaded());

const isSideModuleLoaded = () => skottieLoaded;

export { isSideModuleLoaded };
