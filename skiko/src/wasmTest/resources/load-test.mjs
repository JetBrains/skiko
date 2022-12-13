const karmaLoaded = window.__karma__.loaded.bind(window.__karma__);
window.__karma__.loaded = function() {}
wasmSetup.then(() => {
    import('./skiko-kjs-wasm-test.mjs').then((wasmImports) => {
        wasmImports.default.startUnitTests();
        karmaLoaded();
    })
})