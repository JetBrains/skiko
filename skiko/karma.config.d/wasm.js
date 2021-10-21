// see https://kotlinlang.org/docs/js-project-setup.html#webpack-configuration-file

const path = require("path");

config.browserConsoleLogOptions.level = "debug";

const basePath = config.basePath;
const projectPath = path.resolve(basePath, "..", "..", "..", "..", "..");
const wasmPath = path.resolve(projectPath, "build", "out", "link", "Release-wasm-wasm")
const generatedAssetsPath = path.resolve(projectPath, "build", "karma-webpack-out")

const debug = message => console.log(`[karma-config] ${message}`);

debug(`karma basePath: ${basePath}`);
debug(`karma wasmPath: ${wasmPath}`);
debug(`karma generatedAssetsPath: ${generatedAssetsPath}`);

config.proxies = {
    "/wasm/": wasmPath
}

config.webpack.output = Object.assign(config.webpack.output || {}, {
    path: generatedAssetsPath
})

config.webpack.module.rules.push(
    {
        test: /\.(ttf|woff|woff2)$/,
        type: 'asset/resource',
        generator: {
            filename: "assets/fonts/[name][ext]"
        }
    },
    {
        test: /\.txt$/,
        type: 'asset/inline'
    },
);

config.files = [
    path.resolve(wasmPath, "skiko.js"),
    {pattern: path.resolve(wasmPath, "skiko.wasm"), included: false, served: true, watched: false},
    {pattern: path.resolve(generatedAssetsPath, "**/*"), included: false, served: true, watched: false},
].concat(config.files);
