#!/usr/bin/env node

import fs from "node:fs";

const [
    signaturesPath = "native-gl-signatures.json",
    specialPath = "native-gl-special.json",
    outputPath = "native-skia-gl-bindings.inc",
] = process.argv.slice(2);

const signatures = JSON.parse(
    fs.readFileSync(signaturesPath, "utf8")
);

const special = JSON.parse(
    fs.readFileSync(specialPath, "utf8")
);

const specialNames = new Set(
    special.map((entry) => entry.name)
);

const interfaceNameOverrides = new Map([
    ["glInsertEventMarkerEXT", "InsertEventMarker"],
    ["glPopGroupMarkerEXT", "PopGroupMarker"],
    ["glPushGroupMarkerEXT", "PushGroupMarker"],
    ["glTextureBarrierNV", "TextureBarrier"],
]);

const genericFunctions = signatures.functions.filter(
    (entry) => !specialNames.has(entry.name)
);

function normalize(type) {
    return type.replace(/\s+/g, " ").trim();
}

function interfaceName(functionName) {
    return interfaceNameOverrides.get(functionName) ??
        functionName.replace(/^gl/, "");
}

function pointerPointee(type) {
    let pointee = normalize(type)
        .replace(/\s*\*$/, "")
        .trim()
        .replace(/\bGL/g, "GrGL");

    if (pointee === "GrGLvoid") {
        return "uint8_t";
    }

    if (pointee === "const GrGLvoid") {
        return "const uint8_t";
    }

    return pointee;
}

function argumentExpression(entry, parameter, index) {
    const argument = `args[${index}]`;

    // Skia's GrGLProgramBinaryFn uses void*, while the platform
    // OpenGL header declares this input as const GLvoid*.
    if (entry.name === "glProgramBinary" && index === 2) {
        return `WasmPointer<uint8_t>(U32(env, ${argument}))`;
    }
    const canonical = normalize(parameter.type);
    const spelled = normalize(parameter.spelledType);

    if (spelled.includes("*")) {
        if ((spelled.match(/\*/g) ?? []).length !== 1) {
            throw new Error(
                `${parameter.name}: nested pointer requires a special handler`
            );
        }

        return `WasmPointer<${pointerPointee(spelled)}>` +
            `(U32(env, ${argument}))`;
    }

    switch (canonical) {
        case "unsigned char":
            return `static_cast<uint8_t>(U32(env, ${argument}))`;

        case "unsigned int":
            return `U32(env, ${argument})`;

        case "int":
            return `I32(env, ${argument})`;

        case "float":
            return `F32(env, ${argument})`;

        case "long":
            return `GeneratedI64(env, ${argument})`;

        case "unsigned long long":
            return `GeneratedU64(env, ${argument})`;

        default:
            throw new Error(
                `${parameter.name}: unsupported argument type ` +
                `${parameter.spelledType} (${parameter.type})`
            );
    }
}

function returnStatement(entry, call) {
    const returnType = normalize(entry.returnType);

    switch (returnType) {
        case "void":
            return `${call};
    return GeneratedUndefined(env);`;

        case "GLboolean":
        case "GLenum":
        case "GLuint":
        case "unsigned int":
        case "unsigned char":
            return `const auto result = ${call};
    napi_value value;
    napi_create_uint32(
        env,
        static_cast<uint32_t>(result),
        &value
    );
    return value;`;

        case "GLint":
        case "int":
            return `const auto result = ${call};
    napi_value value;
    napi_create_int32(
        env,
        static_cast<int32_t>(result),
        &value
    );
    return value;`;

        case "GLfloat":
        case "float":
            return `const auto result = ${call};
    napi_value value;
    napi_create_double(
        env,
        static_cast<double>(result),
        &value
    );
    return value;`;

        case "GLint64":
        case "long":
            return `const auto result = ${call};
    napi_value value;
    napi_create_bigint_int64(
        env,
        static_cast<int64_t>(result),
        &value
    );
    return value;`;

        case "GLuint64":
        case "unsigned long long":
            return `const auto result = ${call};
    napi_value value;
    napi_create_bigint_uint64(
        env,
        static_cast<uint64_t>(result),
        &value
    );
    return value;`;

        default:
            throw new Error(
                `${entry.name}: unsupported return type ${entry.returnType}`
            );
    }
}

function generateCallback(entry) {
    const count = entry.parameters.length;
    const member = interfaceName(entry.name);

    const argumentSetup = count === 0
        ? `size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );`
        : `napi_value args[${count}];
    size_t argc = ${count};

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != ${count}) {
        Throw(
            env,
            "${entry.name} expects ${count} arguments"
        );
        return nullptr;
    }`;

    const argumentsList = entry.parameters
        .map((parameter, index) => argumentExpression(entry, parameter, index))
        .join(",\n            ");

    const call = argumentsList.length === 0
        ? `gl->fFunctions.f${member}()`
        : `gl->fFunctions.f${member}(
            ${argumentsList}
        )`;

    return `napi_value Generated_${entry.name}(
        napi_env env,
        napi_callback_info info) {
    ${argumentSetup}

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    ${returnStatement(entry, call)}
}`;
}

const callbacks = genericFunctions
    .map(generateCallback)
    .join("\n\n");

const table = genericFunctions
    .map(
        (entry) =>
            `    {${JSON.stringify(entry.name)}, Generated_${entry.name}},`
    )
    .join("\n");

const output = `// Generated file. Do not edit manually.

napi_value GeneratedUndefined(napi_env env) {
    napi_value value;
    napi_get_undefined(env, &value);
    return value;
}

int64_t GeneratedI64(napi_env env, napi_value value) {
    int64_t result = 0;
    napi_get_value_int64(env, value, &result);
    return result;
}

uint64_t GeneratedU64(napi_env env, napi_value value) {
    bool lossless = false;
    uint64_t result = 0;

    if (napi_get_value_bigint_uint64(
            env,
            value,
            &result,
            &lossless
        ) == napi_ok) {
        return result;
    }

    double number = 0;
    napi_get_value_double(env, value, &number);
    return static_cast<uint64_t>(number);
}

${callbacks}

struct GeneratedSkiaGLBinding {
    const char* name;
    napi_callback callback;
};

const GeneratedSkiaGLBinding generatedSkiaGLBindings[] = {
${table}
};

napi_callback FindGeneratedSkiaGLCallback(
        const std::string& name) {
    for (const auto& binding : generatedSkiaGLBindings) {
        if (name == binding.name) {
            return binding.callback;
        }
    }

    return nullptr;
}
`;

fs.writeFileSync(outputPath, output);

console.log(`Generated Skia GL bindings: ${genericFunctions.length}`);
console.log(`Special bindings retained: ${specialNames.size}`);
console.log(`Output: ${outputPath}`);