export const registeredSkikoExtensions = new Map();
export const loadedSkikoExtensions = new Set();

export const isSideModuleLoaded = (extensionPath) => loadedSkikoExtensions.has(extensionPath);

export const registerSkikoWasmReadyCallback = (extensionPath) => {
    if (!registeredSkikoExtensions.has(extensionPath)) {
        registeredSkikoExtensions.set(extensionPath, null);
    }
};