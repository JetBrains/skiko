var Module = {};

Module['locateFile'] = (path, scriptDirectory) => {
    return "/wasm/skiko.wasm";
};

var ModulePromised = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        resolve(Module);
    };
});
