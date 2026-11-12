// see https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file

const path = require("path");
const fs = require("fs");

config.browserConsoleLogOptions.level = "debug";

config.customLaunchers = Object.assign(config.customLaunchers || {}, {
    ChromeHeadlessWithWebGL: {
        base: 'ChromeHeadless',
        flags: [
            '--no-sandbox',
            '--use-gl=angle',
            '--use-angle=swiftshader',
            '--ignore-gpu-blocklist',
        ]
    }
});
config.browsers = ['ChromeHeadlessWithWebGL'];

const basePath = config.basePath;
const projectPath = path.resolve(basePath, "..", "..", "..", "..");
const wasmPath = path.resolve(basePath, "kotlin")
const skottieWasmPath = path.resolve(basePath, "kotlin", "skiko-skottie.wasm")
const generatedAssetsPath = path.resolve(projectPath, "build", "karma-webpack-out")

const debug = message => console.log(`[karma-config] ${message}`);

debug(`karma basePath: ${basePath}`);
debug(`karma wasmPath: ${wasmPath}`);
debug(`karma generatedAssetsPath: ${generatedAssetsPath}`);

config.proxies = {
    "/wasm/": wasmPath,
    "/resources": path.resolve(basePath, "kotlin"),
    "/skiko-skottie.wasm": skottieWasmPath,
}

config.webpack.output = Object.assign(config.webpack.output || {}, {
    path: generatedAssetsPath
})

config.webpack.module.rules.push(
    {
        test: /\.(ttf|woff|woff2)$/,
        type: 'asset/resource'
    },
    {
        test: /\.(png|jpg|gif)$/,
        type: 'asset/resource'
    },
    {
        test: /\.txt$/,
        type: 'asset/inline'
    },
);

// The test module is an async ES module (skiko.mjs uses top-level await), so tests are registered
// only after wasm is loaded. Delay mocha until then, exactly like an application entry waits for its imports.
config.client.mocha = Object.assign(config.client.mocha || {}, {delay: true});

const testModulePath = config.files.find((file) => typeof file === "string" && file.endsWith(".mjs"));
const testStarterPath = path.resolve(basePath, "static", "start-tests.mjs");
fs.mkdirSync(path.dirname(testStarterPath), {recursive: true});
fs.writeFileSync(testStarterPath,
    `import ${JSON.stringify(path.relative(path.dirname(testStarterPath), testModulePath).split(path.sep).join("/"))};\n` +
    `window.run();\n`
);
config.files.push(testStarterPath);
config.preprocessors[testStarterPath] = ["webpack", "sourcemap"];

config.files = [
    {pattern: path.resolve(wasmPath, "skiko.wasm"), included: false, served: true, watched: false},
    {pattern: path.resolve(generatedAssetsPath, "**/*"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "kotlin", "**/*.png"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "kotlin", "**/*.gif"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "kotlin", "**/*.ttf"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "kotlin", "**/*.txt"), included: false, served: true, watched: false},
    {pattern: path.resolve(basePath, "kotlin", "**/*.json"), included: false, served: true, watched: false},
].concat(config.files);
