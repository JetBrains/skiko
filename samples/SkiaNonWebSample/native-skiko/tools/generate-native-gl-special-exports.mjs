import fs from "node:fs";

const special = JSON.parse(
    fs.readFileSync(
        "../native-gl-special.json",
        "utf8"
    )
);

const output = [
    "/* Generated file. Do not edit manually. */",
    "",
    ...special.map(
        ({name}) => `    EXPORT_GL(${name}),`
    ),
    "",
].join("\n");

fs.writeFileSync(
    "../native-gl-special-exports.inc",
    output
);

console.log(`Generated ${special.length} special exports`);