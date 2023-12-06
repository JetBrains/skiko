// This file is merged with skiko.js by emcc

const { _callCallback, _registerCallback, _releaseCallback, _createLocalCallbackScope, _releaseLocalCallbackScope } = SkikoCallbacks;

var wasmSetup = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        resolve(Module);
    };
});

function onWasmReady(onReady) { wasmSetup.then(onReady); }