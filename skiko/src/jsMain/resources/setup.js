var {_callCallback, _registerCallback, _releaseCallback} = (() => {
    let nextId = 1;
    let callbackMap = new Map();

    return {
        _callCallback(callbackId, ...args) {
            let callback = callbackMap.get(callbackId);
            if (!callback) throw new RangeError("Callback not found")
            try {
                callback.callback(...args);
                return callback.data;
            } catch (e) {
                console.error(e)
            }
        },
        _registerCallback(callback, data = null) {
            if (nextId >= (1 << 30)) throw new RangeError("Too many callbacks")
            callbackMap.set(nextId, { callback, data });
            return nextId++;
        },
        _releaseCallback(callbackId) {
            let callback = callbackMap.get(callbackId);
            callbackMap.delete(callbackId);
            return callback;
        },
    }
})();

var wasmSetup = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        resolve(Module);
    };
});

function onWasmReady(onReady) { wasmSetup.then(onReady); }