var {_callCallback, _registerCallback, _releaseCallback, _createLocalCallbackScope, _releaseLocalCallbackScope} = (() => {
    const CB_NULL = {
        callback: () => { throw new RangeError("attempted to call a callback at index 0") },
        data: null
    };
    const CB_UNDEFINED = {
        callback: () => { throw new RangeError("attempted to call a uninitialized callback") },
        data: null
    };


    class Scope {
        constructor() {
            this.callbackMap = [CB_NULL];
        }

        addCallback(callback, data) {
            this.callbackMap.push({callback, data});
            return this.callbackMap.length - 1;
        }

        getCallback(id) {
            return this.callbackMap[id] || CB_UNDEFINED
        }

        deleteCallback(id) {
            this.callbackMap[id] = CB_UNDEFINED
        }

        release() {
            this.callbackMap = null;
        }
    }

    const GLOBAL_SCOPE = new Scope();
    let scope = GLOBAL_SCOPE;

    return {
        _callCallback(callbackId) {
            let callback = scope.getCallback(callbackId);
            try {
                callback.callback();
                return callback.data;
            } catch (e) {
                console.error(e)
            }
        },
        _registerCallback(callback, data = null) {
            return scope.addCallback(callback, data);
        },
        _releaseCallback(callbackId) {
            scope.deleteCallback(callbackId);
        },
        _createLocalCallbackScope() {
            if (scope !== GLOBAL_SCOPE) {
                throw new Error("attempted to overwrite local scope")
            }
            scope = new Scope()
        },
        _releaseLocalCallbackScope() {
            if (scope === GLOBAL_SCOPE) {
                throw new Error("attempted to release global scope")
            }
            scope.release()
            scope = GLOBAL_SCOPE
        },
    }
})();

var wasmSetup = new Promise(function(resolve, reject) {
    Module['onRuntimeInitialized'] = _ => {
        resolve(Module);
    };
});

function onWasmReady(onReady) { wasmSetup.then(onReady); }