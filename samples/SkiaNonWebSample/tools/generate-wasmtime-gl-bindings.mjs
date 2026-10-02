import fs from "node:fs";
import path from "node:path";
import {fileURLToPath} from "node:url";

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const nativeDirectory = path.resolve(scriptDirectory, "../../native-skiko");
const outputPath = path.resolve(
    scriptDirectory,
    "../generated/wasmtime-gl-bindings.inc"
);

const signatures = JSON.parse(fs.readFileSync(
    path.join(nativeDirectory, "native-gl-signatures.json"),
    "utf8"
));
const special = JSON.parse(fs.readFileSync(
    path.join(nativeDirectory, "native-gl-special.json"),
    "utf8"
));
const specialNames = new Set(special.map(({name}) => name));

const interfaceNameOverrides = new Map([
    ["glBindFragDataLocation", "BindFragDataLocation"],
    ["glBindFragDataLocationIndexed", "BindFragDataLocationIndexed"],
    ["glClearDepthf", "ClearDepthf"],
    ["glDepthRangef", "DepthRangef"],
    ["glInsertEventMarkerEXT", "InsertEventMarker"],
    ["glPopGroupMarkerEXT", "PopGroupMarker"],
    ["glPushGroupMarkerEXT", "PushGroupMarker"],
    ["glTextureBarrierNV", "TextureBarrier"],
]);

const normalize = (type) => type.replace(/\s+/g, " ").trim();
const interfaceName = (name) =>
    interfaceNameOverrides.get(name) ?? name.replace(/^gl/, "");

function pointerPointee(type) {
    const pointee = normalize(type)
        .replace(/\s*\*$/, "")
        .trim()
        .replace(/\bGL/g, "GrGL");
    if (pointee === "GrGLvoid") return "uint8_t";
    if (pointee === "const GrGLvoid") return "const uint8_t";
    return pointee;
}

function argumentExpression(entry, parameter, index) {
    const slot = `slots[${index}]`;
    const canonical = normalize(parameter.type);
    const spelled = normalize(parameter.spelledType);

    if (entry.name === "glProgramBinary" && index === 2) {
        return `host->WasmPointer<uint8_t>(caller, U32(${slot}))`;
    }
    if (spelled.includes("*")) {
        if ((spelled.match(/\*/g) ?? []).length !== 1) {
            throw new Error(`${entry.name}: nested pointer must be special`);
        }
        return `host->WasmPointer<${pointerPointee(spelled)}>` +
            `(caller, U32(${slot}))`;
    }

    switch (canonical) {
        case "unsigned char": return `static_cast<uint8_t>(U32(${slot}))`;
        case "unsigned int": return `U32(${slot})`;
        case "int": return `I32(${slot})`;
        case "float": return `${slot}.f32`;
        // These signatures are parsed from the native macOS GL headers, where
        // GLintptr/GLsizeiptr are 64-bit longs. The importing module is wasm32,
        // whose C ABI passes those typedefs as i32. Reading the raw slot as i64
        // also reads unspecified upper bytes and can turn a small buffer size
        // into a huge/negative native value.
        case "long": return `static_cast<int64_t>(I32(${slot}))`;
        case "unsigned long long": return `static_cast<uint64_t>(${slot}.i64)`;
        default:
            throw new Error(
                `${entry.name}: unsupported ${parameter.spelledType} (${parameter.type})`
            );
    }
}

function returnCode(entry, call) {
    switch (normalize(entry.returnType)) {
        case "void": return `${call};`;
        case "GLboolean":
        case "GLenum":
        case "GLuint":
        case "unsigned int":
        case "unsigned char":
            return `slots[0].i32 = static_cast<int32_t>(${call});`;
        case "GLint":
        case "int":
            return `slots[0].i32 = static_cast<int32_t>(${call});`;
        case "GLfloat":
        case "float":
            return `slots[0].f32 = static_cast<float>(${call});`;
        case "GLint64":
        case "long":
        case "GLuint64":
        case "unsigned long long":
            return `slots[0].i64 = static_cast<int64_t>(${call});`;
        default:
            throw new Error(`${entry.name}: unsupported return ${entry.returnType}`);
    }
}

function callback(entry) {
    const argumentsList = entry.parameters
        .map((parameter, index) => argumentExpression(entry, parameter, index))
        .join(",\n            ");
    const call = argumentsList.length === 0
        ? `gl->fFunctions.f${interfaceName(entry.name)}()`
        : `gl->fFunctions.f${interfaceName(entry.name)}(\n            ${argumentsList}\n        )`;

    const recordObject = entry.name === "glCompileShader" ||
        entry.name === "glLinkProgram"
        ? ", U32(slots[0])"
        : "";
    return `wasm_trap_t* Generated_${entry.name}(
    void* environment,
    wasmtime_caller_t* caller,
    wasmtime_val_raw_t* slots,
    size_t slot_count
) {
    (void)caller;
    (void)slots;
    (void)slot_count;
    auto* host = static_cast<SkikoHost*>(environment);
    const GrGLInterface* gl = host->CurrentGL();
    if (gl == nullptr) {
        return SkikoHost::Trap("${entry.name}: no active GL context");
    }

    ${returnCode(entry, call)}
    host->RecordGLCall("${entry.name}"${recordObject});
    return nullptr;
}`;
}

const generic = signatures.functions.filter(({name}) => !specialNames.has(name));
const callbacks = generic.map(callback).join("\n\n");
const table = generic
    .map(({name}) => `    {"${name}", Generated_${name}},`)
    .join("\n");

const output = `// Generated file. Do not edit manually.

namespace {

uint32_t U32(const wasmtime_val_raw_t& value) {
    return static_cast<uint32_t>(value.i32);
}

int32_t I32(const wasmtime_val_raw_t& value) {
    return value.i32;
}

${callbacks}

const GLBinding kGeneratedGLBindings[] = {
${table}
};

} // namespace

const GLBinding* FindGeneratedGLBinding(const std::string& name) {
    for (const GLBinding& binding : kGeneratedGLBindings) {
        if (name == binding.name) {
            return &binding;
        }
    }
    return nullptr;
}
`;

fs.mkdirSync(path.dirname(outputPath), {recursive: true});
fs.writeFileSync(outputPath, output);
console.log(`Generated ${generic.length} Wasmtime GL bindings`);
