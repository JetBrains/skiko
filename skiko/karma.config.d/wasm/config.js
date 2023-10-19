// see https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file


const path = require("path");
const os = require("os")

config.browserConsoleLogOptions.level = "debug";

const basePath = config.basePath;
const projectPath = path.resolve(basePath, "..", "..", "..", "..", "..");
const generatedAssetsPath = path.resolve(projectPath, "build", "karma-webpack-out")
const wasmTestsMjs = path.resolve(basePath, "..", "kotlin", "skiko-kjs-wasm-test.mjs")
const staticLoadMJs = path.resolve(basePath, "..", "static", "load.mjs")
const wasmTestsLoaderWasm = path.resolve(basePath, "..", "kotlin", "load-test.mjs")

const debug = message => console.log(`[karma-config] ${message}`);

debug(`karma basePath: ${basePath}`);
debug(`karma generatedAssetsPath: ${generatedAssetsPath}`);

config.proxies["/resources"] = path.resolve(basePath, "..", "kotlin");

config.preprocessors[wasmTestsLoaderWasm] = ["webpack"];

// WA to make assets visible
// https://github.com/codymikol/karma-webpack/issues/498#issuecomment-790040818
const output = {
    path: path.join(os.tmpdir(), '_karma_webpack_') + Math.floor(Math.random() * 1000000),
}

config.files = config.files.filter((x) => x !== wasmTestsMjs);
config.files = config.files.filter((x) => x !== staticLoadMJs);

config.files = [
    {pattern: path.resolve(generatedAssetsPath, "**/*"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.png"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.gif"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.ttf"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.txt"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "..", "kotlin", "**/*.json"), included: false, served: true, watched: false},
    {pattern: `${output.path}/**/*`, included: false, served: true, watched: false},
].concat(config.files);

config.files.push(wasmTestsLoaderWasm);

// WA to make assets visible
// https://github.com/codymikol/karma-webpack/issues/498#issuecomment-790040818
config.webpack.output = output;

config.webpack.resolve = {
    alias: {
        skia: false,
        GL: false,
        SkikoCallbacks: false
    },
};

// New opcodes only in Canary
config.browsers = ["ChromeCanaryHeadlessWasmGc"];
config.customLaunchers = {
    ChromeCanaryHeadlessWasmGc: {
        base: 'ChromeCanaryHeadless',
        flags: ['--js-flags=--experimental-wasm-gc']
    }
};