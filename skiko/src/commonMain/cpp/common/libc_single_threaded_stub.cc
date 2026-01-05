#if defined(__linux__)
extern "C" {
// Kotlin/Native links against an older glibc sysroot for compatibility. Some toolchains (or headers) may cause
// libstdc++ to reference __libc_single_threaded, which is unavailable in that sysroot. Defining it here unblocks
// linking of Linux native binaries.
char __libc_single_threaded = 0;
}
#endif

