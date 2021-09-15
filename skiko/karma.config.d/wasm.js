// see https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file

const path = require("path");

config.browserConsoleLogOptions.level = "debug";

const basePath = config.basePath;
const projectPath = path.resolve(basePath, "..", "..", "..", "..", "..");
const wasmPath = path.resolve(projectPath, "build", "wasm")

const debug = message => console.log(`[karma-config] ${message}`); 

debug(`karma basePath: ${basePath}`);
debug(`karma wasmPath: ${wasmPath}`);

config.proxies = {
    "/wasm/": wasmPath
}

config.files = [
    path.resolve(wasmPath, "skiko.js"),
    {pattern: path.resolve(wasmPath, "skiko.wasm"), included: false, served: true, watched: false},
].concat(config.files);


const fs = require("fs");
fs.writeFileSync("KARMA.txt", JSON.stringify(config, null, 4));
