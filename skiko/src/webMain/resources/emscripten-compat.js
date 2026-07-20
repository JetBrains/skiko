// Emscripten build-time utility stubs for browser compatibility.
// These functions are used by libwebgl.preprocessed.js and libwebgl2.preprocessed.js
// at the tail end of each file. They are normally provided by Emscripten's Node.js
// build tooling (utility.mjs, modules.mjs, etc.) but are not available in the browser.

function range(n) {
    return Array.from({length: n}, (_, i) => i);
}

function assert(cond, msg) {
    if (!cond) {
        console.warn('Assertion failed: ' + (msg || ''));
    }
}

var decoratorSuffixes = [
    '__sig',
    '__deps',
    '__proxy',
    '__asm',
    '__inline',
    '__postset',
    '__docs',
    '__nothrow',
    '__noleakcheck',
    '__internal',
    '__user',
    '__async',
    '__i53abi',
];

function isDecorator(ident) {
    return decoratorSuffixes.some(function(suffix) { return ident.endsWith(suffix); });
}

var LibraryManager = { library: {} };

function autoAddDeps(lib, symbol) {}

function addToLibrary(lib) {
    Object.assign(LibraryManager.library, lib);
}

function error(msg) {
    console.error(msg);
}

// --- Emscripten runtime globals ---
// These are referenced by GL function bodies in the preprocessed files.
// They must be initialized after wasm memory is available.

var HEAP8, HEAPU8, HEAP16, HEAPU16, HEAP32, HEAPU32, HEAPF32, HEAPF64;

function updateMemoryViews(buffer) {
    HEAP8 = new Int8Array(buffer);
    HEAPU8 = new Uint8Array(buffer);
    HEAP16 = new Int16Array(buffer);
    HEAPU16 = new Uint16Array(buffer);
    HEAP32 = new Int32Array(buffer);
    HEAPU32 = new Uint32Array(buffer);
    HEAPF32 = new Float32Array(buffer);
    HEAPF64 = new Float64Array(buffer);
}

function UTF8ToString(ptr, maxBytesToRead) {
    if (!ptr) return '';
    var end = ptr;
    if (maxBytesToRead === undefined) {
        while (HEAPU8[end]) ++end;
    } else {
        end = Math.min(ptr + maxBytesToRead, HEAPU8.length);
        while (end > ptr && !HEAPU8[end - 1]) --end;
    }
    return new TextDecoder().decode(HEAPU8.subarray(ptr, end));
}

function stringToNewUTF8(str) {
    var bytes = new TextEncoder().encode(str);
    var ptr = wasmExports.malloc(bytes.length + 1);
    // malloc may grow memory, detaching the old ArrayBuffer — refresh views
    updateMemoryViews(wasmExports.memory.buffer);
    HEAPU8.set(bytes, ptr);
    HEAPU8[ptr + bytes.length] = 0;
    return ptr;
}

function stringToUTF8(str, outPtr, maxBytesToWrite) {
    var bytes = new TextEncoder().encode(str);
    var len = Math.min(bytes.length, maxBytesToWrite - 1);
    HEAPU8.set(bytes.subarray(0, len), outPtr);
    HEAPU8[outPtr + len] = 0;
    return len;
}

var wasmTable = null;
var Module = {};

// Pre-declare all $-prefixed symbols from the preprocessed files as module-scoped
// variables. In Emscripten's build system, $-prefixed keys are extracted from the
// library object and turned into top-level variables. Since skiko.mjs is an ES module,
// globalThis assignments are not visible as bare variable references — we must use var.
var tempFixedLengthArray;
var miniTempWebGLFloatBuffers;
var miniTempWebGLIntBuffers;
var heapObjectForWebGLType;
var toTypedArrayIndex;
var webgl_enable_WEBGL_multi_draw;
var webgl_enable_EXT_polygon_offset_clamp;
var webgl_enable_EXT_clip_control;
var webgl_enable_WEBGL_polygon_mode;
var getEmscriptenSupportedExtensions;
var GLctx;
var webglGetUniformLocation;
var webglPrepExtensions;
var webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance;
var webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance;
var _glDrawElements;
var _emscriptenGL;
var webglBufferSubData;
var webglGetExtensions;
var computeUnpackAlignedImageSize;
var colorChannelsInGlTextureFormat;
var emscriptenWebGLGetTexPixelData;
var emscriptenWebGLGet;
var emscriptenWebGLGetUniform;
var webglGetProgramUniformLocation;
var webglPrepareUniformLocationsBeforeFirstUse;
var emscriptenWebGLGetVertexAttrib;
var webglGetUniformBlockIndex;
var webglGetLeftBracePos;

