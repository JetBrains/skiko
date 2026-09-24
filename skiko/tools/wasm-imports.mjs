#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import {pathToFileURL} from "node:url";

const CALLBACK_IMPORTS = new Set([
    "_releaseCallback",
    "_callBooleanCallback",
    "_callIntCallback",
    "_callNativePointerCallback",
    "_callVoidCallback",
]);

export function categorizeImport({module, name}) {
    if (module === "wasi_snapshot_preview1") return "wasi";
    if (module === "env" && name.startsWith("gl")) return "gl";
    if (module === "env" && CALLBACK_IMPORTS.has(name)) return "callback";
    return "runtime";
}

export function createImportReport(imports) {
    const groups = {
        gl: [],
        callback: [],
        wasi: [],
        runtime: [],
    };

    for (const entry of imports) {
        groups[categorizeImport(entry)].push(entry);
    }

    for (const entries of Object.values(groups)) {
        entries.sort((left, right) =>
            `${left.module}.${left.name}`.localeCompare(`${right.module}.${right.name}`)
        );
    }

    return {
        total: imports.length,
        counts: Object.fromEntries(
            Object.entries(groups).map(([name, entries]) => [name, entries.length])
        ),
        groups,
    };
}

function usage() {
    console.error("Usage: node skiko/tools/wasm-imports.mjs <skiko.wasm> [--json <report.json>]");
}

function main(args) {
    const wasmPath = args[0];
    if (!wasmPath) {
        usage();
        process.exitCode = 2;
        return;
    }

    const jsonFlag = args.indexOf("--json");
    const jsonPath = jsonFlag >= 0 ? args[jsonFlag + 1] : null;
    if (jsonFlag >= 0 && !jsonPath) {
        usage();
        process.exitCode = 2;
        return;
    }

    const bytes = fs.readFileSync(wasmPath);
    const module = new WebAssembly.Module(bytes);
    const report = createImportReport(WebAssembly.Module.imports(module));

    console.log(`Imports: ${report.total}`);
    console.table(report.counts);
    for (const [group, entries] of Object.entries(report.groups)) {
        console.log(`\n${group} (${entries.length})`);
        for (const entry of entries) {
            console.log(`  ${entry.module}.${entry.name} [${entry.kind}]`);
        }
    }

    if (jsonPath) {
        const resolved = path.resolve(jsonPath);
        fs.mkdirSync(path.dirname(resolved), {recursive: true});
        fs.writeFileSync(resolved, `${JSON.stringify(report, null, 2)}\n`);
        console.log(`\nWrote ${resolved}`);
    }
}

const entryPoint = process.argv[1] ? pathToFileURL(path.resolve(process.argv[1])).href : null;
if (import.meta.url === entryPoint) {
    main(process.argv.slice(2));
}