import fs from "node:fs";
import {spawnSync} from "node:child_process";

const [wasmPath, outputPath] = process.argv.slice(2);

if (!wasmPath || !outputPath) {
    console.error(
        "Usage: node generate-gl-signatures.mjs " +
        "<skiko.wasm> <output.json>"
    );
    process.exit(1);
}

const wasmBytes = fs.readFileSync(wasmPath);
const wasmModule = new WebAssembly.Module(wasmBytes);

const requiredNames = new Set(
    WebAssembly.Module.imports(wasmModule)
        .filter(({module, name, kind}) =>
            module === "env" &&
            kind === "function" &&
            name.startsWith("gl")
        )
        .map(({name}) => name)
);

const clang = spawnSync(
    "clang",
    [
        "-x", "c",
        "-Xclang", "-ast-dump=json",
        "-fsyntax-only",
        "-"
    ],
    {
        input: [
            "#include <OpenGL/gl3.h>",
            "#include <OpenGL/glext.h>",

            "extern void glGetInternalformativ(" +
            "GLenum target, " +
            "GLenum internalformat, " +
            "GLenum pname, " +
            "GLsizei count, " +
            "GLint *params);",

            "extern void glTexStorage2D(" +
            "GLenum target, " +
            "GLsizei levels, " +
            "GLenum internalformat, " +
            "GLsizei width, " +
            "GLsizei height);",

            "",
        ].join("\n"),
        encoding: "utf8",
        maxBuffer: 256 * 1024 * 1024,
    }
);

if (clang.status !== 0) {
    process.stderr.write(clang.stderr);
    process.exit(clang.status ?? 1);
}

const ast = JSON.parse(clang.stdout);
const declarations = new Map();

const typedefDeclarations = new Map();

const typedefToFunction = new Map(
    [...requiredNames].map(name => [
        `PFN${name.toUpperCase()}PROC`,
        name,
    ])
);

function typeName(type) {
    return (
        type?.desugaredQualType ??
        type?.qualType ??
        null
    );
}

function parseFunctionPointerType(functionName, type) {
    const match = type.match(
        /^(.*?)\s*\(\s*\*\s*\)\s*\((.*)\)$/
    );

    if (!match) {
        return null;
    }

    const returnType = match[1].trim();
    const parameterText = match[2].trim();

    const parameterTypes =
        parameterText === "" || parameterText === "void"
            ? []
            : parameterText
                .split(",")
                .map(value => value.trim());

    return {
        name: functionName,
        returnType,
        functionType: type,
        parameters: parameterTypes.map(type => ({
            name: null,
            type,
            spelledType: type,
        })),
        source: "function-pointer typedef",
    };
}

function visit(node) {
    if (
        node?.kind === "FunctionDecl" &&
        requiredNames.has(node.name)
    ) {
        const functionType =
            node.type?.qualType ?? "";

        const returnSeparator =
            functionType.indexOf("(");

        const returnType =
            returnSeparator >= 0
                ? functionType.slice(0, returnSeparator)
                : functionType;

        const parameters = (node.inner ?? [])
            .filter(child => child.kind === "ParmVarDecl")
            .map(parameter => ({
                name: parameter.name ?? null,
                type: typeName(parameter.type),
                spelledType:
                    parameter.type?.qualType ?? null,
            }));

        declarations.set(node.name, {
            name: node.name,
            returnType,
            functionType,
            parameters,
        });
    }
    if (
        node?.kind === "TypedefDecl" &&
        typedefToFunction.has(node.name)
    ) {
        const functionName =
            typedefToFunction.get(node.name);

        const typedefType = typeName(node.type);

        if (typedefType) {
            const declaration =
                parseFunctionPointerType(
                    functionName,
                    typedefType
                );

            if (declaration) {
                typedefDeclarations.set(
                    functionName,
                    declaration
                );
            }
        }
    }

    for (const child of node?.inner ?? []) {
        visit(child);
    }
}

visit(ast);

for (const name of requiredNames) {
    if (!declarations.has(name)) {
        const typedefDeclaration =
            typedefDeclarations.get(name);

        if (typedefDeclaration) {
            declarations.set(
                name,
                typedefDeclaration
            );
        }
    }
}

const functions = [...requiredNames]
    .sort()
    .map(name =>
            declarations.get(name) ?? {
                name,
                unresolved: true,
            }
    );

const unresolved = functions
    .filter(({unresolved}) => unresolved)
    .map(({name}) => name);

const output = {
    generatedFrom: {
        wasm: wasmPath,
        headers: [
            "OpenGL/gl3.h",
            "OpenGL/glext.h",
        ],
    },
    requiredFunctionCount: requiredNames.size,
    resolvedFunctionCount:
        requiredNames.size - unresolved.length,
    unresolved,
    functions,
};

fs.writeFileSync(
    outputPath,
    JSON.stringify(output, null, 2) + "\n"
);

console.log(
    `Required: ${output.requiredFunctionCount}`
);

console.log(
    `Resolved: ${output.resolvedFunctionCount}`
);

console.log(
    `Unresolved: ${unresolved.length}`
);

for (const name of unresolved) {
    console.log(`  ${name}`);
}