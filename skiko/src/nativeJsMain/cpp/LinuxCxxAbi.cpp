#if defined(__linux__)

#include <cstdlib>

// Kotlin/Native emits user linker options before KLIB-embedded archives. Newer
// libstdc++ headers can therefore leave this late bridge-only helper unresolved.
// Keep a weak, fatal fallback in the bridge archive; a strongly linked C++
// runtime implementation remains free to replace it.
namespace std {
__attribute__((weak, noreturn)) void __throw_bad_array_new_length() {
    std::abort();
}
}

// libstdc++ 15+ uses this glibc 2.32 optimization flag from inline reference
// counting code. Kotlin/Native Linux still targets glibc 2.19, so provide a
// hidden conservative value: false means always use thread-safe atomics.
extern "C" {
__attribute__((visibility("hidden"))) char __libc_single_threaded = 0;
}

#endif
