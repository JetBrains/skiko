config.entry = {
    main: [require('path').resolve(__dirname, "kotlin/load.mjs")]
};

config.devtool = undefined; // default is `eval-source-map`
config.mode = 'none'; // default is `development` (for jsBrowserRun).