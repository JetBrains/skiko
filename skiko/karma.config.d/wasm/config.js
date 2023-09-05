// see https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file


const path = require("path");

config.browserConsoleLogOptions.level = "debug";

const basePath = config.basePath;
const projectPath = path.resolve(basePath, "..", "..", "..", "..", "..");
const wasmPath = path.resolve(projectPath, "build", "out", "link", "Release-wasm-wasm")
const generatedAssetsPath = path.resolve(projectPath, "build", "karma-webpack-out")
const wasmTestsMjs = path.resolve(basePath, "..", "kotlin", "skiko-kjs-wasm-test.mjs")
const staticLoadMJs = path.resolve(basePath, "..", "static", "load.mjs")
const wasmTestsWasm = path.resolve(basePath, "..", "kotlin", "skiko-kjs-wasm-test.wasm")
const wasmTestsLoaderWasm = path.resolve(basePath, "..", "kotlin", "load-test.mjs")

const debug = message => console.log(`[karma-config] ${message}`);

debug(`karma basePath: ${basePath}`);
debug(`karma wasmPath: ${wasmPath}`);
debug(`karma generatedAssetsPath: ${generatedAssetsPath}`);

config.browsers = ["ChromeHeadlessWasmGc"];
config.customLaunchers = {
    ChromeHeadlessWasmGc: {
        base: 'ChromeHeadless',
        flags: ['--js-flags=--experimental-wasm-gc']
    }
};

config.proxies = {
    "/wasm/": wasmPath,
    "/skiko-kjs-wasm-test.uninstantiated.mjs": wasmTestsMjs,
    "/skiko-kjs-wasm-test.wasm": wasmTestsWasm,
    "/resources": path.resolve(basePath, "..", "kotlin")
}

config.preprocessors[wasmTestsLoaderWasm] = ["webpack"];

config.files = config.files.filter((x) => x !== wasmTestsMjs);
config.files = config.files.filter((x) => x !== staticLoadMJs);

config.files = [
    path.resolve(wasmPath, "skiko.js"),
    {pattern: path.resolve(wasmPath, "skiko.wasm"), included: false, served: true, watched: false},
    {pattern: path.resolve(generatedAssetsPath, "**/*"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.png"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.gif"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.ttf"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.txt"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.json"), included: false, served: true, watched: false},
    {pattern: wasmTestsMjs, included: false, served: true, watched: false},
    {pattern: wasmTestsWasm, included: false, served: true, watched: false},
].concat(config.files);

config.files.push({pattern: wasmTestsLoaderWasm, type: 'module'});
