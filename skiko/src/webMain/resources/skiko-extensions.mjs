export const registeredSkikoExtensions = [];
export const loadedSkikoExtensions = new Set();

export const isSideModuleLoaded = (extensionPath) => loadedSkikoExtensions.has(extensionPath);

export const registerSkikoWasmReadyCallback = (extensionPath) => {
    registeredSkikoExtensions.push(extensionPath);
};