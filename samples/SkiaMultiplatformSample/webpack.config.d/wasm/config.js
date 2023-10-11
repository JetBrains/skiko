config.entry = {
    main: [require('path').resolve(__dirname, "kotlin/load.mjs")]
};

config.devtool = undefined; // default is `eval-source-map`
config.mode = 'none'; // default is `development` (for jsBrowserRun).

class IgnoreImportErrorsPlugin {
    apply(compiler) {
        compiler.hooks.done.tap('IgnoreImportErrorsPlugin', (stats) => {
            stats.compilation.errors = stats.compilation.errors.filter((error) => {
                if (error.message.includes("skia")) {
                    return false; // Remove the error
                }
                return true; // Keep the error
            });
        });
    }
}

config.plugins.push(new IgnoreImportErrorsPlugin());