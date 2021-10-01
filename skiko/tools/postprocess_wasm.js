const fs = require("fs");

function postprocess(code) {
    return code.replace(/_org_jetbrains_/g, "org_jetbrains_");
}

function main(args) {
    const filename = args[2];
    fs.writeFileSync(filename, postprocess(fs.readFileSync(filename, "utf-8")));
}

main(process.argv);