function writeI53ToI64(ptr, num) {
    HEAPU32[((ptr)>>2)] = num;
    HEAPU32[(((ptr)+(4))>>2)] = (num - HEAPU32[((ptr)>>2)])/4294967296;
}

function readI53FromI64(ptr) {
    return HEAPU32[((ptr)>>2)] + HEAP32[(((ptr)+(4))>>2)] * 4294967296;
}

// Map of module-scoped variable setters for $-prefixed symbols.
// We cannot use globalThis because skiko.mjs is an ES module and bare
// variable references resolve to module scope, not the global object.
var _emscriptenGlobalSetters = {
    'tempFixedLengthArray': function(v) { tempFixedLengthArray = v; },
    'miniTempWebGLFloatBuffers': function(v) { miniTempWebGLFloatBuffers = v; },
    'miniTempWebGLIntBuffers': function(v) { miniTempWebGLIntBuffers = v; },
    'heapObjectForWebGLType': function(v) { heapObjectForWebGLType = v; },
    'toTypedArrayIndex': function(v) { toTypedArrayIndex = v; },
    'webgl_enable_WEBGL_multi_draw': function(v) { webgl_enable_WEBGL_multi_draw = v; },
    'webgl_enable_EXT_polygon_offset_clamp': function(v) { webgl_enable_EXT_polygon_offset_clamp = v; },
    'webgl_enable_EXT_clip_control': function(v) { webgl_enable_EXT_clip_control = v; },
    'webgl_enable_WEBGL_polygon_mode': function(v) { webgl_enable_WEBGL_polygon_mode = v; },
    'getEmscriptenSupportedExtensions': function(v) { getEmscriptenSupportedExtensions = v; },
    'GLctx': function(v) { GLctx = v; },
    'GL': function(v) { _emscriptenGL = v; },
    'webglGetUniformLocation': function(v) { webglGetUniformLocation = v; },
    'webglPrepExtensions': function(v) { webglPrepExtensions = v; },
    'webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance': function(v) { webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance = v; },
    'webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance': function(v) { webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance = v; },
    '_glDrawElements': function(v) { _glDrawElements = v; },
    'webglBufferSubData': function(v) { webglBufferSubData = v; },
    'webglGetExtensions': function(v) { webglGetExtensions = v; },
    'computeUnpackAlignedImageSize': function(v) { computeUnpackAlignedImageSize = v; },
    'colorChannelsInGlTextureFormat': function(v) { colorChannelsInGlTextureFormat = v; },
    'emscriptenWebGLGetTexPixelData': function(v) { emscriptenWebGLGetTexPixelData = v; },
    'emscriptenWebGLGet': function(v) { emscriptenWebGLGet = v; },
    'emscriptenWebGLGetUniform': function(v) { emscriptenWebGLGetUniform = v; },
    'webglGetProgramUniformLocation': function(v) { webglGetProgramUniformLocation = v; },
    'webglPrepareUniformLocationsBeforeFirstUse': function(v) { webglPrepareUniformLocationsBeforeFirstUse = v; },
    'emscriptenWebGLGetVertexAttrib': function(v) { emscriptenWebGLGetVertexAttrib = v; },
    'webglGetUniformBlockIndex': function(v) { webglGetUniformBlockIndex = v; },
    'webglGetLeftBracePos': function(v) { webglGetLeftBracePos = v; },
};

// Extract $-prefixed symbols from LibraryManager.library into module-scoped
// variables and run their __postset initializers. In Emscripten's build system,
// keys starting with $ are turned into top-level variables.
function extractEmscriptenGlobals() {
    var lib = LibraryManager.library;
    for (var key in lib) {
        if (key.startsWith('$') && !key.includes('__')) {
            var globalName = key.substring(1);
            var setter = _emscriptenGlobalSetters[globalName];
            if (setter) {
                setter(lib[key]);
            }
        }
    }
    // Run __postset initializers
    for (var key in lib) {
        if (key.endsWith('__postset') && key.startsWith('$')) {
            var code = lib[key];
            if (typeof code === 'string') {
                eval(code);
            }
        }
    }

    // Wire up _glDrawElements — Emscripten's underscore-prefixed reference
    // used by glDrawRangeElements in libwebgl2.preprocessed.js.
    // The function is stored as 'glDrawElements' (or 'emscripten_glDrawElements'
    // after recordGLProcAddressGet renames it) in the library.
    var drawElem = lib['glDrawElements'];
    if (typeof drawElem === 'string') {
        drawElem = lib[drawElem];
    }
    if (typeof drawElem === 'function') {
        _glDrawElements = drawElem;
    }
}
