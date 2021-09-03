console.log("INIT WASM");


var Module = {}

Module['locateFile'] = (path, scriptDirectory) => {
    return "/wasm/skiko.wasm";
}

var ModulePromised = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        resolve(Module);
        return;
        const fib = Module.cwrap('fib', 'number', ['number']);
        console.log(fib(12));
        console.log(fib(13));
        console.log(fib(14));
    };
});
