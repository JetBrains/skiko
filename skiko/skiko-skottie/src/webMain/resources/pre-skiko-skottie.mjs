import {
    isSideModuleLoaded as isSkikoExtensionLoaded,
    registerSkikoWasmReadyCallback,
} from "./skiko-extensions.mjs";

export const skottieWasm = new URL("./skiko-skottie.wasm", import.meta.url).href;

registerSkikoWasmReadyCallback(skottieWasm);

export const isSideModuleLoaded = () => isSkikoExtensionLoaded(skottieWasm);
