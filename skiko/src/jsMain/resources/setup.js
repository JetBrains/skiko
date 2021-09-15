var wasmSetup = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        onInit(window);
        resolve(Module);
    };
});

function onWasmReady(onReady) { wasmSetup.then(onReady); }

function onInit(context) {
    Object.keys(Module["asm"]).forEach((key) => {
        console.log(`KEY ${key}`);
        if (key.startsWith("org_jetbrains_skia")) {
            console.log(`BINGO ${key}`);
        }
    })
    //context["org_jetbrains_skia_Paint__1nMake"] = Module["org_jetbrains_skia_Paint__1nMake"];
}
