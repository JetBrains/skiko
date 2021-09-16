var wasmSetup = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        resolve(Module);
    };
});

function onWasmReady(onReady) { wasmSetup.then(onReady); }

var GetWebGLContext = function(canvas, attrs) {
    function get(obj, attr, defaultValue) {
        if (obj && obj.hasOwnProperty(attr)) {
            return obj[attr];
        }
        return defaultValue;
    }

    if (!canvas) {
        throw 'null canvas passed into makeWebGLContext';
    }
    var contextAttributes = {
        'alpha': get(attrs, 'alpha', 1),
        'depth': get(attrs, 'depth', 1),
        'stencil': get(attrs, 'stencil', 8),
        'antialias': get(attrs, 'antialias', 0),
        'premultipliedAlpha': get(attrs, 'premultipliedAlpha', 1),
        'preserveDrawingBuffer': get(attrs, 'preserveDrawingBuffer', 0),
        'preferLowPowerToHighPerformance': get(attrs, 'preferLowPowerToHighPerformance', 0),
        'failIfMajorPerformanceCaveat': get(attrs, 'failIfMajorPerformanceCaveat', 0),
        'enableExtensionsByDefault': get(attrs, 'enableExtensionsByDefault', 1),
        'explicitSwapControl': get(attrs, 'explicitSwapControl', 0),
        'renderViaOffscreenBackBuffer': get(attrs, 'renderViaOffscreenBackBuffer', 0),
    };

    if (attrs && attrs['majorVersion']) {
        contextAttributes['majorVersion'] = attrs['majorVersion']
    } else {
        // Default to WebGL 2 if available and not specified.
        contextAttributes['majorVersion'] = (typeof WebGL2RenderingContext !== 'undefined') ? 2 : 1;
    }

    // This check is from the emscripten version
    if (contextAttributes['explicitSwapControl']) {
        throw 'explicitSwapControl is not supported';
    }

    // Creates a WebGL context and sets it to be the current context.
    // These functions are defined in emscripten's library_webgl.js
    var handle = GL.createContext(canvas, contextAttributes);
    if (!handle) {
        return 0;
    }
    GL.makeContextCurrent(handle);
    return handle;
}