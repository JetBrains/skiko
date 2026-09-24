import assert from "node:assert/strict";
import test from "node:test";

import {categorizeImport, createImportReport} from "./wasm-imports.mjs";

test("categorizes the Skiko host import families", () => {
    assert.equal(categorizeImport({module: "env", name: "glClear"}), "gl");
    assert.equal(categorizeImport({module: "env", name: "_callVoidCallback"}), "callback");
    assert.equal(categorizeImport({module: "wasi_snapshot_preview1", name: "fd_write"}), "wasi");
    assert.equal(categorizeImport({module: "env", name: "setjmp"}), "runtime");
});

test("reports counts and stable alphabetical groups", () => {
    const report = createImportReport([
        {module: "env", name: "glClearColor", kind: "function"},
        {module: "env", name: "glClear", kind: "function"},
        {module: "env", name: "longjmp", kind: "function"},
    ]);

    assert.deepEqual(report.counts, {gl: 2, callback: 0, wasi: 0, runtime: 1});
    assert.deepEqual(report.groups.gl.map(({name}) => name), ["glClear", "glClearColor"]);
});