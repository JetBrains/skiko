val OS.dynamicLibExt: String
    get() = when (this) {
        OS.Linux -> ".so"
        OS.Windows -> ".dll"
        OS.MacOS -> ".dylib"
        OS.Wasm, OS.IOS -> error("Should not be called for $this")
    }
