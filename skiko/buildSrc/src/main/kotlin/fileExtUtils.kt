val OS.dynamicLibExt: String
    get() = when (this) {
        OS.Linux -> ".so"
        OS.Windows -> ".dll"
        OS.MacOS, OS.IOS -> ".dylib"
        OS.Wasm -> ".wasm"
    }
