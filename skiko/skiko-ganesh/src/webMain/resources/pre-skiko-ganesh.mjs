import "./js-skiko-reexport-symbols.mjs";
import {
    loadedWasm,
    loadSkikoExtension,
    registerSkikoWasmReadyCallback,
} from "./skiko.mjs";

let ganeshLoadPromise = null;
let ganeshLoaded = false;
const ganeshWasm = new URL("./skiko-ganesh.wasm", import.meta.url).href;

const ensureGaneshLoaded = () => {
    if (!ganeshLoadPromise) {
        ganeshLoadPromise = loadSkikoExtension(ganeshWasm).then(() => {
            ganeshLoaded = true;
        });
    }
    return ganeshLoadPromise;
};

registerSkikoWasmReadyCallback(() => ensureGaneshLoaded());

const isSideModuleLoaded = () => ganeshLoaded;

export { isSideModuleLoaded };