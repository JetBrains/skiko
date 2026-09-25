import fs from "node:fs";

const [inputPath, descriptorPath, specialPath] =
    process.argv.slice(2);

if (!inputPath || !descriptorPath || !specialPath) {
    console.error(
        "Usage: node classify-gl-signatures.mjs " +
        "<signatures.json> <descriptors.inc> <special.json>"
    );
    process.exit(1);
}

const input = JSON.parse(
    fs.readFileSync(inputPath, "utf8")
);

const overrides = new Map([
    [
        "glBindFramebuffer",
        "logical framebuffer 0 must map to the canvas FBO"
    ],
    [
        "glGetIntegerv",
        "framebuffer binding must be translated back to logical 0"
    ],

    ["glClientWaitSync", "GLsync handle translation"],
    ["glDeleteSync", "GLsync handle translation"],
    ["glFenceSync", "GLsync handle translation"],
    ["glIsSync", "GLsync handle translation"],
    ["glWaitSync", "GLsync handle translation"],

    ["glMapBuffer", "mapped-buffer staging memory"],
    ["glMapBufferRange", "mapped-buffer staging memory"],
    ["glUnmapBuffer", "mapped-buffer staging memory"],

    [
        "glCompressedTexImage2D",
        "pointer may represent a pixel-buffer offset"
    ],
    [
        "glCompressedTexSubImage2D",
        "pointer may represent a pixel-buffer offset"
    ],
    [
        "glDrawArraysIndirect",
        "pointer represents an indirect-buffer offset"
    ],
    [
        "glDrawElements",
        "pointer may represent an element-buffer offset"
    ],
    [
        "glDrawElementsIndirect",
        "pointer represents an indirect-buffer offset"
    ],
    [
        "glDrawElementsInstanced",
        "pointer may represent an element-buffer offset"
    ],
    [
        "glDrawRangeElements",
        "pointer may represent an element-buffer offset"
    ],
    [
        "glReadPixels",
        "pointer may represent a pixel-buffer offset"
    ],
    [
        "glTexImage2D",
        "pointer may represent a pixel-buffer offset"
    ],
    [
        "glTexSubImage2D",
        "pointer may represent a pixel-buffer offset"
    ],
    [
        "glVertexAttribIPointer",
        "pointer may represent a vertex-buffer offset"
    ],
    [
        "glVertexAttribPointer",
        "pointer may represent a vertex-buffer offset"
    ],
]);

function pointerDepth(type) {
    return [...type.matchAll(/\*/g)].length;
}

function scalarKind(type) {
    const normalized = type
        .replace(/\bconst\b/g, "")
        .replace(/\bvolatile\b/g, "")
        .replace(/\brestrict\b/g, "")
        .replace(/\s+/g, " ")
        .trim();

    const kinds = new Map([
        ["void", "Void"],

        ["GLboolean", "U8"],
        ["unsigned char", "U8"],

        ["GLenum", "U32"],
        ["GLuint", "U32"],
        ["unsigned int", "U32"],

        ["GLint", "I32"],
        ["int", "I32"],

        ["float", "F32"],

        ["long", "I64"],
        ["unsigned long long", "U64"],
    ]);

    return kinds.get(normalized) ?? null;
}

function classify(functionInfo) {
    const overridden = overrides.get(functionInfo.name);

    if (overridden) {
        return {
            ...functionInfo,
            dispatch: "special",
            reason: overridden,
        };
    }

    if (pointerDepth(functionInfo.returnType) > 0) {
        return {
            ...functionInfo,
            dispatch: "special",
            reason: "native pointer return value",
        };
    }

    if (functionInfo.returnType === "GLsync") {
        return {
            ...functionInfo,
            dispatch: "special",
            reason: "native GLsync return value",
        };
    }

    const returnKind =
        scalarKind(functionInfo.returnType);

    if (!returnKind) {
        return {
            ...functionInfo,
            dispatch: "special",
            reason:
                `unsupported return type: ` +
                functionInfo.returnType,
        };
    }

    const argumentKinds = [];

    for (const parameter of functionInfo.parameters) {
        const depth = pointerDepth(parameter.type);

        if (depth > 1) {
            return {
                ...functionInfo,
                dispatch: "special",
                reason:
                    `nested pointer argument: ` +
                    parameter.type,
            };
        }

        if (depth === 1) {
            if (
                parameter.type.includes("__GLsync")
            ) {
                return {
                    ...functionInfo,
                    dispatch: "special",
                    reason: "native GLsync argument",
                };
            }

            argumentKinds.push("WasmPtr");
            continue;
        }

        const kind = scalarKind(parameter.type);

        if (!kind || kind === "Void") {
            return {
                ...functionInfo,
                dispatch: "special",
                reason:
                    `unsupported argument type: ` +
                    parameter.type,
            };
        }

        argumentKinds.push(kind);
    }

    return {
        ...functionInfo,
        dispatch: "ffi",
        returnKind,
        argumentKinds,
    };
}

const classified =
    input.functions.map(classify);

const generic = classified.filter(
    item => item.dispatch === "ffi"
);

const special = classified.filter(
    item => item.dispatch === "special"
);

const includeText = [
    "/* Generated file. Do not edit manually. */",
    "",
    ...generic.map(item => {
        const argumentsText =
            item.argumentKinds
                .map(kind => `FFIKind::${kind}`)
                .join(", ");

        return (
            `    {"${item.name}", ` +
            `FFIKind::${item.returnKind}, ` +
            `{${argumentsText}}},`
        );
    }),
    "",
].join("\n");

fs.writeFileSync(
    descriptorPath,
    includeText
);

fs.writeFileSync(
    specialPath,
    JSON.stringify(special, null, 2) + "\n"
);

console.log(`Total: ${classified.length}`);
console.log(`Generic libffi: ${generic.length}`);
console.log(`Special handlers: ${special.length}`);

for (const item of special) {
    console.log(
        `  ${item.name}: ${item.reason}`
    );
}