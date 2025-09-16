
// This file is merged with skiko.js and skiko.mjs by emcc
// It used by setup.js and setup.mjs (see in the same directory)

const CB_NULL = {
    callback: () => { throw new RangeError("attempted to call a callback at NULL") },
    data: null
};
const CB_UNDEFINED = {
    callback: () => { throw new RangeError("attempted to call an uninitialized callback") },
    data: null
};

class Scope {
    constructor() {
        this.nextId = 1;
        this.callbackMap = new Map();
        this.callbackMap.set(0, CB_NULL);
    }

    addCallback(callback, data) {
        let id = this.nextId++;
        this.callbackMap.set(id, {callback, data});
        return id;
    }

    getCallback(id) {
        return this.callbackMap.get(id) || CB_UNDEFINED;
    }

    deleteCallback(id) {
        this.callbackMap.delete(id);
    }

    release() {
        this.callbackMap = null;
    }
}


const GLOBAL_SCOPE = new Scope();
let scope = GLOBAL_SCOPE;



function _callCallback(callbackId, global = false) {
    let callback = (global ? GLOBAL_SCOPE : scope).getCallback(callbackId);
    try {
        callback.callback();
        return callback.data;
    } catch (e) {
        console.error(e)
    }
}

function _registerCallback(callback, data = null, global = false) {
    return (global ? GLOBAL_SCOPE : scope).addCallback(callback, data);
}

function _releaseCallback(callbackId, global = false) {
    (global ? GLOBAL_SCOPE : scope).deleteCallback(callbackId);
}

function _createLocalCallbackScope() {
    if (scope !== GLOBAL_SCOPE) {
        throw new Error("attempted to overwrite local scope")
    }
    scope = new Scope()
}

function _releaseLocalCallbackScope() {
    if (scope === GLOBAL_SCOPE) {
        throw new Error("attempted to release global scope")
    }
    scope.release()
    scope = GLOBAL_SCOPE
}

export const skikoApi = {
    _callCallback: _callCallback,
    _registerCallback: _registerCallback,
    _releaseCallback: _releaseCallback,
    _createLocalCallbackScope: _createLocalCallbackScope,
    _releaseLocalCallbackScope: _releaseLocalCallbackScope
}