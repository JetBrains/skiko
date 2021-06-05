#include <stdio.h>
#include <stdlib.h>

#if defined(__GNUC__) && !defined(__clang__)
extern "C" void __attribute__((weak)) __warn_memset_zero_len() {
    fprintf(stderr, "__warn_memset_zero_len called\n");
    abort();
}
#endif